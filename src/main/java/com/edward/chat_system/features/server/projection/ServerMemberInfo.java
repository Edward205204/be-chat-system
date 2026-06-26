package com.edward.chat_system.features.server.projection;

public interface ServerMemberInfo {
    String getMemberId();
    String getServerId();
    Boolean getIsOwner();
}
