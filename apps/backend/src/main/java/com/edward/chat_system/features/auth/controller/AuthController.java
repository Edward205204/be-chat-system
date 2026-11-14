package com.edward.chat_system.features.auth.controller;

import com.edward.chat_system.features.auth.dto.request.*;
import com.edward.chat_system.features.auth.dto.response.AuthResponse;
import com.edward.chat_system.features.auth.dto.response.TokenResponse;
import com.edward.chat_system.features.auth.dto.response.UnverifiedResponse;
import com.edward.chat_system.features.auth.service.AuthService;
import com.edward.chat_system.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthController {
    AuthService authService;

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .message("Login successfully")
                .result(authService.login(request))
                .build();
    }

    @PostMapping("/register")
    ApiResponse<UnverifiedResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponse.<UnverifiedResponse>builder()
                .message("Register successfully")
                .result(authService.register(request))
                .build();
    }

    @PostMapping("/send/email-otp")
    ApiResponse<Void> sendOtpVerifyEmail(@AuthenticationPrincipal Jwt principal) {
        authService.sendOtpVerifyEmail(principal.getSubject(), principal.getClaim("email"));
        return ApiResponse.<Void>builder().message("Send otp successfully").build();
    }

    @PostMapping("/verify/email-otp")
    ApiResponse<TokenResponse> verifyEmailOtp(
            @RequestBody @Valid VerifyEmailRequest request,
            @AuthenticationPrincipal Jwt principal) {
        TokenResponse tokens = authService.verifyEmailOtp(principal.getSubject(), request.getOtp());
        return ApiResponse.<TokenResponse>builder()
                .message("Verify email successfully")
                .result(tokens)
                .build();
    }

    @PostMapping("/send/forgot-password")
    ApiResponse<Void> sendOtpForgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.sendOtpForgotPassword(request.getEmail());
        return ApiResponse.<Void>builder()
                .message("If an account exists, an OTP will be sent to your email.")
                .build();
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getOtp(), request.getPassword());
        return ApiResponse.<Void>builder().message("Reset password successfully").build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody @Valid TokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.<Void>builder().message("Logout successfully").build();
    }

    @PostMapping("/refresh")
    ApiResponse<TokenResponse> refreshToken(@RequestBody @Valid TokenRequest request) {
        return ApiResponse.<TokenResponse>builder()
                .message("Refresh Token successfully")
                .result(authService.refreshToken(request.getRefreshToken()))
                .build();
    }
}
