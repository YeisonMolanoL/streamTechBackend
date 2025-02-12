package com.TechPulseInnovations.streamTech.configuration.authModule.core.jwt;


import com.TechPulseInnovations.streamTech.configuration.authModule.services.UserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class JwtTokenFilter extends OncePerRequestFilter {
    private final static Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);
    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserDetailService userDetailService;

    private String getToken(HttpServletRequest request){
        System.out.println("request :>>" + request.toString());
        logger.info("yo " + request.getHeader("Authorization"));
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer ")) {
            logger.info("Aca va esta");
            return header.replace("Bearer ", "");
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
        String token = getToken(request);
        if(token != null && jwtProvider.validateToken(token)){
            String nombreUsuario = jwtProvider.getNombreUsuario(token);
            UserDetails userDetails = userDetailService.loadUserByUsername(nombreUsuario);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }catch (Exception e){
        logger.error("Fail el metodo doFilterInternal " + e.getMessage());
    }
        filterChain.doFilter(request, response);
    }
}
