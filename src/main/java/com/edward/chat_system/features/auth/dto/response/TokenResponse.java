package com.edward.chat_system.features.auth.dto.response;

import com.edward.chat_system.infrastructure.jwt.JwtSignerResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenResponse {
    JwtSignerResponse accessToken;
    JwtSignerResponse refreshToken;
}
