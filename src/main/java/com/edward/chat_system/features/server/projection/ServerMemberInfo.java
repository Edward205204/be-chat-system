package com.edward.chat_system.features.server.projection;

public interface ServerMemberInfo {
    String getUserId();

    String getServerId();

    boolean getIsOwner();
}
