package com.edward.chat_system.features.server.dto.request;

import com.edward.chat_system.shared.aop.annotation.AtLeastOneField;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@AtLeastOneField(message = "No information available for update.")
public class ServerPatchUpdateRequest {
    @NotBlank(message = "Name is required")
    @Length(min = 1, max = 100, message = "Name must be 1 - 100 chars")
    String name;

    @URL(message = "Avatar must be a valid URL")
    String avatar;

    @URL(message = "Banner must be a valid URL")
    String banner;
}
