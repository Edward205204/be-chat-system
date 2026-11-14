package com.edward.chat_system.features.permission.dto.request;

import com.edward.chat_system.shared.aop.annotation.AtLeastOneField;
import com.edward.chat_system.shared.aop.annotation.ValidServerPermission;
import java.util.Set;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AtLeastOneField
public class ServerPermissionPutUpdateRequest {
    @NotEmpty(message = "Permission is required")
    Set<@ValidServerPermission String> permission;
}
