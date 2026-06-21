package com.edward.chat_system.modules.auth.dto.response;

public sealed interface AuthResponse permits AuthSuccessResponse, UnverifiedResponse {}
