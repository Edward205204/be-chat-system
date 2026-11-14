package com.edward.chat_system.shared.utils;

import java.security.SecureRandom;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SecureTokenGenerator {

    private static final int BYTE_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String BASE62_CHARS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public String generate() {
        byte[] bytes = new byte[BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return encodeBase62(bytes);
    }

    private String encodeBase62(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            // 0xFF để convert signed byte → unsigned int
            sb.append(BASE62_CHARS.charAt((b & 0xFF) % 62));
        }
        return sb.toString();
    }
}
