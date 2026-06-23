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

    UNCATEGORIZED("UNCATEGORIZED", "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    LOGIN_FAILED("LOGIN_FAILED", "Email or Password is incorect", HttpStatus.UNAUTHORIZED),
    OTP_COOLDOWN(
            "OTP_COOLDOWN",
            "Otp is on cooldown, please wait before requesting a new code.",
            HttpStatus.TOO_MANY_REQUESTS),
    ACCOUNT_VERIFIED("ACCOUNT_VERIFIED", "Account is verified", HttpStatus.CONFLICT),
    OTP_DOES_NOT_EXIST(
            "OTP_DOES_NOT_EXIST",
            "No OTP request found. Please request a new one.",
            HttpStatus.BAD_REQUEST),
    OTP_INCORRECT("OTP_INCORRECT", "Otp is incorrect", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("OTP_EXPIRED", "Verify failed because otp expired", HttpStatus.BAD_REQUEST),
    OTP_REVOKED(
            "OTP_REVOKED",
            "Verify failed because otp has bean revoked, please request a new one.",
            HttpStatus.TOO_MANY_REQUESTS),
    OTP_MAX_ATTEMPTS_EXCEEDED(
            "OTP_MAX_ATTEMPTS_EXCEEDED",
            "You have entered an incorrect OTP 5 times. Your OTP has"
                    + " been revoked. Please request a new one.",
            HttpStatus.TOO_MANY_REQUESTS),
    OTP_HAS_BEEN_USED("OTP_HAS_BEEN_USED", "Otp has been used", HttpStatus.BAD_REQUEST),
    ;
    // INVALID_DOB("Age of user must be at least {value}",
    // HttpStatus.UNPROCESSABLE_CONTENT),

    String code;
    String message;
    HttpStatusCode statusCode;
}
