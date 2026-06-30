package com.edward.chat_system.features.server.service;

import com.edward.chat_system.features.channel.constant.ChannelConstants;
import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.permission.service.RoleService;
import com.edward.chat_system.features.server.dto.request.CreateServerRequest;
import com.edward.chat_system.features.server.dto.request.ServerPatchUpdateRequest;
import com.edward.chat_system.features.server.dto.response.ServerResponse;
import com.edward.chat_system.features.server.dto.response.ServerUpdateResponse;
import com.edward.chat_system.features.server.entity.Server;
import com.edward.chat_system.features.server.entity.ServerMember;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.features.server.mapper.ServerMapper;
import com.edward.chat_system.features.server.projection.ServerProjection;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.features.server.repository.ServerRepository;
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.features.user.repository.UserRepository;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresOwner;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerPermission;
import com.edward.chat_system.infrastructure.aop.annotation.ServerId;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServerService {
    ServerRepository serverRepository;
    UserRepository userRepository;
    ServerMemberRepository serverMemberRepository;
    RoleService roleService;
    ChannelRepository channelRepository;
    ServerMapper serverMapper;

    void checkServerNameDuplicate(String userId, String serverName) {
        if (serverRepository.existsByUserIdAndName(userId, serverName))
            throw new AppException(ErrorCode.SERVER_NAME_DUPLICATE);
    }

    public List<ServerResponse> getMyServers(String userId) {
        return serverRepository.findAllServerUserJoined(userId).stream()
                .map(
                        server ->
                                ServerResponse.builder()
                                        .id(server.getServerId())
                                        .name(server.getName())
                                        .avatar(server.getAvatar())
                                        .banner(server.getBanner())
                                        .ownerId(server.getOwnerId())
                                        .isOwner(server.getOwnerId().equals(userId))
                                        .joinedAt(server.getJoinedAt())
                                        .build())
                .toList();
    }

    public ServerResponse getServerById(@ServerId String serverId, String currentUserId) {
        ServerProjection projection =
                serverRepository
                        .findServerUserJoinedByServerIdAndUserId(serverId, currentUserId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_A_MEMBER));

        return ServerResponse.builder()
                .id(projection.getServerId())
                .name(projection.getName())
                .avatar(projection.getAvatar())
                .banner(projection.getBanner())
                .ownerId(projection.getOwnerId())
                .isOwner(projection.getOwnerId().equals(currentUserId))
                .joinedAt(projection.getJoinedAt())
                .build();
    }

    @Transactional
    public ServerResponse createServer(String userId, CreateServerRequest request) {
        checkServerNameDuplicate(userId, request.getName());
        User user = userRepository.getReferenceById(userId);
        Server server =
                Server.builder()
                        .user(user)
                        .name(request.getName())
                        .avatar(request.getAvatar())
                        .build();
        server = serverRepository.save(server);
        ServerMember serverMember = ServerMember.builder().server(server).user(user).build();
        serverMemberRepository.save(serverMember);
        roleService.createDefaultEveryoneRoleWithDefaultPermission(server.getId());
        Channel channel =
                Channel.builder()
                        .server(server)
                        .name(ChannelConstants.DEFAULT_CHANNEL_NAME)
                        .build();
        channelRepository.save(channel);

        return ServerResponse.builder()
                .id(server.getId())
                .name(server.getName())
                .avatar(server.getAvatar())
                .banner(server.getBanner())
                .ownerId(userId)
                .isOwner(true)
                .joinedAt(serverMember.getJoinedAt())
                .build();
    }

    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_SERVER)
    public ServerUpdateResponse serverUpdateResponse(
            @ServerId String serverId, String userId, ServerPatchUpdateRequest request) {
        if (request.getName() != null) checkServerNameDuplicate(userId, request.getName());

        Server server =
                serverRepository
                        .findById(serverId)
                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_EXIST));

        serverMapper.updateServerFromDto(request, server);

        serverRepository.save(server);

        return serverMapper.toServerUpdateResponse(server);
    }

    @RequiresOwner
    public void deleteServer(@ServerId String serverId) {
        serverRepository.deleteById(serverId);
    }
}
