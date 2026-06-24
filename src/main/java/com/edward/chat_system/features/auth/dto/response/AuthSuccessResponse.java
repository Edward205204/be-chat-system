package com.edward.chat_system.features.auth.dto.response;

import com.edward.chat_system.features.user.dto.response.UserResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class AuthSuccessResponse implements AuthResponse {
    String accessToken;
    String refreshToken;
    UserResponse user;
}
