package com.example.blogsystem.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
public class CryptoUtil {

    // Exactly 32 bytes (256 bits) for AES-256 and exactly 16 bytes (128 bits) for IV
    private static final String SECRET_KEY = "BlogViet_Secure_AES256_Key_2026_"; // 32 chars
    private static final String INIT_VECTOR = "BlogVietInitVec1"; // 16 chars
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private final SecretKeySpec keySpec;
    private final IvParameterSpec ivSpec;

    public CryptoUtil() {
        byte[] keyBytes = new byte[32];
        byte[] rawKey = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(rawKey, 0, keyBytes, 0, Math.min(rawKey.length, 32));

        byte[] ivBytes = new byte[16];
        byte[] rawIv = INIT_VECTOR.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(rawIv, 0, ivBytes, 0, Math.min(rawIv.length, 16));

        this.keySpec = new SecretKeySpec(keyBytes, "AES");
        this.ivSpec = new IvParameterSpec(ivBytes);
    }

    /**
     * Encrypt plaintext into Base64 ciphertext
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("[CRYPTO ERROR] Lỗi mã hóa dữ liệu: {}", e.getMessage());
            return plainText;
        }
    }

    /**
     * Decrypt Base64 ciphertext back to plaintext
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText.trim());
            byte[] original = cipher.doFinal(decoded);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[CRYPTO WARNING] Không thể giải mã chuỗi: {}", e.getMessage());
            return cipherText;
        }
    }

    public static String getStaticKey() {
        return SECRET_KEY;
    }

    public static String getStaticIv() {
        return INIT_VECTOR;
    }
}
