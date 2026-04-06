package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.codeReception.entities.Code;
import com.TechPulseInnovations.streamTech.codeReception.entities.CodeRequest;
import com.TechPulseInnovations.streamTech.app.repository.CodeRepository;
import com.TechPulseInnovations.streamTech.app.repository.CodeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio para asignar códigos a solicitudes (FIFO)
 * Usa transacciones con bloqueo pesimista para evitar race conditions
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CodeAssignmentService {

    private final CodeRepository codeRepository;
    private final CodeRequestRepository codeRequestRepository;
    private final CodeNotificationService notificationService;

    /**
     * Asigna un código a la solicitud más antigua pendiente para un email
     * Usa transacción con bloqueo pesimista para garantizar FIFO
     * Notifica al LongPollingService cuando se asigna exitosamente
     * 
     * @return Optional con el código asignado, o empty si no hay solicitud o código
     */
    @Transactional
    public Optional<Code> assignCodeToOldestRequest(String email) {
        try {
            // 1. Obtener la solicitud más antigua PENDIENTE con lock
            Optional<CodeRequest> oldestRequest = 
                codeRequestRepository.findFirstPendingByEmailWithLock(email);
            
            if (oldestRequest.isEmpty()) {
                log.debug("No hay solicitudes pendientes para el email: {}", email);
                return Optional.empty();
            }

            // 2. Obtener el código más antiguo SIN ASIGNAR con lock
            Optional<Code> unassignedCode = 
                codeRepository.findFirstUnassignedByEmailWithLock(email);
            
            if (unassignedCode.isEmpty()) {
                log.debug("No hay códigos sin asignar para el email: {}", email);
                return Optional.empty();
            }

            // 3. Asignar el código a la solicitud
            CodeRequest request = oldestRequest.get();
            Code code = unassignedCode.get();

            code.setIsAssigned(true);
            code.setAssignedAt(LocalDateTime.now());
            code.setAssignedToRequestId(request.getId());
            codeRepository.save(code);

            request.setAssignedCodeId(code.getId());
            request.setStatus(CodeRequest.CodeRequestStatus.ASSIGNED);
            request.setAsignedAt(LocalDateTime.now());
            codeRequestRepository.save(request);

            log.info("Código asignado - Request ID: {}, Code ID: {}, Email: {}, Código: {}", 
                     request.getId(), code.getId(), email, code.getCode());
            
            // 4. Notificar al LongPollingService que el código está disponible
            notificationService.notifyCodeAvailable(request.getId(), code.getCode());
            
            return Optional.of(code);

        } catch (Exception e) {
            log.error("Error asignando código al email: {}", email, e);
            return Optional.empty();
        }
    }

    /**
     * Verifica si una solicitud ya tiene código asignado
     */
    public Optional<Code> getAssignedCode(Long requestId) {
        try {
            Optional<CodeRequest> request = codeRequestRepository.findById(requestId);
            
            if (request.isEmpty()) {
                return Optional.empty();
            }

            CodeRequest cr = request.get();
            if (cr.getAssignedCodeId() == null) {
                return Optional.empty();
            }

            return codeRepository.findById(cr.getAssignedCodeId());
        } catch (Exception e) {
            log.error("Error obteniendo código asignado para request: {}", requestId, e);
            return Optional.empty();
        }
    }

    /**
     * Marca una solicitud como expirada
     */
    @Transactional
    public void expireRequest(Long requestId) {
        try {
            Optional<CodeRequest> request = codeRequestRepository.findById(requestId);
            if (request.isPresent()) {
                CodeRequest cr = request.get();
                if (cr.getStatus() == CodeRequest.CodeRequestStatus.PENDING) {
                    cr.setStatus(CodeRequest.CodeRequestStatus.EXPIRED);
                    cr.setExpiredAt(LocalDateTime.now());
                    codeRequestRepository.save(cr);
                    log.info("Request expirado - ID: {}", requestId);
                }
            }
        } catch (Exception e) {
            log.error("Error expirando request: {}", requestId, e);
        }
    }
}
