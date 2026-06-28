package com.edward.chat_system.features.permission.dto.request;

import com.edward.chat_system.shared.aop.annotation.AtLeastOneField;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AtLeastOneField
public class RolePatchUpdateRequest {
    @Size(min = 3, max = 20, message = "Name must be between 3 and 20 characters")
    private String name;

    @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "Color must be a valid hex color code, exp: #5865F2")
    private String color;
}
