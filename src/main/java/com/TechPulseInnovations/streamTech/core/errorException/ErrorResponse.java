package com.TechPulseInnovations.streamTech.core.errorException;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Envoltorio estandarizado para respuestas de error en toda la API
 * Proporciona estructura consistente con: status, message, description, stackTrace
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /**
     * Timestamp cuando ocurrió el error (ISO 8601 format)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP (ej: 400, 401, 500)
     */
    private int status;

    /**
     * Mensaje principal del error (corto, orientado al usuario)
     * Ejemplo: "Error de autenticación", "Recurso no encontrado"
     */
    private String message;

    /**
     * Descripción detallada del error (contexto específico)
     * Ejemplo: "Las credenciales proporcionadas no son válidas"
     */
    private String description;

    /**
     * Código de error personalizado para fácil identificación
     * Ejemplo: "AUTH_FAILED", "INVALID_REQUEST", "DB_ERROR"
     */
    private String errorCode;

    /**
     * Stack trace de la excepción formateado y legible
     * Cada línea es un método en la cadena de llamadas
     */
    private List<String> stackTrace;

    /**
     * Ruta del endpoint donde ocurrió el error
     * Ejemplo: "/api/code-reception/get-code"
     */
    private String path;

    /**
     * Campo adicional para errores de validación
     * Mapea campo -> lista de errores
     */
    private java.util.Map<String, List<String>> fieldErrors;

    /**
     * Constructor simplificado para respuestas rápidas sin stackTrace
     */
    public ErrorResponse(int status, String message, String description, String errorCode, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.description = description;
        this.errorCode = errorCode;
        this.path = path;
    }

    /**
     * Constructor para deserialización sin stackTrace en cliente
     */
    public ErrorResponse(int status, String message, String description) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.description = description;
    }
}
