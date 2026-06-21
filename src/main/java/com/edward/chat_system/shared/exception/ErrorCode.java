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
    USER_EXISTED("USER_EXISTED", "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED("USER_NOT_EXISTED", "User not existed", HttpStatus.NOT_FOUND),
    JWT_SIGNING_FAILED("JWT_SIGNING_FAILED", "JWT signing failed", HttpStatus.INTERNAL_SERVER_ERROR),

    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("UNAUTHORIZED", "You do not have permisson", HttpStatus.FORBIDDEN),

    UNCATEGORIZED("UNCATEGORIZED", "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    LOGIN_FAILED("LOGIN_FAILED", "Email or Password is incorect", HttpStatus.UNAUTHORIZED);
    // INVALID_DOB("Age of user must be at least {value}",
    // HttpStatus.UNPROCESSABLE_CONTENT),

    String code;
    String message;
    HttpStatusCode statusCode;
}
