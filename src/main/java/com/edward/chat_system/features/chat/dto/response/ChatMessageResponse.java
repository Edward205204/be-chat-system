package com.edward.chat_system.features.chat.dto.response;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
    String id;
    String channelId;
    String senderId;
    String senderName;
    String content;
    LocalDateTime createdAt;
}
