package com.edward.chat_system.features.chat.websocket;

public class ChannelDestination {
    private ChannelDestination() {}

    public static String channel(String channelId) {
        return "/topic/channel/" + channelId;
    }
}
