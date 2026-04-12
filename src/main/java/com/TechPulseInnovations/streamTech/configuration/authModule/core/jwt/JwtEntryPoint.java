package com.TechPulseInnovations.streamTech.configuration.authModule.core.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador de autenticación fallida (error 401)
 * Se ejecuta cuando se intenta acceder a un endpoint protegido sin token válido
 * Retorna JSON en lugar de HTML
 */
@Component
@Slf4j
public class JwtEntryPoint implements AuthenticationEntryPoint {

    /**
     * Se invoca cuando hay un AuthenticationException
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.warn("Acceso no autorizado: {} - {} - Reason: {}", 
            request.getMethod(), 
            request.getRequestURI(), 
            authException.getMessage());

        // Preparar respuesta JSON
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Autenticación requerida");
        errorResponse.put("details", authException.getMessage());
        errorResponse.put("path", request.getRequestURI());

        // Serializar y escribir respuesta
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
