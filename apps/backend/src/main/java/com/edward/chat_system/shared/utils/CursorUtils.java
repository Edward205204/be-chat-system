package com.edward.chat_system.shared.utils;

import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CursorUtils {

    private static final ObjectMapper MAPPER =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public String encode(LocalDateTime createdAt, String id) {
        try {
            String json =
                    MAPPER.writeValueAsString(Map.of("createdAt", createdAt.toString(), "id", id));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED);
        }
    }

    public CursorPayload decode(String cursor) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cursor);
            JsonNode node = MAPPER.readTree(bytes);
            return new CursorPayload(
                    LocalDateTime.parse(node.get("createdAt").asText()), node.get("id").asText());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_CURSOR);
        }
    }

    public record CursorPayload(LocalDateTime createdAt, String id) {}
}
