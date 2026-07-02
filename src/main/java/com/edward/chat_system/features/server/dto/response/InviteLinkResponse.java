package com.edward.chat_system.features.server.dto.response;

import com.edward.chat_system.features.user.dto.response.UserBasicInfoResponse;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InviteLinkResponse {
    String id;
    String token;
    String inviteUrl;
    UserBasicInfoResponse createdBy;
    long useCount;
    LocalDateTime expiresAt;
    LocalDateTime createdAt;
    boolean isRevoked;
}
