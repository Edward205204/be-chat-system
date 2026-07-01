package com.edward.chat_system.features.server.dto.response;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServerMemberResponse {
    long total;
    long page;
    long size;
    List<ServerMemberItemResponse> items;
}
