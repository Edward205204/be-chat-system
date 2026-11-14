package com.edward.chat_system.features.chat.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MessageCreatedEvent {
    String messageId;
    String channelId;
    String senderId;
    String senderAvatar;
    String senderName;
    String content;
    LocalDateTime createdAt;
}
