package com.edward.chat_system.features.auth.exception;

import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpCooldownException extends AppException {
    private final LocalDateTime lastSendAt;

    public OtpCooldownException(ErrorCode errorCode, LocalDateTime lastSendAt) {
        super(errorCode);
        this.lastSendAt = lastSendAt;
    }
}
