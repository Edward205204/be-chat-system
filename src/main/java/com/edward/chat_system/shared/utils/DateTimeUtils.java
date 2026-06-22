package com.edward.chat_system.shared.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.experimental.UtilityClass;
@UtilityClass
public class DateTimeUtils {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    public LocalDateTime toLocalDateTime(java.util.Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZONE).toLocalDateTime();
    }

    public java.util.Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return java.util.Date.from(localDateTime.atZone(ZONE).toInstant());
    }
}
