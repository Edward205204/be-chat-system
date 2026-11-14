package com.edward.chat_system.features.server.dto.response;

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
public class ServerResponse {
    String id;
    String name;
    String avatar;
    String banner;
    String ownerId;
    boolean isOwner;
    LocalDateTime joinedAt;
}
