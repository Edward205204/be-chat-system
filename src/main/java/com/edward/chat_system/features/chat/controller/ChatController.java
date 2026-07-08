package com.edward.chat_system.features.chat.controller;

import com.edward.chat_system.features.chat.dto.request.SendMessageRequest;
import com.edward.chat_system.features.chat.dto.response.ChatMessageResponse;
import com.edward.chat_system.features.chat.service.ChatService;
import com.edward.chat_system.shared.dto.ApiResponse;
import com.edward.chat_system.shared.dto.CursorPageRequest;
import com.edward.chat_system.shared.dto.CursorPageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {
    ChatService service;

    @MessageMapping("/chat.send")
    public void send(@AuthenticationPrincipal Jwt principal, SendMessageRequest request) {
        service.send(principal.getSubject(), request.getChannelId(), request.getContent());
    }

    @GetMapping("/chat/{channelId}/messages")
    public ApiResponse<CursorPageResponse<ChatMessageResponse>> getMessages(
            @PathVariable String channelId, @Valid CursorPageRequest pageRequest) {
        return ApiResponse.<CursorPageResponse<ChatMessageResponse>>builder()
                .message("Get messages successfully!")
                .result(
                        service.getMessages(
                                channelId, pageRequest.getCursor(), pageRequest.getSize()))
                .build();
    }
}
