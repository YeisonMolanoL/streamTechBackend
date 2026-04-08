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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/code-reception/") || path.equals("/users/login") || path.equals("/users/create");
    }

    private String getToken(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer ")) {
            return header.replace("Bearer ", "");
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getToken(request);
            if (token != null) {
                // Log only partial token for debugging (avoid full token leaks)
                String tokenHint = token.length() > 8 ? "****" + token.substring(token.length() - 8) : token;
                logger.debug("JwtTokenFilter: token found (len={}), hint={}", token.length(), tokenHint);

                if (jwtProvider == null) {
                    logger.error("JwtTokenFilter: jwtProvider is null (injection failed?)");
                }
                if (userDetailService == null) {
                    logger.error("JwtTokenFilter: userDetailService is null (injection failed?)");
                }

                boolean valid = false;
                try {
                    valid = jwtProvider != null && jwtProvider.validateToken(token);
                } catch (Exception ex) {
                    logger.error("JwtTokenFilter: error while validating token", ex);
                }

                if (valid) {
                    String userName = jwtProvider.getUserName(token);
                    logger.debug("JwtTokenFilter: token valid for user={}", userName);
                    UserDetails userDetails = userDetailService.loadUserByUsername(userName);
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    logger.warn("JwtTokenFilter: no valid token (proceeding without authentication)");
                }
            }
        } catch (Exception e) {
            logger.error("Fail in the method doFilterInternal", e);
            throw e;
        }
        filterChain.doFilter(request, response);
    }
}
