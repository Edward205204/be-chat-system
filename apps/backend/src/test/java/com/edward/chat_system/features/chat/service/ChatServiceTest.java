package com.edward.chat_system.features.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.chat.dto.response.ChatMessageResponse;
import com.edward.chat_system.features.chat.entity.Message;
import com.edward.chat_system.features.chat.repository.MessageRepository;
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.features.user.repository.UserRepository;
import com.edward.chat_system.shared.dto.CursorPageResponse;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import com.edward.chat_system.shared.utils.CursorUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock ApplicationEventPublisher publisher;
    @Mock UserRepository userRepository;
    @Mock ChannelRepository channelRepository;
    @Mock MessageRepository messageRepository;
    @Mock CursorUtils cursorUtils;

    @InjectMocks ChatService chatService;

    private Message buildMessage(String id, User sender, Channel channel) {
        return Message.builder()
                .id(id)
                .sender(sender)
                .channel(channel)
                .content("hello")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void send_userExists_savesMessageAndPublishesEvent() {
        User sender = User.builder().id("u1").username("alice").build();
        Channel channel = Channel.builder().id("ch-1").build();
        Message savedMessage = buildMessage("msg-1", sender, channel);
        savedMessage.setContent("hello");
        savedMessage.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById("u1")).thenReturn(Optional.of(sender));
        when(channelRepository.getReferenceById("ch-1")).thenReturn(channel);
        when(messageRepository.save(any())).thenReturn(savedMessage);

        chatService.send("u1", "ch-1", "hello");

        verify(messageRepository).save(any(Message.class));
        verify(messageRepository).flush();
        verify(publisher).publishEvent(any(Object.class));
    }

    @Test
    void send_userNotFound_throwsUserNotFound() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.send("missing", "ch-1", "hello"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void getMessages_noCursor_returnsFirstPageInReverseOrder() {
        User sender = User.builder().id("u1").username("alice").build();
        Channel channel = Channel.builder().id("ch-1").build();

        Message msg1 = buildMessage("msg-1", sender, channel);
        Message msg2 = buildMessage("msg-2", sender, channel);

        when(messageRepository.findFirstPage("ch-1", 3)).thenReturn(List.of(msg1, msg2));

        CursorPageResponse<ChatMessageResponse> result = chatService.getMessages("ch-1", null, 2);

        assertThat(result.getData()).hasSize(2);
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    void getMessages_hasNextPage_setsNextCursor() {
        User sender = User.builder().id("u1").username("alice").build();
        Channel channel = Channel.builder().id("ch-1").build();

        Message msg1 = buildMessage("msg-1", sender, channel);
        Message msg2 = buildMessage("msg-2", sender, channel);

        when(messageRepository.findFirstPage("ch-1", 2)).thenReturn(List.of(msg1, msg2));
        when(cursorUtils.encode(any(), eq("msg-1"))).thenReturn("cursor-xyz");

        CursorPageResponse<ChatMessageResponse> result = chatService.getMessages("ch-1", null, 1);

        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("cursor-xyz");
        assertThat(result.getData()).hasSize(1);
    }

    @Test
    void getMessages_withCursor_usesNextPageQuery() {
        CursorUtils.CursorPayload payload = new CursorUtils.CursorPayload(LocalDateTime.now(), "msg-0");
        User sender = User.builder().id("u1").username("alice").build();
        Channel channel = Channel.builder().id("ch-1").build();
        Message msg = buildMessage("msg-1", sender, channel);

        when(cursorUtils.decode("some-cursor")).thenReturn(payload);
        when(messageRepository.findNextPage("ch-1", payload.createdAt(), payload.id(), 2))
                .thenReturn(List.of(msg));

        CursorPageResponse<ChatMessageResponse> result = chatService.getMessages("ch-1", "some-cursor", 1);

        assertThat(result.getData()).hasSize(1);
        assertThat(result.isHasNext()).isFalse();
    }
}
