package com.TechPulseInnovations.streamTech.configuration.authModule.configuration;

import com.TechPulseInnovations.streamTech.configuration.authModule.core.jwt.JwtEntryPoint;
import com.TechPulseInnovations.streamTech.configuration.authModule.core.jwt.JwtTokenFilter;
import com.TechPulseInnovations.streamTech.configuration.authModule.services.UserDetailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Spring Security con JWT
 * - Autenticación mediante tokens JWT
 * - Manejo centralizado de 401 (no autorizado) y 403 (acceso denegado)
 * - CORS habilitado
 * - Endpoints públicos: /api/code-reception/**, /users/login, /users/create
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {

    @Autowired
    private JwtEntryPoint jwtEntryPoint;

    @Autowired
    private UserDetailService userDetailService;

    /**
     * Manejador para acceso denegado (403)
     * Retorna JSON en lugar del error HTML por defecto
     */
    private final AccessDeniedHandler accessDeniedHandler = (request, response, accessDeniedException) -> {
        log.warn("Acceso denegado: {} {} - Reason: {}", 
            request.getMethod(), 
            request.getRequestURI(), 
            accessDeniedException.getMessage());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpServletResponse.SC_FORBIDDEN);
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", "No tienes permiso para acceder a este recurso");
        errorResponse.put("details", accessDeniedException.getMessage());
        errorResponse.put("path", request.getRequestURI());

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    };

    @Bean
    public JwtTokenFilter jwtTokenFilter() {
        return new JwtTokenFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers("/api/code-reception/**", "/users/login", "/users/create").permitAll();
                    
                    registry.requestMatchers("/admin/**").hasAuthority("ADMIN");
                    
                    registry.requestMatchers("/platform/**").hasAnyAuthority("USER", "ADMIN");
                    
                    registry.anyRequest().authenticated();
                })
                
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling.authenticationEntryPoint(jwtEntryPoint);
                    
                    exceptionHandling.accessDeniedHandler(accessDeniedHandler);
                })
                
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return userDetailService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
