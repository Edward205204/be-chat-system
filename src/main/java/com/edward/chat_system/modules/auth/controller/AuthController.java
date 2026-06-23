package com.edward.chat_system.modules.auth.controller;

import com.edward.chat_system.modules.auth.dto.request.LoginRequest;
import com.edward.chat_system.modules.auth.dto.request.RegisterRequest;
import com.edward.chat_system.modules.auth.dto.request.VerifyEmailRequest;
import com.edward.chat_system.modules.auth.dto.response.AuthResponse;
import com.edward.chat_system.modules.auth.dto.response.TokenResponse;
import com.edward.chat_system.modules.auth.dto.response.UnverifiedResponse;
import com.edward.chat_system.modules.auth.service.AuthService;
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
            @RequestBody @Valid VerifyEmailRequest otp, @AuthenticationPrincipal Jwt principal) {
        TokenResponse tokens = authService.verifyEmailOtp(principal.getSubject(), otp.getOtp());
        return ApiResponse.<TokenResponse>builder()
                .message("Verify email successfully")
                .result(tokens)
                .build();
    }
}
