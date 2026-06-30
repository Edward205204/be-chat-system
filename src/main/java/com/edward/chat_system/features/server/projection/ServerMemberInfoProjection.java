package com.edward.chat_system.features.server.projection;

public interface ServerMemberInfoProjection {
    String getUserId();

    String getServerId();

    boolean getIsOwner();
}
