package com.edward.chat_system.features.chat.service;

import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.chat.dto.request.SendMessageRequest;
import com.edward.chat_system.features.chat.entity.Message;
import com.edward.chat_system.features.chat.event.MessageCreatedEvent;
import com.edward.chat_system.features.chat.repository.MessageRepository;
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.features.user.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    MessageRepository repository;
    ApplicationEventPublisher publisher;
    UserRepository userRepository;
    private final ChannelRepository channelRepository;

    public void send(String senderId, String senderName, SendMessageRequest request) {
        User sender = userRepository.getReferenceById(senderId);
        Channel channel = channelRepository.getReferenceById(request.getChannelId());
        Message message =
                repository.save(
                        Message.builder()
                                .content(request.getContent())
                                .sender(sender)
                                .channel(channel)
                                .build());
        repository.flush();
        publisher.publishEvent(
                MessageCreatedEvent.builder()
                        .messageId(message.getId())
                        .channelId(request.getChannelId())
                        .senderId(senderId)
                        .senderName(senderName)
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt())
                        .build());
    }
}
