package com.edward.chat_system.shared.utils;

import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Value;

@UtilityClass
public class InviteUrlBuilder {
    @Value("${app.frontend-url}")
    private String frontendUrl;

    public String build(String token) {
        return frontendUrl + "/invite/" + token;
    }
}
