package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.configuration.authModule.configuration.modells.AccountRecord;
import com.TechPulseInnovations.streamTech.app.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio que sincroniza y reinicializa listeners IMAP al arrancar la aplicación
 * 
 * PROBLEMA RESUELTO:
 * - Cuando el sistema se reinicia, los listeners en memoria se pierden
 * - Pero la BD mostraba isImapActive=TRUE aunque no había listeners activos
 * - Esta clase garantiza que:
 *   1. Al arrancar, busca todas las cuentas marcadas como activas en BD
 *   2. Reinicia los listeners real es
 *   3. Si falla alguno, lo marca como inactivo en BD con el error
 *   4. Mantiene la sincronización BD <-> Memoria 100% consistente
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImapInitializerService {

    private final ImapManagerService imapManagerService;
    private final AccountRepository accountRepository;
    private volatile boolean initialized = false;

    /**
     * Se ejecuta automáticamente cuando Spring termina de inicializar el contexto
     * Sincroniza todos los listeners IMAP que deben estar activos
     */
    @EventListener(ContextRefreshedEvent.class)
    public void initializeImapListenersOnStartup() {
        if (initialized) {
            log.info("ImapInitializerService ya fue inicializado, saltando reinicio");
            return;
        }

        initialized = true;
        log.info("░░░ INICIANDO SINCRONIZACIÓN DE LISTENERS IMAP ░░░");

        try {
            // Buscar todas las cuentas que fueron marcadas como activas
            List<AccountRecord> activeAccounts = accountRepository.findByIsImapActiveTrue();
            
            if (activeAccounts.isEmpty()) {
                log.info("✓ No hay listeners IMAP que reiniciar en esta sesión");
                return;
            }

            log.info("═══════════════════════════════════════════════════════════════");
            log.info("Encontradas {} cuentas para reinicializar listeners", activeAccounts.size());
            log.info("═══════════════════════════════════════════════════════════════");

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            // Reiniciar listeners fallidos
            for (AccountRecord account : activeAccounts) {
                String email = account.getAccountEmail();
                log.info("Reiniciando listener para: {}", email);

                try {
                    // Intentar reiniciar el listener
                    imapManagerService.startListeningForEmail(email);
                    successCount.incrementAndGet();
                    log.info("✓ Listener reiniciado exitosamente: {}", email);

                } catch (Exception e) {
                    log.error("✗ Error reiniciando listener para {}: {}", email, e.getMessage());
                    
                    // Si falla, marcar como inactivo en BD
                    try {
                        account.setIsImapActive(false);
                        account.setConnectionError("Fallo al reiniciar en startup: " + e.getMessage());
                        accountRepository.save(account);
                        log.warn("Marcado como inactivo en BD: {}", email);
                        failCount.incrementAndGet();
                    } catch (Exception ex) {
                        log.error("Error actualizando BD para {}: {}", email, ex.getMessage());
                    }
                }
            }

            log.info("═══════════════════════════════════════════════════════════════");
            log.info("Resultado de sincronización de listeners:");
            log.info("  ✓ Exitosos: {}", successCount.get());
            log.info("  ✗ Fallidos: {}", failCount.get());
            log.info("  ~ Total: {}", activeAccounts.size());
            log.info("═══════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            log.error("Error crítico en inicialización de listeners IMAP", e);
        }
    }

    /**
     * Método manual para forzar resincronización bajo demanda
     * Útil para debugging y administración
     */
    public void resyncAllListeners() {
        log.info("Forzando resincronización manual de todos los listeners");
        initialized = false;
        initializeImapListenersOnStartup();
    }

    /**
     * Obtiene estadísticas actuales de listeners
     */
    public ImapListenerStats getListenerStats() {
        List<AccountRecord> dbActive = accountRepository.findByIsImapActiveTrue();
        int memoryActive = imapManagerService.getActiveEmails().size();
        int inconsistent = dbActive.size() - memoryActive;

        return ImapListenerStats.builder()
            .databaseActive(dbActive.size())
            .memoryActive(memoryActive)
            .inconsistent(Math.abs(inconsistent))
            .isConsistent(inconsistent == 0)
            .activeEmails(imapManagerService.getActiveEmails())
            .build();
    }

    /**
     * DTO para estadísticas de listeners
     */
    @lombok.Builder
    @lombok.Getter
    public static class ImapListenerStats {
        private final int databaseActive;      // Cuentas en BD con isImapActive=true
        private final int memoryActive;        // Listeners realmente activos en memoria
        private final int inconsistent;        // Diferencia (inconsistencias)
        private final boolean isConsistent;    // ¿Están sincronizados?
        private final java.util.Set<String> activeEmails;  // Emails con listeners activos
    }
}
