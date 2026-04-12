package com.TechPulseInnovations.streamTech.configuration.handler;

import com.TechPulseInnovations.streamTech.core.errorException.ErrorCode;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorResponse;
import com.TechPulseInnovations.streamTech.core.errorException.StackTraceFormatter;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Manejador centralizado de excepciones para toda la API REST
 * Convierte TODAS las excepciones en respuestas JSON estructuradas y coherentes
 * 
 * Estructura de respuesta:
 * {
 *   "timestamp": "2026-04-11T10:30:45",
 *   "status": 500,
 *   "message": "Error interno del servidor",
 *   "description": "Detalles específicos del error",
 *   "errorCode": "INTERNAL_SERVER_ERROR",
 *   "path": "/api/endpoint",
 *   "stackTrace": ["com.package.Class.method (File.java:123)", ...]
 * }
 */
@ControllerAdvice
@Slf4j
public class RestExceptionHandler {

    private static final int MAX_STACK_TRACE_LINES = 10;

    /**
     * Maneja excepciones personalizadas de negocio (StreamTechException)
     * Se usan para errores de lógica de negocio
     */
    @ExceptionHandler(StreamTechException.class)
    public ResponseEntity<ErrorResponse> handleStreamTechException(StreamTechException ex, WebRequest request) {
        log.warn("StreamTechException: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Error de validación")
                .description(ex.getMessage())
                .errorCode(ErrorCode.INVALID_REQUEST.getCode())
                .path(getRequestPath(request))
                .stackTrace(StackTraceFormatter.formatStackTrace(ex, MAX_STACK_TRACE_LINES))
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de autenticación genéricas
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("AuthenticationException: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Autenticación fallida")
                .description(ex.getMessage())
                .errorCode(ErrorCode.AUTH_FAILED.getCode())
                .path(getRequestPath(request))
                .stackTrace(StackTraceFormatter.formatStackTrace(ex, MAX_STACK_TRACE_LINES))
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja BadCredentialsException (credenciales incorrectas)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.warn("BadCredentialsException: Las credenciales proporcionadas son incorrectas");

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Credenciales inválidas")
                .description("El email o contraseña proporcionados son incorrectos")
                .errorCode(ErrorCode.INVALID_CREDENTIALS.getCode())
                .path(getRequestPath(request))
                .stackTrace(StackTraceFormatter.formatStackTrace(ex, MAX_STACK_TRACE_LINES))
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Argumento inválido")
                .description(ex.getMessage())
                .errorCode(ErrorCode.INVALID_FORMAT.getCode())
                .path(getRequestPath(request))
                .stackTrace(StackTraceFormatter.formatStackTrace(ex, MAX_STACK_TRACE_LINES))
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex, WebRequest request) {
        log.error("NullPointerException - Referencia nula no esperada", ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Error interno del servidor")
                .description("Se encontró una referencia nula inesperada durante la procesamiento")
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .path(getRequestPath(request))
                .stackTrace(StackTraceFormatter.formatStackTrace(ex, MAX_STACK_TRACE_LINES))
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maneja excepciones de acceso a datos / base de datos
     */
    @ExceptionHandler({
            org.springframework.dao.DataAccessException.class,
            org.springframework.dao.DataIntegrityViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleDataAccessException(Exception ex, WebRequest request) {
        log.error("Error en acceso a base de datos", ex);

        String description = ex.getMessage() != null ?
                ex.getMessage() :
                "Error al acceder a la base de datos. Por favor intente más tarde.";

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Error de base de datos")
                .description(description)
                .errorCode(ErrorCode.DATABASE_ERROR.getCode())
                .path(getRequestPath(request))
                .stackTrace(StackTraceFormatter.formatStackTrace(ex, MAX_STACK_TRACE_LINES))
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Captura TODAS las demás excepciones no manejadas
     * Esto previene que se retorne el error Whitelabel HTML por defecto
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Exception genérica no capturada - Tipo: " + ex.getClass().getSimpleName(), ex);

        // Detectar tipo de error automáticamente
        ErrorCode errorCode = ErrorCode.fromException(ex);
        HttpStatus status = errorCode.getStatus();

        String message = Objects.requireNonNullElse(ex.getMessage(), 
                "Error inesperado durante la procesamiento de la solicitud");

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(status.getReasonPhrase())
                .description(StackTraceFormatter.getDetailedMessage(ex))
                .errorCode(errorCode.getCode())
                .path(getRequestPath(request))
                .stackTrace(StackTraceFormatter.formatStackTrace(ex, MAX_STACK_TRACE_LINES))
                .build();

        log.error("ErrorResponse enviada al cliente - Code: {}, Status: {}, Message: {}",
                errorCode.getCode(), status.value(), error.getMessage());

        return new ResponseEntity<>(error, status);
    }

    /**
     * Extrae la ruta del request de manera segura
     * 
     * @param request El WebRequest
     * @return Ruta del endpoint o "unknown"
     */
    private String getRequestPath(WebRequest request) {
        try {
            return request.getDescription(false).replace("uri=", "");
        } catch (Exception e) {
            return "unknown";
        }
    }
}
