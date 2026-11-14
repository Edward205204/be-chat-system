package com.edward.chat_system.infrastructure.mail;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MailTemplate {
    OTP_VERIFICATION("otp", "Verify your email - RChat"),
    FORGOT_PASSWORD("forgot-password", "Reset your password - RChat");
    private final String templateName;
    private final String subject;
}
