package com.TechPulseInnovations.streamTech.configuration.authModule.core.jwt;

import com.TechPulseInnovations.streamTech.configuration.authModule.models.UserRecord;
import com.TechPulseInnovations.streamTech.configuration.authModule.models.UsuarioPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final static Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private int expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        UsuarioPrincipal usuarioPrincipal = (UsuarioPrincipal) authentication.getPrincipal();
        // Crear y firmar el token
        return Jwts.builder()
                .setSubject(usuarioPrincipal.getUsername())  // Establecer el nombre de usuario como 'subject'
                .setIssuedAt(new Date())  // Establecer la fecha de emisión
                .setExpiration(new Date(new Date().getTime() + expiration * 1000))  // Establecer la expiración
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Usar la clave secreta generada
                .compact();  // Generar el token
    }

    public String getNombreUsuario(String token) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Jwts.parserBuilder()
                .setSigningKey(secretBytes)  // Establecer la clave secreta como arreglo de bytes
                .build()  // Construir el parser
                .parseClaimsJws(token)  // Parsear el JWT
                .getBody()  // Obtener el cuerpo de los claims
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            // Usar parserBuilder para crear el JwtParser
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())  // Establecer la clave
                    .build()  // Construir el parser
                    .parseClaimsJws(token);  // Parsear el JWT

            return true;  // El token es válido
        } catch (MalformedJwtException e) {
            logger.error("Token mal formado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token no soportado");
        } catch (ExpiredJwtException e) {
            logger.error("Token expirado");
        } catch (IllegalArgumentException e) {
            logger.error("Token vacío");
        } catch (SignatureException e) {
            logger.error("Error en la firma");
        }
        return false;  // El token no es válido
    }
}
