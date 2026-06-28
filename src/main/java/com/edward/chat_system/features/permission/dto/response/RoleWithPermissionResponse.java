package com.edward.chat_system.features.permission.dto.response;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleWithPermissionResponse {
    String roleId;
    List<String> permissions;
}
