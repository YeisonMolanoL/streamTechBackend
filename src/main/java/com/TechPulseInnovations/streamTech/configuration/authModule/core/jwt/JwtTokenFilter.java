package com.TechPulseInnovations.streamTech.configuration.authModule.core.jwt;


import com.TechPulseInnovations.streamTech.configuration.authModule.services.UserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filtro JWT que valida tokens en cada request
 * Evita filtrar endpoints públicos (code-reception, login, create)
 */
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserDetailService userDetailService;

    /**
     * Los siguientes endpoints NO requieren JWT
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/code-reception/") || 
               path.equals("/users/login") || 
               path.equals("/users/create");
    }

    /**
     * Extrae el token del header Authorization
     */
    private String getToken(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer ")) {
            return header.replace("Bearer ", "");
        }
        return null;
    }

    /**
     * Procesa el filtro JWT
     * Si el token es válido, establece la autenticación
     * Si no hay token o es inválido, permite continuar (Spring Security rechazará si endpoint requiere auth)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getToken(request);
            if (token != null) {
                // Log solo una pista del token para debugging (evita exponer el token completo)
                String tokenHint = token.length() > 8 ? "****" + token.substring(token.length() - 8) : token;
                log.debug("JwtTokenFilter: token encontrado (len={}), hint={}", token.length(), tokenHint);

                if (jwtProvider == null) {
                    log.error("JwtTokenFilter: jwtProvider es null (¿inyección fallida?)");
                }
                if (userDetailService == null) {
                    log.error("JwtTokenFilter: userDetailService es null (¿inyección fallida?)");
                }

                boolean valid = false;
                try {
                    valid = jwtProvider != null && jwtProvider.validateToken(token);
                } catch (Exception ex) {
                    log.warn("JwtTokenFilter: error validando token: {}", ex.getMessage());
                    // No relanzar la excepción, solo continuar sin autenticar
                }

                if (valid) {
                    try {
                        String userName = jwtProvider.getUserName(token);
                        log.debug("JwtTokenFilter: token válido para usuario={}", userName);
                        UserDetails userDetails = userDetailService.loadUserByUsername(userName);
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    } catch (Exception ex) {
                        log.warn("JwtTokenFilter: error cargando usuario: {}", ex.getMessage());
                        // No establecer autenticación si hay error
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    log.warn("JwtTokenFilter: token inválido (continuando sin autenticación)");
                }
            }
        } catch (Exception e) {
            log.error("JwtTokenFilter: excepción en doFilterInternal", e);
            // NO relanzar la excepción - dejar que continúe el flujo
            // Spring Security manejará la autenticación faltante
        }
        
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JwtTokenFilter: error en filterChain", e);
            throw e;
        }
    }
}
