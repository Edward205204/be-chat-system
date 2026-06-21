package com.edward.chat_system.modules.auth.controller;

import com.edward.chat_system.modules.auth.dto.request.LoginRequest;
import com.edward.chat_system.modules.auth.dto.request.RegisterRequest;
import com.edward.chat_system.modules.auth.dto.response.AuthResponse;
import com.edward.chat_system.modules.auth.dto.response.UnverifiedResponse;
import com.edward.chat_system.modules.auth.service.AuthService;
import com.edward.chat_system.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthService authService;

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .message("Login sucessfully")
                .result(authService.login(request))
                .build();
    }

    @PostMapping("/register")
    ApiResponse<UnverifiedResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponse.<UnverifiedResponse>builder()
                .message("Register sucessfully")
                .result(authService.register(request))
                .build();
    }
}
