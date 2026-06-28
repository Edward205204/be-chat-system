package com.edward.chat_system.features.permission.dto.request;

import com.edward.chat_system.features.permission.projection.PermissionNameProjection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetPermissionRequest {
    String roleId;
    List<PermissionNameProjection> permissions;
}
