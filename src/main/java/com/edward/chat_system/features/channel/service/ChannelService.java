package com.edward.chat_system.features.channel.service;

import com.edward.chat_system.features.channel.dto.request.AddToChannelRequest;
import com.edward.chat_system.features.channel.dto.request.ChannelPatchUpdateRequest;
import com.edward.chat_system.features.channel.dto.request.CreateChannelRequest;
import com.edward.chat_system.features.channel.dto.response.ChannelResponse;
import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.features.channel.mapper.ChannelMapper;
import com.edward.chat_system.features.channel.projection.ChannelInfoRaw;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.channel.repository.ChannelUserPermissionRepository;
import com.edward.chat_system.features.permission.entity.ChannelUserPermission;
import com.edward.chat_system.features.server.entity.Server;
import com.edward.chat_system.features.server.entity.ServerMember;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.features.server.repository.ServerRepository;
import com.edward.chat_system.infrastructure.aop.annotation.ChannelId;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresChannelPermission;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerPermission;
import com.edward.chat_system.infrastructure.aop.annotation.ServerId;
import com.edward.chat_system.shared.dto.CursorPageResponse;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import com.edward.chat_system.shared.utils.CursorUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ChannelService {
    CursorUtils cursorUtils;
    ChannelRepository channelRepository;
    ServerRepository serverRepository;
    ChannelMapper channelMapper;
    ChannelUserPermissionRepository channelUserPermissionRepository;
    private final ServerMemberRepository serverMemberRepository;

    public CursorPageResponse<ChannelResponse> getChannelList(
            String serverId, String userId, String cursor, int size) {
        int fetchSize = size + 1;

        List<ChannelInfoRaw> channelInfoRaw;
        if (cursor == null || cursor.isBlank()) {
            channelInfoRaw =
                    channelRepository.findFirstPageVisibleChannels(serverId, userId, fetchSize);
        } else {
            CursorUtils.CursorPayload payload = cursorUtils.decode(cursor);
            channelInfoRaw =
                    channelRepository.findVisibleChannelsByServerWithCursor(
                            serverId, userId, cursor, payload.createdAt(), payload.id(), fetchSize);
        }

        boolean hasNext = channelInfoRaw.size() > size;
        List<ChannelInfoRaw> pageItems = hasNext ? channelInfoRaw.subList(0, size) : channelInfoRaw;

        String nextCursor = null;
        if (hasNext) {
            ChannelInfoRaw last = pageItems.getLast();
            nextCursor = cursorUtils.encode(last.getCreatedAt(), last.getChannelId());
        }

        return CursorPageResponse.<ChannelResponse>builder()
                .data(
                        pageItems.stream()
                                .map(
                                        item ->
                                                ChannelResponse.builder()
                                                        .id(item.getChannelId())
                                                        .name(item.getName())
                                                        .isPrivate(item.getIsPrivate())
                                                        .createdAt(item.getCreatedAt())
                                                        .build())
                                .toList())
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_CHANNELS)
    public ChannelResponse createChannel(@ServerId String serverId, CreateChannelRequest request) {
        if (channelRepository.existsByNameAndServer_Id(serverId, request.getName()))
            throw new AppException(ErrorCode.CHANNEL_NAME_DUPLICATE);

        Server server = serverRepository.getReferenceById(serverId);
        Channel channel =
                channelRepository.save(
                        Channel.builder()
                                .server(server)
                                .name(request.getName())
                                .isPrivate(request.isPrivate())
                                .build());

        return ChannelResponse.builder()
                .id(channel.getId())
                .name(channel.getName())
                .isPrivate(channel.isPrivate())
                .createdAt(channel.getCreatedAt())
                .build();
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.MANAGE_CHANNEL)
    public ChannelResponse channelPatchUpdate(
            @ServerId String serverId,
            @ChannelId String channelId,
            ChannelPatchUpdateRequest request) {
        Channel channel =
                channelRepository
                        .findByIdAndServerId(channelId, serverId)
                        .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_IS_NOT_EXIST));

        channelMapper.updateChannelFromDto(request, channel);
        channel = channelRepository.save(channel);

        return ChannelResponse.builder()
                .id(channel.getId())
                .name(channel.getName())
                .isPrivate(channel.isPrivate())
                .createdAt(channel.getCreatedAt())
                .build();
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.MANAGE_CHANNEL)
    void deleteChannel(@ServerId String serverId, @ChannelId String channelId) {
        //  cascade delete
        channelRepository.deleteByIdAndServer_Id(channelId, serverId);
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.INVITE_MEMBERS)
    void addMemberToPrivateChannel(
            @ServerId String serverId, @ChannelId String channelId, AddToChannelRequest request) {
        final ChannelPermissionKeyEnum permissionKeyEnum = ChannelPermissionKeyEnum.VIEW_CHANNEL;
        if (serverMemberRepository.existsByIdAndServerId(request.getMemberId(), serverId))
            throw new AppException(ErrorCode.NOT_A_MEMBER);

        if (channelUserPermissionRepository.existsByUniqueConstraint(
                channelId, request.getMemberId(), permissionKeyEnum))
            throw new AppException(ErrorCode.PERMISSION_DUPLICATE_FOR_THIS_USER);

        Channel channel = channelRepository.getReferenceById(channelId);
        ServerMember serverMember = serverMemberRepository.getReferenceById(request.getMemberId());
        ChannelUserPermission channelUserPermission =
                ChannelUserPermission.builder()
                        .channel(channel)
                        .serverMember(serverMember)
                        .permission(permissionKeyEnum)
                        .build();
        channelUserPermissionRepository.save(channelUserPermission);
    }

//    AFTER: Add role to private channel
}
