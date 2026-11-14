package com.edward.chat_system.features.permission.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateRoleRequest {
    @NotBlank(message = "Name of role is required")
    @Size(min = 3, max = 20, message = "Name must be between 3 and 20 characters")
    String name;

    @NotBlank(message = "Color of role is required")
    @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "Color must be a valid hex color code, exp: #5865F2")
    String color;
}
