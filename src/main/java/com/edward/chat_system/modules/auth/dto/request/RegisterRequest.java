package com.edward.chat_system.modules.auth.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
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

    @Pattern(
            regexp = "^[a-z0-9_]{3,20}$",
            message =
                    "Username must contain only lowercase letters, numbers, underscores and be 3-20"
                            + " characters long")
    @NotBlank(message = "Username is required")
    String username;

    @NotBlank(message = "Display name is required")
    @Length(min = 1, max = 20, message = "Display name must be 1 - 20 chars")
    String displayName;

    @Past(message = "Date of birth must be in the past")
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;
}
