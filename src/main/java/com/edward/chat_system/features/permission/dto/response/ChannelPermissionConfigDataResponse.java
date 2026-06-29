package com.edward.chat_system.features.permission.dto.response;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChannelPermissionConfigDataResponse {
    String channelId;
    boolean isPrivate;

    List<ChannelPermissionByRoleResponse> rolePermissions;
    List<ChannelPermissionByUserResponse> userPermissions;
}
