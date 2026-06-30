package com.edward.chat_system.features.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class CreateServerRequest {
    @NotBlank(message = "Name is required")
    @Length(min = 1, max = 100, message = "Name must be 1 - 100 chars")
    String name;

    @URL(message = "Avatar must be a valid URL")
    String avatar;
}
