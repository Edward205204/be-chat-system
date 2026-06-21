package com.edward.chat_system.shared.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeUtils {

    public LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }

        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
