package com.edward.chat_system.features.server.dto.response;

import com.edward.chat_system.features.server.enums.InviteStatusEnum;
import com.edward.chat_system.features.user.dto.response.UserBasicInfoResponse;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DirectInvitationResponse {
    String id;
    ServerBasicInfoResponse server;
    UserBasicInfoResponse inviter;
    InviteStatusEnum status;
    LocalDateTime expiresAt;
    LocalDateTime createdAt;
}
