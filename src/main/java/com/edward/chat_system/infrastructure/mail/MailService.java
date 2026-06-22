package com.edward.chat_system.infrastructure.mail;

public interface MailService {
    void sendOtp(String to, String otpCode);
}
