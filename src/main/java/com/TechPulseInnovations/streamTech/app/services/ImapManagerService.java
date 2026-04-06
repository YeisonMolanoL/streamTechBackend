package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.codeReception.entities.Code;
import com.TechPulseInnovations.streamTech.configuration.authModule.configuration.modells.AccountRecord;
import com.TechPulseInnovations.streamTech.app.repository.CodeRepository;
import com.TechPulseInnovations.streamTech.app.repository.AccountRepository;
import com.TechPulseInnovations.streamTech.app.repository.ParsingRuleRepository;
import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio para manejar conexiones IMAP y escuchar correos entrantes
 * Mantiene conexiones persistentes por cada cuenta de email
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImapManagerService {

    private final EncryptionService encryptionService;
    private final CodeRepository codeRepository;
    private final CodeAssignmentService assignmentService;
    private final ParsingRuleRepository parsingRuleRepository;
    private final AccountRepository accountRepository;

    // Mantiene Store activos por email
    private final Map<String, Store> activeStores = new ConcurrentHashMap<>();
    
    // Mantiene listeners activos por email
    private final Map<String, ImapListener> activeListeners = new ConcurrentHashMap<>();

    /**
     * Inicializa la escucha para una cuenta de email
     */
    @Async
    public void startListeningForEmail(String email) {
        AccountRecord account = null;
        try {
            log.info("Iniciando escucha para email: {}", email);
            
            Optional<AccountRecord> accountOpt = accountRepository.findByAccountEmail(email);
            if (accountOpt.isEmpty()) {
                log.error("Cuenta de email no encontrada: {}", email);
                return;
            }

            account = accountOpt.get();
            
            // No iniciar si ya está activo
            if (activeStores.containsKey(email)) {
                log.warn("Ya hay una conexión activa para: {}", email);
                return;
            }

            // Crear conexión IMAP
            String decryptedPassword = encryptionService.decrypt(account.getAccountPassword());
            Store store = createImapConnection(account.getImapHost(), account.getImapPort(), 
                                             account.getImapSecure(), email, decryptedPassword);

            if (store == null) {
                log.error("No se pudo conectar a IMAP para: {}", email);
                throw new Exception("No se pudo conectar a IMAP");
            }

            activeStores.put(email, store);

            // Obtener carpeta INBOX
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Crear listener
            ImapListener listener = new ImapListener(this, email, inbox);
            activeListeners.put(email, listener);
            inbox.addMessageCountListener(listener);

            log.info("Escucha iniciada exitosamente para: {}", email);

            // Actualizar estado
            account.setIsImapActive(true);
            account.setConnectionError(null);
            accountRepository.save(account);

            keepListeningIdle(email, store, inbox);
        } catch (Exception e) {
            log.error("Error iniciando escucha para email: {}", email, e);
            if (account != null) {
                account.setIsImapActive(false);
                account.setConnectionError(e.getMessage());
                accountRepository.save(account);
            }
            stopListeningForEmail(email);
        }
        }

    private void reconnect(String email) {
        try {
            log.info("Reiniciando conexión para: {}", email);

            stopListeningForEmail(email);

            Thread.sleep(3000); // pequeño delay

            startListeningForEmail(email);

        } catch (Exception e) {
            log.error("Error reconectando: {}", email, e);
        }
    }

    private void keepListeningIdle(String email, Store store, Folder folder) {
        while (true) {
            try {
                if (!store.isConnected()) {
                    log.warn("Store desconectado, reconectando: {}", email);
                    reconnect(email);
                    return;
                }

                if (!(folder instanceof com.sun.mail.imap.IMAPFolder)) {
                    log.error("Folder no soporta IDLE para: {}", email);
                    return;
                }

                com.sun.mail.imap.IMAPFolder imapFolder = (com.sun.mail.imap.IMAPFolder) folder;

                log.info("Esperando correos (IDLE) para: {}", email);

                // 🔥 AQUÍ SE BLOQUEA HASTA QUE LLEGA UN CORREO
                imapFolder.idle();

            } catch (FolderClosedException e) {
                log.warn("Folder cerrado, reconectando: {}", email);
                reconnect(email);
                return;

            } catch (StoreClosedException e) {
                log.warn("Store cerrado, reconectando: {}", email);
                reconnect(email);
                return;

            } catch (Exception e) {
                log.error("Error en IDLE para: {}", email, e);

                try {
                    Thread.sleep(5000); // evita loop agresivo
                } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * Detiene la escucha para una cuenta
     */
    public void stopListeningForEmail(String email) {
        try {
            log.info("Deteniendo escucha para email: {}", email);

            ImapListener listener = activeListeners.remove(email);
            if (listener != null) {
                try {
                    listener.getFolder().close(false);
                } catch (Exception e) {
                    log.warn("Error cerrando carpeta para: {}", email, e);
                }
            }

            Store store = activeStores.remove(email);
            if (store != null && store.isConnected()) {
                store.close();
            }

            // Actualizar estado en BD
            Optional<AccountRecord> accountOpt = accountRepository.findByAccountEmail(email);
            if (accountOpt.isPresent()) {
                AccountRecord account = accountOpt.get();
                account.setIsImapActive(false);
                accountRepository.save(account);
            }

            log.info("Escucha detenida para: {}", email);
        } catch (Exception e) {
            log.error("Error deteniendo escucha para email: {}", email, e);
        }
    }

    /**
     * Procesa un mensaje de correo para extraer el código
     */
    @Transactional
    public void processMessage(String toEmail, Message message) {
        try {
            String subject = message.getSubject();
            String content = getMessageContent(message);

            log.info("Procesando mensaje para {} - Asunto: {}", toEmail, subject);

            // Extraer código según reglas de parsing
            String extractedCode = extractCode(content);

            if (extractedCode != null && !extractedCode.isEmpty()) {
                // Guardar código en BD
                Code code = Code.builder()
                    .email(toEmail)
                    .code(extractedCode)
                    .isAssigned(false)
                    .createdAt(LocalDateTime.now())
                    .build();

                codeRepository.save(code);
                log.info("Código guardado - Email: {}, Código: {}", toEmail, extractedCode);

                // Intentar asignar a una solicitud pendiente
                assignmentService.assignCodeToOldestRequest(toEmail);
            } else {
                log.warn("No se pudo extraer código del mensaje para: {}", toEmail);
            }

        } catch (Exception e) {
            log.error("Error procesando mensaje para email: {}", toEmail, e);
        }
    }

    /**
     * Extrae el código del contenido del mensaje usando regex
     */
    private String extractCode(String content) {
        System.out.println("Contenido: " + content);
        try {
            // Buscar código en diferentes formatos
            String[] patterns = {
                    "c[oó]digo[:\\s]+(\\d{4,6})",   // "código: 1234" o "codigo 123456"
                    "code[:\\s]+(\\d{4,6})",        // "code: 1234" o "code 123456"
                    "\\b(\\d{6})\\b",               // 6 dígitos exactos
                    "\\b(\\d{4})\\b",               // 4 dígitos exactos
                    "\\b([A-Z0-9]{8})\\b"           // 8 alfanuméricos
            };

            for (String patternStr : patterns) {
                Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(content);

                if (matcher.find()) {
                    return matcher.group(1);
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error extrayendo código", e);
            return null;
        }
    }

    /**
     * Obtiene el contenido de un mensaje (texto simple)
     */
    private String getMessageContent(Message message) {
        try {
            Object content = message.getContent();
            
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof Multipart) {
                Multipart mp = (Multipart) content;
                StringBuilder result = new StringBuilder();
                
                for (int i = 0; i < mp.getCount(); i++) {
                    Part part = mp.getBodyPart(i);
                    if (part.isMimeType("text/plain")) {
                        result.append(part.getContent());
                    }
                }
                return result.toString();
            }
        } catch (Exception e) {
            log.warn("Error obteniendo contenido del mensaje", e);
        }
        return "";
    }

    /**
     * Crea una conexión IMAP
     */
    private Store createImapConnection(String host, Integer port, Boolean secure,
                                      String email, String password) {
        try {
            Properties props = new Properties();
            props.put("mail.imap.host", host);
            props.put("mail.imap.port", port);
            props.put("mail.imap.starttls.enable", secure);
            props.put("mail.imap.starttls.required", secure);
            props.put("mail.imap.ssl.enable", secure);
            props.put("mail.imap.ssl.protocols", "TLSv1.2");
            props.put("mail.imap.connectiontimeout", 10000);
            props.put("mail.imap.timeout", 10000);
            props.put("mail.imap.enableimapevents", "true");

            Session session = Session.getInstance(props);
            Store store = session.getStore("imap");
            store.connect(host, email, password);

            if (store.isConnected()) {
                log.info("Conexión IMAP exitosa para: {}", email);
                return store;
            }

            return null;
        } catch (Exception e) {
            log.error("Error creando conexión IMAP para: {}", email, e);
            return null;
        }
    }

    /**
     * Listener para mensajes nuevos en IMAP
     */
    private static class ImapListener extends MessageCountAdapter {

        private final ImapManagerService imapService;
        private final String email;
        private final Folder folder;

        ImapListener(ImapManagerService imapService, String email, Folder folder) {
            this.imapService = imapService;
            this.email = email;
            this.folder = folder;
        }

        @Override
        public void messagesAdded(MessageCountEvent event) {
            try {
                Message[] messages = event.getMessages();
                for (Message msg : messages) {
                    imapService.processMessage(email, msg);
                }
            } catch (Exception e) {
                log.error("Error en listener de mensajes", e);
            }
        }

        public Folder getFolder() {
            return folder;
        }
    }

    /**
     * Obtiene todas las cuentas activas
     */
    public Set<String> getActiveEmails() {
        return activeStores.keySet();
    }

    /**
     * Verifica si una conexión está activa
     */
    public boolean isEmailActive(String email) {
        Store store = activeStores.get(email);
        return store != null && store.isConnected();
    }
}
