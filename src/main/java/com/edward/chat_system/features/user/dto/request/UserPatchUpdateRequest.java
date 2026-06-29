package com.edward.chat_system.features.user.dto.request;

import com.edward.chat_system.shared.aop.annotation.AtLeastOneField;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@AtLeastOneField(message = "No information available for update.")
public class UserPatchUpdateRequest {
    @Length(min = 1, max = 20, message = "Display name must be 1 - 20 chars")
    String displayName;

    @Pattern(
            regexp = "^[a-z0-9_]{3,20}$",
            message =
                    "Username must contain only lowercase letters, numbers, underscores and be 3-20"
                            + " characters long")
    String username;

    @URL(message = "Avatar must be a valid URL")
    String avatar;

    @URL(message = "Banner must be a valid URL")
    String banner;

    LocalDate dateOfBirth;
}
