package com.edward.chat_system.features.server.projection;

import java.time.LocalDateTime;

public interface MemberProjection {
    String getMemberId();

    String getUserId();

    String getDisplayName();

    String getUsername();

    String getAvatar();

    boolean getIsMuted();

    LocalDateTime getJoinedAt();

    String getOwnerId();
}
