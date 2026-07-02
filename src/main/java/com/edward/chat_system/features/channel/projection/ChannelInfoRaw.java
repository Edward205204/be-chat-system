package com.edward.chat_system.features.channel.projection;

import java.time.LocalDateTime;

public interface ChannelInfoRaw {
    String getChannelId();

    String getName();

    boolean getIsPrivate();

    LocalDateTime getCreatedAt();
}
