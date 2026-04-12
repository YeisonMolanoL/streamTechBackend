package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.codeReception.dtos.CodeResponseDto;
import com.TechPulseInnovations.streamTech.codeReception.dtos.GetCodeRequestDto;
import com.TechPulseInnovations.streamTech.app.services.EncryptionService;
import com.TechPulseInnovations.streamTech.app.services.ImapManagerService;
import com.TechPulseInnovations.streamTech.app.services.ImapInitializerService;
import com.TechPulseInnovations.streamTech.app.services.LongPollingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para recepción de códigos por correo
 * Endpoint principal: POST /api/code-reception/get-code
 */
@RestController
@RequestMapping("/api/code-reception")
@Slf4j
@RequiredArgsConstructor
public class CodeReceptionController {

    private final LongPollingService longPollingService;
    private final ImapManagerService imapManagerService;
    private final ImapInitializerService imapInitializerService;
    private final EncryptionService encryptionService;

    /**
     * ENDPOINT PRINCIPAL: Obtiene un código por correo (30s timeout)
     * 
     * @param request Contiene el email del usuario
     * @return CodeResponseDto con el código o error
     */
    @PostMapping("/get-code")
    public ResponseEntity<CodeResponseDto> getCode(@RequestBody GetCodeRequestDto request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                log.warn("Solicitud con email vacío");
                return ResponseEntity.badRequest().body(
                    CodeResponseDto.builder()
                        .success(false)
                        .message("Email requerido")
                        .build()
                );
            }

            String email = request.getEmail().trim().toLowerCase();
            log.info("Solicitud de código para email: {}", email);

            // Llamar a long polling
            CodeResponseDto response = longPollingService.waitForCode(email);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error en endpoint get-code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CodeResponseDto.builder()
                    .success(false)
                    .message("Error procesando solicitud")
                    .build()
            );
        }
    }

    /**
     * Health check del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<Object> health() {
        return ResponseEntity.ok(
            Map.of(
                "status", "UP",
                "service", "code-reception",
                "activeEmails", imapManagerService.getActiveEmails().size()
            )
        );
    }

    /**
     * ENDPOINT DE DEBUG: Obtiene estadísticas de listeners
     * Muestra qué listeners realmente están activos vs qué dice la BD
     * Resuelve inconsistencias entre estado en memoria y en BD
     */
    @GetMapping("/listeners/stats")
    public ResponseEntity<Object> getListenerStats() {
        ImapInitializerService.ImapListenerStats stats = imapInitializerService.getListenerStats();
        return ResponseEntity.ok(
            Map.of(
                "status", stats.isConsistent() ? "✓ SINCRONIZADO" : "⚠ INCONSISTENTE",
                "databaseMarkedActive", stats.getDatabaseActive(),
                "memoryActiveNow", stats.getMemoryActive(),
                "inconsistencies", stats.getInconsistent(),
                "activeEmails", stats.getActiveEmails()
            )
        );
    }

    /**
     * ENDPOINT DE DEBUG: Estado de salud de un listener específico
     * Verifica sincronización BD <-> Memoria para un email
     */
    @GetMapping("/listeners/health/{email}")
    public ResponseEntity<Object> getListenerHealth(@PathVariable String email) {
        ImapManagerService.ListenerHealthStatus health = imapManagerService.getListenerHealthStatus(email);
        return ResponseEntity.ok(
            Map.of(
                "email", health.getEmail(),
                "memoryStore", health.isMemoryStore(),
                "memoryListener", health.isMemoryListener(),
                "databaseMarked", health.isDatabaseMarked(),
                "healthy", health.isHealthy(),
                "status", health.isHealthy() ? "✓ OK" : "⚠ PROBLEMA",
                "error", health.getError()
            )
        );
    }

    /**
     * ENDPOINT DE DEBUG: Fuerza sincronización manual
     * Útil si hay inconsistencias detectadas
     * Limpia listeners fantasma y reinicia los que fallaron
     */
    @PostMapping("/listeners/sync")
    public ResponseEntity<Object> forceSync() {
        log.info("Solicitado sync manual de listeners");
        try {
            imapManagerService.syncListenerStateWithDatabase();
            imapInitializerService.resyncAllListeners();
            return ResponseEntity.ok(
                Map.of(
                    "status", "success",
                    "message", "Sincronización completada",
                    "activeNow", imapManagerService.getActiveEmails().size()
                )
            );
        } catch (Exception e) {
            log.error("Error en sync manual", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                    "status", "error",
                    "message", e.getMessage()
                )
            );
        }
    }
}
