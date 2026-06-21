package com.edward.chat_system.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @Email(message = "Email is invalid format")
    @NotBlank(message = "Email is required")
    String email;

    @Pattern(
            regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()\\-+=_])(?=\\S+$).{8,20}$",
            message =
                    "Password must be 8-20 chars, include uppercase, lowercase, digit, and special"
                            + " character")
    @NotBlank(message = "Password is required")
    String password;
}
