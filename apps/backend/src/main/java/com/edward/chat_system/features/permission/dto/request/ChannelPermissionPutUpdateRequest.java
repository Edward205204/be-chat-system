package com.edward.chat_system.features.permission.dto.request;

import com.edward.chat_system.shared.aop.annotation.AtLeastOneField;
import com.edward.chat_system.shared.aop.annotation.ValidChannelPermission;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AtLeastOneField
public class ChannelPermissionPutUpdateRequest {
    @NotEmpty(message = "Permission is required")
    Set<@ValidChannelPermission String> permission;
}
