package com.edward.chat_system.features.permission.dto.response;

import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleWithPermissionResponse {
    String roleId;
    Set<String> permissions;
}
