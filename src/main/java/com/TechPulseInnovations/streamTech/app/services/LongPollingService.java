package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.codeReception.entities.Code;
import com.TechPulseInnovations.streamTech.codeReception.entities.CodeRequest;
import com.TechPulseInnovations.streamTech.codeReception.dtos.CodeResponseDto;
import com.TechPulseInnovations.streamTech.app.repository.CodeRequestRepository;
import com.TechPulseInnovations.streamTech.app.repository.CodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para manejar el long polling
 * Implementa CompletableFuture para mejor manejo de threads (no bloquea)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LongPollingService {

    private final CodeRequestRepository codeRequestRepository;
    private final CodeRepository codeRepository;
    private final CodeAssignmentService assignmentService;
    private final ImapManagerService imapManagerService;
    private final CodeNotificationService notificationService;

    @Value("${code-reception.polling-timeout:30000}")
    private long pollingTimeoutMs;

    /**
     * Espera hasta 30 segundos por un código para un email
     * 🚀 NO BLOQUEA - usa CompletableFuture para no consumir threads innecesariamente
     * 
     * Flujo:
     * 1. Crear solicitud pendiente
     * 2. Registrar el future antes de esperar
     * 3. Buscar si ya hay código o si ya fue asignado
     * 4. Si existe, retornar inmediatamente
     * 5. Si no existe, esperar con timeout
     * 6. Listener IMAP notifica cuando llega código
     */
    public CodeResponseDto waitForCode(String email) {
        try {
            log.info("Iniciando espera por código para email: {}", email);

            // 1. Crear solicitud pendiente
            CodeRequest request = CodeRequest.builder()
                .email(email)
                .status(CodeRequest.CodeRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

            CodeRequest savedRequest = codeRequestRepository.save(request);
            log.info("Solicitud creada - ID: {}, Email: {}", savedRequest.getId(), email);

            // 2. Registrar el future antes de buscar códigos nuevos
            CompletableFuture<CodeResponseDto> future = new CompletableFuture<>();
            notificationService.registerFuture(savedRequest.getId(), future);

            // 3. Buscar si ya hay código sin asignar (llegó antes de la request)
            Optional<Code> existingCode = codeRepository.findFirstUnassignedByEmail(email);
            if (existingCode.isPresent()) {
                Code code = existingCode.get();
                log.info("Código ya existe para email: {} - Code ID: {}", email, code.getId());
                
                // Asignar inmediatamente
                code.setIsAssigned(true);
                code.setAssignedAt(LocalDateTime.now());
                code.setAssignedToRequestId(savedRequest.getId());
                codeRepository.save(code);

                savedRequest.setAssignedCodeId(code.getId());
                savedRequest.setStatus(CodeRequest.CodeRequestStatus.ASSIGNED);
                savedRequest.setAsignedAt(LocalDateTime.now());
                codeRequestRepository.save(savedRequest);

                notificationService.removeFuture(savedRequest.getId());
                return CodeResponseDto.builder()
                    .success(true)
                    .code(code.getCode())
                    .message("Código encontrado en sistema")
                    .build();
            }

            // 4. Iniciar escucha IMAP si no está activa
            if (!imapManagerService.isEmailActive(email)) {
                imapManagerService.startListeningForEmail(email);
                // Pequeño delay para que la escucha se establezca
                Thread.sleep(100);
            }

            // 5. Verificar si la solicitud fue asignada mientras registrábamos el future
            Optional<Code> assignedCode = assignmentService.getAssignedCode(savedRequest.getId());
            if (assignedCode.isPresent()) {
                Code code = assignedCode.get();
                notificationService.removeFuture(savedRequest.getId());
                return CodeResponseDto.builder()
                    .success(true)
                    .code(code.getCode())
                    .message("Código asignado mientras se procesaba la solicitud")
                    .build();
            }

            try {
                log.debug("Esperando código con timeout de {} ms para request ID: {}", 
                         pollingTimeoutMs, savedRequest.getId());
                
                // ✅ AQUÍ NO SE BLOQUEA - CompletableFuture se completa cuando llega código
                CodeResponseDto result = future.get(pollingTimeoutMs, TimeUnit.MILLISECONDS);
                
                log.info("Código obtenido para request ID: {} - Código: {}", 
                         savedRequest.getId(), result.getCode());
                return result;

            } catch (java.util.concurrent.TimeoutException e) {
                log.info("Timeout en espera para email: {} - Request ID: {}", 
                         email, savedRequest.getId());
                
                // Marcar como expirada
                assignmentService.expireRequest(savedRequest.getId());
                
                return CodeResponseDto.builder()
                    .success(false)
                    .message("No llegó ningún código (timeout 30s)")
                    .build();

            } finally {
                // Limpiar future del registro
                notificationService.removeFuture(savedRequest.getId());
            }

        } catch (InterruptedException e) {
            log.warn("Thread interrumpido en long polling para email: {}", email);
            Thread.currentThread().interrupt();
            return CodeResponseDto.builder()
                .success(false)
                .message("Solicitud interrumpida")
                .build();

        } catch (Exception e) {
            log.error("Error en long polling para email: {}", email, e);
            return CodeResponseDto.builder()
                .success(false)
                .message("Error procesando solicitud")
                .build();
        }
    }
}
