package com.edward.chat_system.features.channel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;

import com.edward.chat_system.features.channel.dto.request.ChannelPatchUpdateRequest;
import com.edward.chat_system.features.channel.dto.request.CreateChannelRequest;
import com.edward.chat_system.features.channel.dto.response.ChannelResponse;
import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.mapper.ChannelMapper;
import com.edward.chat_system.features.channel.projection.ChannelInfoRaw;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.channel.repository.ChannelUserPermissionRepository;
import com.edward.chat_system.features.server.entity.Server;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.features.server.repository.ServerRepository;
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

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock CursorUtils cursorUtils;
    @Mock ChannelRepository channelRepository;
    @Mock ServerRepository serverRepository;
    @Mock ChannelMapper channelMapper;
    @Mock ChannelUserPermissionRepository channelUserPermissionRepository;
    @Mock ServerMemberRepository serverMemberRepository;

    @InjectMocks ChannelService channelService;

    private ChannelInfoRaw mockChannelInfo(String id, String name, boolean isPrivate) {
        ChannelInfoRaw raw = mock(ChannelInfoRaw.class, withSettings().lenient());
        when(raw.getChannelId()).thenReturn(id);
        when(raw.getName()).thenReturn(name);
        when(raw.getIsPrivate()).thenReturn(isPrivate);
        when(raw.getCreatedAt()).thenReturn(LocalDateTime.now());
        return raw;
    }

    @Test
    void getChannelList_noCursor_returnsFirstPage() {
        ChannelInfoRaw raw = mockChannelInfo("ch-1", "general", false);

        when(channelRepository.findFirstPageVisibleChannels("srv-1", "user-1", 6))
                .thenReturn(List.of(raw));

        CursorPageResponse<ChannelResponse> result =
                channelService.getChannelList("srv-1", "user-1", null, 5);

        assertThat(result.getData()).hasSize(1);
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
    }

    @Test
    void getChannelList_hasNextPage_setsNextCursor() {
        List<ChannelInfoRaw> items = List.of(
                mockChannelInfo("ch-1", "general", false),
                mockChannelInfo("ch-2", "random", false)
        );

        when(channelRepository.findFirstPageVisibleChannels("srv-1", "user-1", 2))
                .thenReturn(items);
        when(cursorUtils.encode(any(), eq("ch-1"))).thenReturn("cursor-abc");

        CursorPageResponse<ChannelResponse> result =
                channelService.getChannelList("srv-1", "user-1", null, 1);

        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("cursor-abc");
        assertThat(result.getData()).hasSize(1);
    }

    @Test
    void createChannel_nameNotDuplicate_savesAndReturnsResponse() {
        CreateChannelRequest request = new CreateChannelRequest();
        request.setName("new-channel");
        request.setPrivate(false);

        Channel savedChannel = Channel.builder().id("ch-1").name("new-channel").build();
        savedChannel.setPrivate(false);

        when(channelRepository.existsByNameAndServer_Id("srv-1", "new-channel")).thenReturn(false);
        when(serverRepository.getReferenceById("srv-1")).thenReturn(Server.builder().id("srv-1").build());
        when(channelRepository.save(any())).thenReturn(savedChannel);

        ChannelResponse result = channelService.createChannel("srv-1", request);

        assertThat(result.getId()).isEqualTo("ch-1");
        assertThat(result.getName()).isEqualTo("new-channel");
    }

    @Test
    void createChannel_nameDuplicate_throwsChannelNameDuplicate() {
        CreateChannelRequest request = new CreateChannelRequest();
        request.setName("existing-channel");

        when(channelRepository.existsByNameAndServer_Id("srv-1", "existing-channel")).thenReturn(true);

        assertThatThrownBy(() -> channelService.createChannel("srv-1", request))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.CHANNEL_NAME_DUPLICATE));
    }

    @Test
    void channelPatchUpdate_channelExists_updatesAndReturnsResponse() {
        Channel channel = Channel.builder().id("ch-1").name("old-name").build();
        channel.setPrivate(false);
        ChannelPatchUpdateRequest request = new ChannelPatchUpdateRequest();

        when(channelRepository.findByIdAndServerId("ch-1", "srv-1")).thenReturn(Optional.of(channel));
        when(channelRepository.save(channel)).thenReturn(channel);

        ChannelResponse result = channelService.channelPatchUpdate("srv-1", "ch-1", request);

        assertThat(result.getId()).isEqualTo("ch-1");
        verify(channelMapper).updateChannelFromDto(request, channel);
    }

    @Test
    void channelPatchUpdate_channelNotExist_throwsChannelNotExist() {
        when(channelRepository.findByIdAndServerId("ch-99", "srv-1")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () -> channelService.channelPatchUpdate("srv-1", "ch-99", new ChannelPatchUpdateRequest()))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.CHANNEL_IS_NOT_EXIST));
    }

    @Test
    void deleteChannel_delegatesToRepository() {
        channelService.deleteChannel("srv-1", "ch-1");

        verify(channelRepository).deleteByIdAndServer_Id("ch-1", "srv-1");
    }
}
