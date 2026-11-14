package com.edward.chat_system.shared.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CursorPageRequest {

    String cursor;

    @Min(value = 1, message = "Size must be greater than 0")
    @Max(value = 100, message = "Size must be less than 100")
    int size = 20;
}
