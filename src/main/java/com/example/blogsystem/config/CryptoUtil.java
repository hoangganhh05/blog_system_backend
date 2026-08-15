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

    // 256-bit Secret Key (32 bytes) and 128-bit IV (16 bytes)
    private static final String SECRET_KEY = "BlogVietSecureKey2026AES256Secret";
    private static final String INIT_VECTOR = "BlogVietInitVec1";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private final SecretKeySpec keySpec;
    private final IvParameterSpec ivSpec;

    public CryptoUtil() {
        this.keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        this.ivSpec = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
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
            log.warn("[CRYPTO WARNING] Không thể giải mã chuỗi (có thể là dữ liệu thường chưa mã hóa): {}", e.getMessage());
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
