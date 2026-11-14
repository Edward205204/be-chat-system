package com.edward.chat_system.features.channel.dto.request;

import com.edward.chat_system.shared.aop.annotation.AtLeastOneField;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@AtLeastOneField
public class ChannelPatchUpdateRequest {
    @Length(min = 1, max = 100, message = "Name must be 1 - 100 chars")
    @NotBlank(message = "Name is required")
    String name;
}
