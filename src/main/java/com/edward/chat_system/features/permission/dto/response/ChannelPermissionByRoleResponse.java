package com.edward.chat_system.features.permission.dto.response;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChannelPermissionByRoleResponse {
    String roleId;
    String roleName;

    Set<ChannelPermissionKeyEnum> permissions;
}
