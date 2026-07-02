package com.edward.chat_system.shared.dto;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CursorPageResponse<T> {
    List<T> data;
    String nextCursor;
    boolean hasNext;
}
