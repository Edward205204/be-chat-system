package com.edward.chat_system.features.server.dto.response;

import com.edward.chat_system.shared.aop.annotation.AtLeastOneField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@AtLeastOneField
public class ServerPatchUpdateResponse {
    String id;
    String name;
    String avatar;
    String banner;
}
