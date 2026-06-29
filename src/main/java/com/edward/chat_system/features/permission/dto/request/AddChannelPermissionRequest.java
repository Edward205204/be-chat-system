package com.edward.chat_system.features.permission.dto.request;

import com.edward.chat_system.shared.aop.annotation.ValidChannelPermission;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AddChannelPermissionRequest {
    @NotBlank(message = "Channel id is required")
    String roleId;

    @NotBlank(message = "Permission is required")
    @ValidChannelPermission
    String permission;
}
