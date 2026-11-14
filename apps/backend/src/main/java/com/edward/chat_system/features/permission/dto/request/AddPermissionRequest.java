package com.edward.chat_system.features.permission.dto.request;

import com.edward.chat_system.shared.aop.annotation.ValidServerPermission;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AddPermissionRequest {
    @NotBlank(message = "Permission is required")
    @ValidServerPermission
    String permission;
}
