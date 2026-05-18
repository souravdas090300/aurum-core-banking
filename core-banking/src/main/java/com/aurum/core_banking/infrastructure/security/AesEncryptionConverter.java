package com.aurum.core_banking.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JPA {@link AttributeConverter} that transparently encrypts/decrypts sensitive
 * column values using AES-256-GCM.
 *
 * <p>Annotate a JPA entity field with {@code @Convert(converter = AesEncryptionConverter.class)}
 * to enable column-level encryption. Used for {@code national_id_enc} in the customers table.
 *
 * <p>Cipher text format: Base64( IV[12 bytes] | GCM ciphertext + tag )
 */
@Slf4j
@Converter
@Component
public class AesEncryptionConverter implements AttributeConverter<String, String> {

    private static final String  ALGORITHM   = "AES/GCM/NoPadding";
    private static final int     GCM_IV_LEN  = 12;
    private static final int     GCM_TAG_LEN = 128;

    private final SecretKeySpec secretKey;

    public AesEncryptionConverter(@Value("${encryption.secret-key}") String hexKey) {
        byte[] keyBytes = hexKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        // Accept exactly 32 bytes (256-bit)
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                "AES key must be exactly 32 bytes; got " + keyBytes.length);
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LEN, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt field value", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            byte[] iv         = new byte[GCM_IV_LEN];
            byte[] cipherText = new byte[combined.length - GCM_IV_LEN];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LEN);
            System.arraycopy(combined, GCM_IV_LEN, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LEN, iv));
            return new String(cipher.doFinal(cipherText), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt field value", e);
        }
    }
}
