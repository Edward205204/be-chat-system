package com.edward.chat_system.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ResetPasswordRequest {
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

    @NotBlank(message = "OTP is required")
    @Length(min = 6, max = 6, message = "OTP must be 6 digits")
    String otp;
}
