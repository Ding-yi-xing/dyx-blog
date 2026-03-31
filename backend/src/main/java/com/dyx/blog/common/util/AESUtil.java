package com.dyx.blog.common.util;

import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.config.DyxSecurityProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 对称加密工具类。
 * 用于加密数据库中的敏感配置信息。
 */
@Component
public class AESUtil {

    private static String secretKey;

    public AESUtil(DyxSecurityProperties dyxSecurityProperties) {
        AESUtil.secretKey = dyxSecurityProperties.getEncryptKey();
    }

    private static final String ALGORITHM = "AES";

    /**
     * 加密字符串。
     *
     * @param data 待加密内容。
     * @return 加密后的 Base64 字符串。
     */
    public static String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        try {
            SecretKeySpec spec = new SecretKeySpec(getValidKey(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new BusinessException("敏感信息加密失败");
        }
    }

    /**
     * 解密字符串。
     *
     * @param encryptedData 加密后的 Base64 字符串。
     * @return 解密后的原始内容。
     */
    public static String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        try {
            SecretKeySpec spec = new SecretKeySpec(getValidKey(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, spec);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] original = cipher.doFinal(decoded);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 如果解密失败，可能是原始内容（未加密），直接返回
            return encryptedData;
        }
    }

    /**
     * 确保密钥长度为 16/24/32 字节。
     */
    private static byte[] getValidKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] validKey = new byte[32]; // 强制 256 位
        System.arraycopy(keyBytes, 0, validKey, 0, Math.min(keyBytes.length, 32));
        return validKey;
    }
}
