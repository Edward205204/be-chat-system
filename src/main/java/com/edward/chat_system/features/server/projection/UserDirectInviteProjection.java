package com.edward.chat_system.features.server.projection;

import java.time.LocalDateTime;

import com.edward.chat_system.features.server.enums.InviteStatusEnum;

public interface UserDirectInviteProjection {
    String getId();
    String getServerId();
    String getServerName();
    String getServerAvater();
    String getInviterId();
    String getInviterUserName();
    String getInviterDisplayName();
    String getInviterAvatar();
    InviteStatusEnum getStatus();
    LocalDateTime getExpiresAt();
    LocalDateTime getCreatedAt();
}
