package com.TechPulseInnovations.streamTech.core.errorException;

import org.springframework.http.HttpStatus;

/**
 * Enumeración de códigos de error estandarizados
 * Facilita identificación consistente y traducción de errores
 */
public enum ErrorCode {

    // Errores de autenticación (401)
    AUTH_FAILED("AUTH_FAILED", "Autenticación fallida", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Credenciales inválidas", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token expirado o inválido", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("UNAUTHORIZED", "No autorizado para esta operación", HttpStatus.FORBIDDEN),

    // Errores de validación (400)
    INVALID_REQUEST("INVALID_REQUEST", "Solicitud inválida", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("VALIDATION_ERROR", "Error de validación", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER("MISSING_PARAMETER", "Parámetro requerido faltante", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT("INVALID_FORMAT", "Formato de datos inválido", HttpStatus.BAD_REQUEST),

    // Errores de recurso (404)
    NOT_FOUND("NOT_FOUND", "Recurso no encontrado", HttpStatus.NOT_FOUND),
    EMAIL_ACCOUNT_NOT_FOUND("EMAIL_ACCOUNT_NOT_FOUND", "Cuenta de email no encontrada", HttpStatus.NOT_FOUND),
    CODE_REQUEST_NOT_FOUND("CODE_REQUEST_NOT_FOUND", "Solicitud de código no encontrada", HttpStatus.NOT_FOUND),

    // Errores de conflicto (409)
    CONFLICT("CONFLICT", "Conflicto en la solicitud", HttpStatus.CONFLICT),
    DUPLICATE_ENTRY("DUPLICATE_ENTRY", "El registro ya existe", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "El email ya está registrado", HttpStatus.CONFLICT),

    // Errores de negocio (422)
    UNPROCESSABLE_ENTITY("UNPROCESSABLE_ENTITY", "Entidad no procesable", HttpStatus.UNPROCESSABLE_ENTITY),
    IMAP_CONNECTION_FAILED("IMAP_CONNECTION_FAILED", "Fallo al conectar con IMAP", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_EMAIL_ACCOUNT("INVALID_EMAIL_ACCOUNT", "Configuración de email inválida", HttpStatus.UNPROCESSABLE_ENTITY),

    // Errores de servidor (500)
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR("DATABASE_ERROR", "Error en base de datos", HttpStatus.INTERNAL_SERVER_ERROR),
    ENCRYPTION_ERROR("ENCRYPTION_ERROR", "Error en encriptación de datos", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAP_ERROR("IMAP_ERROR", "Error en operación IMAP", HttpStatus.INTERNAL_SERVER_ERROR),

    // Errores de sobrecarga (503)
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Servicio no disponible", HttpStatus.SERVICE_UNAVAILABLE),

    // Otros
    UNKNOWN("UNKNOWN", "Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String description;
    private final HttpStatus status;

    ErrorCode(String code, String description, HttpStatus status) {
        this.code = code;
        this.description = description;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Obtiene el ErrorCode correspondiente a una excepción
     * 
     * @param exception La excepción
     * @return ErrorCode apropiadoo
     */
    public static ErrorCode fromException(Throwable exception) {
        if (exception == null) {
            return UNKNOWN;
        }

        String exceptionName = exception.getClass().getSimpleName();

        if (exceptionName.contains("Authentication") || exceptionName.contains("Credentials")) {
            return AUTH_FAILED;
        } else if (exceptionName.contains("Validation") || exceptionName.contains("Constraint")) {
            return VALIDATION_ERROR;
        } else if (exceptionName.contains("NotFound")) {
            return NOT_FOUND;
        } else if (exceptionName.contains("Database") || exceptionName.contains("DataAccess")) {
            return DATABASE_ERROR;
        } else if (exception instanceof StreamTechException) {
            return INTERNAL_SERVER_ERROR;
        }

        return UNKNOWN;
    }
}
