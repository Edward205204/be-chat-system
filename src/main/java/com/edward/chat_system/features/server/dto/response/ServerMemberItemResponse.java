package com.edward.chat_system.features.server.dto.response;

import com.edward.chat_system.features.permission.dto.response.RoleResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServerMemberItemResponse {
    String memberId;
    String userId;
    String displayName;
    String username;
    String avatar;
    boolean isOwner;
    boolean isMuted;
    LocalDateTime joinedAt;
    List<RoleResponse> roles;
}
