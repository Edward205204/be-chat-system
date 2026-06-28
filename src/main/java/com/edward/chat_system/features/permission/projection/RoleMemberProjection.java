package com.edward.chat_system.features.permission.projection;

public interface RoleMemberProjection {
    String getServerMemberId();

    String getUserId();

    String getDisplayName();

    String getUsername();

    String getAvatar();
}
