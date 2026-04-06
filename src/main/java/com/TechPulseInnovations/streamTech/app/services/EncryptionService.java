package com.TechPulseInnovations.streamTech.app.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.SecureRandom;

/**
 * Servicio para encriptar y desencriptar contraseñas de correos
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EncryptionService {

    @Value("${encryption.key:defaultKeyFor32BytesLengthAES256}")
    private String encryptionKeyString;

    private static final String ALGORITHM = "AES";

    /**
     * Encripta una contraseña
     */
    public String encrypt(String plainPassword) {
        try {
            SecretKey key = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            byte[] encryptedBytes = cipher.doFinal(plainPassword.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Error encriptando contraseña", e);
            throw new RuntimeException("Error encriptando contraseña", e);
        }
    }

    /**
     * Desencripta una contraseña
     */
    public String decrypt(String encryptedPassword) {
        try {
            SecretKey key = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("Error desencriptando contraseña", e);
            throw new RuntimeException("Error desencriptando contraseña", e);
        }
    }

    /**
     * Genera la clave secreta a partir del string configurado
     */
    private SecretKey generateKey() {
        // Asegurar que la clave tenga exactamente 32 bytes para AES-256
        String key = encryptionKeyString;
        if (key.length() < 32) {
            key = String.format("%-32s", key).replace(" ", "0");
        } else if (key.length() > 32) {
            key = key.substring(0, 32);
        }
        
        byte[] decodedKey = key.getBytes();
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }
}
