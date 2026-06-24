package com.edward.chat_system.features.auth.dto.response;

public sealed interface AuthResponse permits AuthSuccessResponse, UnverifiedResponse {}
