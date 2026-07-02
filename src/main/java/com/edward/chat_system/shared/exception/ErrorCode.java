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
    CHANNEL_IS_NOT_EXIST("CHANNEL_IS_NOT_EXIST", "Channel is not exist", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_FOUND("EMAIL_NOT_FOUND", "Email not found", HttpStatus.NOT_FOUND),
    EMAIL_EXISTED("EMAIL_EXISTED", "Email existed", HttpStatus.CONFLICT),
    USERNAME_EXISTED("USERNAME_EXISTED", "Username existed", HttpStatus.CONFLICT),
    USER_NOT_FOUND("USER_NOT_FOUND", "User is not exist", HttpStatus.NOT_FOUND),
    USER_BANNED("USER_BANNED", "User is banned", HttpStatus.FORBIDDEN),
    USER_ALREADY_A_MEMBER("USER_ALREADY_A_MEMBER", "User is already a member", HttpStatus.CONFLICT),
    JWT_SIGNING_FAILED(
            "JWT_SIGNING_FAILED", "JWT signing failed", HttpStatus.INTERNAL_SERVER_ERROR),

    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(
            "UNAUTHORIZED", "You do not have permission for this action", HttpStatus.FORBIDDEN),

    UNCATEGORIZED("UNCATEGORIZED", "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    LOGIN_FAILED("LOGIN_FAILED", "Email or Password is incorrect", HttpStatus.UNAUTHORIZED),
    OTP_COOLDOWN(
            "OTP_COOLDOWN",
            "Otp is on cooldown, please wait before requesting a new code.",
            HttpStatus.TOO_MANY_REQUESTS),
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

    NOT_A_MEMBER("NOT_A_MEMBER", "User is not a member of a server", HttpStatus.FORBIDDEN),
    MISSING_PERMISSION(
            "MISSING_PERMISSION",
            "User isn't allowed to perform this action",
            HttpStatus.FORBIDDEN),

    ROLE_NAME_DUPLICATE("ROLE_NAME_DUPLICATE ", "Role name is duplicate", HttpStatus.CONFLICT),
    ROLE_NOT_EXIST("ROLE_NOT_EXIST", "Role is not exist", HttpStatus.NOT_FOUND),

    USER_ALREADY_ASSIGNED_FOR_THIS_ROLE(
            "USE_ALREADY_ASSIGNED_FOR_THIS_ROLE",
            "User is already assigned for this role",
            HttpStatus.CONFLICT),

    PERMISSION_DUPLICATE_FOR_THIS_ROLE(
            "PERMISSION_DUPLICATE_FOR_THIS_ROLE",
            "Permission is already assigned for this role",
            HttpStatus.CONFLICT),

    PERMISSION_DUPLICATE_FOR_THIS_USER(
            "PERMISSION_DUPLICATE_FOR_THIS_USER",
            "Permission is already assigned for this user",
            HttpStatus.CONFLICT),

    NOW_DO_NOT_HAVE_PERMISSION(
            "NOW_DO_NOT_HAVE_PERMISSION",
            "User isn't allowed to perform this action",
            HttpStatus.FORBIDDEN),
    INVALID_CURSOR("INVALID_CURSOR", "Invalid cursor", HttpStatus.BAD_REQUEST),
    SERVER_NAME_DUPLICATE("SERVER_NAME_DUPLICATE", "Server name is duplicate", HttpStatus.CONFLICT),
    SERVER_NOT_EXIST("SERVER_NOT_EXIST", "Server is not exist", HttpStatus.NOT_FOUND),

    CHANNEL_NAME_DUPLICATE(
            "CHANNEL_NAME_DUPLICATE", "Channel name is duplicate", HttpStatus.CONFLICT),

    INVITE_LINK_NOT_FOUND(
            "INVITE_LINK_NOT_FOUND", "Invite link is not found", HttpStatus.NOT_FOUND),
    ;
    // INVALID_DOB("Age of user must be at least {value}",
    // HttpStatus.UNPROCESSABLE_CONTENT),
    String code;
    String message;
    HttpStatusCode statusCode;
}
