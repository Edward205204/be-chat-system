package com.edward.chat_system.features.chat.listener;

import com.edward.chat_system.features.chat.dto.response.ChatMessageResponse;
import com.edward.chat_system.features.chat.event.MessageCreatedEvent;
import com.edward.chat_system.features.chat.websocket.ChannelDestination;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MessageBroadcastListener {
    SimpMessagingTemplate template;

    @EventListener
    public void on(MessageCreatedEvent event) {

        template.convertAndSend(
                ChannelDestination.channel(event.getChannelId()),
                ChatMessageResponse.builder()
                        .id(event.getMessageId())
                        .channelId(event.getChannelId())
                        .senderId(event.getSenderId())
                        .content(event.getContent())
                        .createdAt(event.getCreatedAt())
                        .senderName(event.getSenderName())
                        .build());
    }
}
