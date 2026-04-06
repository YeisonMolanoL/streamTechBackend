package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.codeReception.dtos.CodeResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para notificar entre el listener IMAP y el LongPollingService
 * Mantiene los futures pendientes y los completa cuando hay códigos disponibles
 */
@Service
@Slf4j
public class CodeNotificationService {

    // Mapa de futures por requestId
    private final ConcurrentHashMap<Long, CompletableFuture<CodeResponseDto>> requestFutures = 
        new ConcurrentHashMap<>();

    /**
     * Registra un future para una solicitud
     */
    public void registerFuture(Long requestId, CompletableFuture<CodeResponseDto> future) {
        requestFutures.put(requestId, future);
        log.debug("Future registrado para request ID: {}", requestId);
    }

    /**
     * Notifica que hay un código disponible para una solicitud
     */
    public void notifyCodeAvailable(Long requestId, String code) {
        CompletableFuture<CodeResponseDto> future = requestFutures.remove(requestId);
        
        if (future != null && !future.isDone()) {
            CodeResponseDto response = CodeResponseDto.builder()
                .success(true) 
                .code(code)
                .message("Código recibido exitosamente")
                .build();
            
            future.complete(response);
            log.info("Future completado para request ID: {} con código", requestId);
        } else {
            log.warn("No hay future pendiente para request ID: {}", requestId);
        }
    }

    /**
     * Limpia un future si no se llegó a usar
     */
    public void removeFuture(Long requestId) {
        requestFutures.remove(requestId);
        log.debug("Future eliminado para request ID: {}", requestId);
    }

    /**
     * Obtiene el número de requests en espera
     */
    public int getPendingRequestCount() {
        return requestFutures.size();
    }
}
