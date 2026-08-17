package com.edward.chat_system.features.server.projection;

import com.edward.chat_system.features.server.enums.InviteStatusEnum;
import java.time.LocalDateTime;

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
