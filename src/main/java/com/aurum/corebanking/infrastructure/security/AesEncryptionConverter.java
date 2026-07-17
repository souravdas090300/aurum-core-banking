package com.aurum.corebanking.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JPA {@link AttributeConverter} that transparently encrypts/decrypts sensitive
 * column values using AES-256-GCM.
 *
 * <p>Annotate a JPA entity field with
 * {@code @Convert(converter = AesEncryptionConverter.class)}
 * to enable column-level encryption.
 * Used for {@code national_id_enc} in the customers table.
 *
 * <p>Cipher text format: Base64( IV[12 bytes] | GCM ciphertext + auth-tag )
 */
@Slf4j
@Converter
public class AesEncryptionConverter implements AttributeConverter<String, String> {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LEN = 12;
    private static final int GCM_TAG_LEN = 128;
    private static final SecretKeySpec SECRET_KEY;

    static {
        // For development only – use a fixed key (64 hex chars = 32 bytes)
        // NEVER use this in production! Use environment variables.
        String hexKey = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        byte[] keyBytes = decodeHex(hexKey);
        SECRET_KEY = new SecretKeySpec(keyBytes, "AES");
        log.info("AES-256-GCM encryption converter initialised with static key (DEV ONLY)");
    }

    // ─── AttributeConverter implementation ──────────────────────────────────

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            new SecureRandom().nextBytes(iv);   // fresh random IV per encryption

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY,
                        new GCMParameterSpec(GCM_TAG_LEN, iv));

            byte[] cipherText = cipher.doFinal(
                    plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Prepend IV to ciphertext so we can extract it on decryption
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv,         0, combined, 0,         iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt column value", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] combined   = Base64.getDecoder().decode(dbData);
            byte[] iv         = new byte[GCM_IV_LEN];
            byte[] cipherText = new byte[combined.length - GCM_IV_LEN];

            System.arraycopy(combined, 0,          iv,         0, GCM_IV_LEN);
            System.arraycopy(combined, GCM_IV_LEN, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY,
                        new GCMParameterSpec(GCM_TAG_LEN, iv));

            return new String(
                    cipher.doFinal(cipherText),
                    java.nio.charset.StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt column value", e);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Decodes a hex string (e.g. "0a1b2c...") into a byte array.
     *
     * @param hex even-length hex string
     * @return decoded bytes
     * @throws IllegalArgumentException if the string has odd length or invalid chars
     */
    private static byte[] decodeHex(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Hex string must be non-null and have even length; got: " +
                    (hex == null ? "null" : hex.length() + " chars"));
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(
                    hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}