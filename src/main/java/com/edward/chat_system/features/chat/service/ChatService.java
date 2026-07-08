package com.edward.chat_system.features.chat.service;

import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.chat.dto.response.ChatMessageResponse;
import com.edward.chat_system.features.chat.entity.Message;
import com.edward.chat_system.features.chat.event.MessageCreatedEvent;
import com.edward.chat_system.features.chat.repository.MessageRepository;
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.features.user.repository.UserRepository;
import com.edward.chat_system.infrastructure.aop.annotation.ChannelId;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresChannelPermission;
import com.edward.chat_system.shared.dto.CursorPageResponse;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import com.edward.chat_system.shared.utils.CursorUtils;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {
    ApplicationEventPublisher publisher;
    UserRepository userRepository;
    ChannelRepository channelRepository;
    MessageRepository messageRepository;
    CursorUtils cursorUtils;

    @RequiresChannelPermission(ChannelPermissionKeyEnum.VIEW_CHANNEL)
    public void send(String senderId, @ChannelId String channelId, String content) {
        User sender =
                userRepository
                        .findById(senderId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Channel channel = channelRepository.getReferenceById(channelId);
        Message message =
                messageRepository.save(
                        Message.builder().content(content).sender(sender).channel(channel).build());
        messageRepository.flush();
        publisher.publishEvent(
                MessageCreatedEvent.builder()
                        .messageId(message.getId())
                        .channelId(channelId)
                        .senderId(senderId)
                        .senderAvatar(sender.getAvatar())
                        .senderName(sender.getUsername())
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt())
                        .build());
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.VIEW_CHANNEL)
    public CursorPageResponse<ChatMessageResponse> getMessages(
            @ChannelId String channelId, String cursor, int size) {
        int fetchSize = size + 1;

        List<Message> messages;
        if (cursor == null || cursor.isBlank()) {
            messages = messageRepository.findFirstPage(channelId, fetchSize);
        } else {
            CursorUtils.CursorPayload payload = cursorUtils.decode(cursor);
            messages =
                    messageRepository.findNextPage(
                            channelId, payload.createdAt(), payload.id(), fetchSize);
        }

        boolean hasNext = messages.size() > size;
        List<Message> pageItems = hasNext ? messages.subList(0, size) : messages;

        List<ChatMessageResponse> messageResponse =
                new ArrayList<>(pageItems)
                        .reversed().stream()
                                .map(
                                        item ->
                                                ChatMessageResponse.builder()
                                                        .id(item.getId())
                                                        .channelId(item.getChannel().getId())
                                                        .senderId(item.getSender().getId())
                                                        .senderName(item.getSender().getUsername())
                                                        .senderAvatar(item.getSender().getAvatar())
                                                        .content(item.getContent())
                                                        .createdAt(item.getCreatedAt())
                                                        .build())
                                .toList();

        String nextCursor = null;
        if (hasNext) {
            Message last = pageItems.getLast();
            nextCursor = cursorUtils.encode(last.getCreatedAt(), last.getId());
        }

        return CursorPageResponse.<ChatMessageResponse>builder()
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .data(messageResponse)
                .build();
    }
}
