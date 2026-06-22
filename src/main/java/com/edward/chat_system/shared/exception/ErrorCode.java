package com.edward.chat_system.shared.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    EMAIL_EXISTED("EMAIL_EXISTED", "Email existed", HttpStatus.CONFLICT),
    USERNAME_EXISTED("USERNAME_EXISTED", "Username existed", HttpStatus.CONFLICT),
    JWT_SIGNING_FAILED(
            "JWT_SIGNING_FAILED", "JWT signing failed", HttpStatus.INTERNAL_SERVER_ERROR),

    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("UNAUTHORIZED", "You do not have permisson", HttpStatus.FORBIDDEN),
    INVALID_TOKEN_TYPE("INVALID_TOKEN_TYPE", "Invalid token type", HttpStatus.UNAUTHORIZED),

    UNCATEGORIZED("UNCATEGORIZED", "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    LOGIN_FAILED("LOGIN_FAILED", "Email or Password is incorect", HttpStatus.UNAUTHORIZED);
    // INVALID_DOB("Age of user must be at least {value}",
    // HttpStatus.UNPROCESSABLE_CONTENT),

    String code;
    String message;
    HttpStatusCode statusCode;
}
