package com.aurum.core_banking.infrastructure.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JPA {@link AttributeConverter} that transparently encrypts/decrypts sensitive string
 * columns using AES-256 in GCM mode (authenticated encryption — provides both
 * confidentiality and integrity).
 *
 * <p>Storage format: {@code Base64( IV[12 bytes] || CipherText+AuthTag )}.
 * A fresh random IV is generated per encryption so identical plaintexts produce
 * different ciphertexts (probabilistic encryption — prevents frequency analysis).
 *
 * <p>Usage on an entity field:
 * <pre>{@code
 *   @Convert(converter = AesEncryptionConverter.class)
 *   @Column(name = "national_id_enc")
 *   private String nationalId;
 * }</pre>
 *
 * The encryption key MUST be exactly 32 bytes (256 bits) and supplied via the
 * {@code ENCRYPTION_KEY} environment variable in production.
 */
@Slf4j
@Converter
@Component
public class AesEncryptionConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM  = "AES/GCM/NoPadding";
    private static final int    IV_LENGTH  = 12;   // 96-bit IV — recommended for GCM
    private static final int    TAG_LENGTH = 128;  // 128-bit authentication tag

    @Value("${app.security.encryption-key}")
    private String encryptionKey;

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[]    iv     = generateIv();
            SecretKey key    = buildKey();
            Cipher    cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] cipherText = cipher.doFinal(plaintext.getBytes());
            byte[] combined   = new byte[IV_LENGTH + cipherText.length];
            System.arraycopy(iv,         0, combined, 0,         IV_LENGTH);
            System.arraycopy(cipherText, 0, combined, IV_LENGTH, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Column encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String stored) {
        if (stored == null) return null;
        try {
            byte[] combined   = Base64.getDecoder().decode(stored);
            byte[] iv         = new byte[IV_LENGTH];
            byte[] cipherText = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0,         iv,         0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.length);

            SecretKey key    = buildKey();
            Cipher    cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));

            return new String(cipher.doFinal(cipherText));
        } catch (Exception e) {
            log.error("Column decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private SecretKey buildKey() {
        byte[] keyBytes = encryptionKey.getBytes();
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "app.security.encryption-key must be exactly 32 bytes (256 bits); " +
                    "current length: " + keyBytes.length);
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
