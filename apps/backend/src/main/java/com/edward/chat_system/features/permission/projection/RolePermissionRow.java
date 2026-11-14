package com.edward.chat_system.features.permission.projection;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;

public interface RolePermissionRow {
    String getRoleId();

    String getRoleName();

    ChannelPermissionKeyEnum getPermission();
}
