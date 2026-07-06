package com.edward.chat_system.features.chat.controller;

import com.edward.chat_system.features.chat.dto.request.SendMessageRequest;
import com.edward.chat_system.features.chat.service.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {
    ChatService service;

    @MessageMapping("/chat.send")
    public void send(@AuthenticationPrincipal Jwt principal, SendMessageRequest request) {
        service.send(principal.getSubject(),
                principal.getClaim("username"),
                request.getChannelId(),
                request.getContent());
    }
}
