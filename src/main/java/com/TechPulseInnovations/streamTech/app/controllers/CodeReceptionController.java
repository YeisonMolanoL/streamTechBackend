package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.codeReception.dtos.CodeResponseDto;
import com.TechPulseInnovations.streamTech.codeReception.dtos.GetCodeRequestDto;
import com.TechPulseInnovations.streamTech.app.services.EncryptionService;
import com.TechPulseInnovations.streamTech.app.services.ImapManagerService;
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
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@Slf4j
@RequiredArgsConstructor
public class CodeReceptionController {

    private final LongPollingService longPollingService;
    private final ImapManagerService imapManagerService;
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
}
