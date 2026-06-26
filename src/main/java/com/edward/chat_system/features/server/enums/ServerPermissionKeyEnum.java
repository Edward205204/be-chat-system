package com.edward.chat_system.features.server.enums;

public enum ServerPermissionKeyEnum {
    NONE, // Do not insert this value into db.
    MANAGE_CHANNELS,
    CREATE_INVITE,
    MANAGE_SERVER,
    KICK_MEMBER,
    BAN_MEMBER,
    MUTE_MEMBER,
    MANAGE_ROLES
}
