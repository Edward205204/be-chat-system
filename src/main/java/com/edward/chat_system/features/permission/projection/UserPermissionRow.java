package com.edward.chat_system.features.permission.projection;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;

public interface UserPermissionRow {
    String getMemberId();

    String getUserId();

    String getDisplayName(); // từ User entity

    ChannelPermissionKeyEnum getPermission();
}
