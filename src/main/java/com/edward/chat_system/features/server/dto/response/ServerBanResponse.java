package com.edward.chat_system.features.server.dto.response;

import com.edward.chat_system.features.user.dto.response.UserBanInfoResponse;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServerBanResponse {
    String id;
    UserBanInfoResponse user;
    UserBanInfoResponse bannedBy;
    String reason;
    LocalDateTime createdAt;
}
