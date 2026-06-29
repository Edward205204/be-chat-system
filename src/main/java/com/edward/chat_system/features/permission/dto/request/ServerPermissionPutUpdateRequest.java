package com.edward.chat_system.features.permission.dto.request;

import com.edward.chat_system.shared.aop.annotation.AtLeastOneField;
import com.edward.chat_system.shared.aop.annotation.ValidServerPermission;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AtLeastOneField
public class ServerPermissionPutUpdateRequest {
    @NotBlank(message = "Permission is required")
    Set<@ValidServerPermission String> permission;
}
