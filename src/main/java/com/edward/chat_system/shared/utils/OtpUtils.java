package com.edward.chat_system.shared.utils;

import java.security.SecureRandom;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OtpUtils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateOtp() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
}
