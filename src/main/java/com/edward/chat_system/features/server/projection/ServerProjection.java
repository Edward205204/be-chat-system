package com.edward.chat_system.features.server.projection;

import java.time.LocalDateTime;

public interface ServerProjection {
    String getServerId();

    String getName();

    String getAvatar();

    String getBanner();

    String getOwnerId();

    LocalDateTime getJoinedAt();
}
