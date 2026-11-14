package com.edward.chat_system.infrastructure.security.jwt;

import com.edward.chat_system.features.auth.enums.TokenTypeEnum;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtClaimObject {
    String userId;
    String username;
    String email;
    TokenTypeEnum tokenType;
}
