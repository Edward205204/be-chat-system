package com.edward.chat_system.features.chat.controller;

import com.edward.chat_system.features.chat.dto.request.SendMessageRequest;
import com.edward.chat_system.features.chat.dto.response.ChatMessageResponse;
import com.edward.chat_system.features.chat.service.ChatService;
import com.edward.chat_system.shared.dto.ApiResponse;
import com.edward.chat_system.shared.dto.CursorPageRequest;
import com.edward.chat_system.shared.dto.CursorPageResponse;
import com.edward.chat_system.shared.exception.AppException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {
    ChatService service;

    @MessageMapping("/chat.send")
    public void send(Principal principal, SendMessageRequest request) {
        service.send(principal.getName(), request.getChannelId(), request.getContent());
    }

    // Errors from /app/chat.send are delivered to the sender on /user/queue/errors
    @MessageExceptionHandler(AppException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ApiResponse<Void> handleSendError(AppException exception) {
        log.warn("chat.send failed: {}", exception.getMessage());
        return ApiResponse.<Void>builder()
                .status(exception.getErrorCode().getCode())
                .message(exception.getErrorCode().getMessage())
                .build();
    }

    @ResponseBody
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
