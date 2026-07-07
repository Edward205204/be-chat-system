package com.edward.chat_system.features.server.service;

import com.edward.chat_system.features.channel.constant.ChannelConstants;
import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.file.FileService;
import com.edward.chat_system.features.permission.dto.response.RoleResponse;
import com.edward.chat_system.features.permission.projection.MemberRoleRow;
import com.edward.chat_system.features.permission.repository.RoleMemberRepository;
import com.edward.chat_system.features.permission.service.RoleService;
import com.edward.chat_system.features.server.dto.request.BanMemberRequest;
import com.edward.chat_system.features.server.dto.request.CreateServerRequest;
import com.edward.chat_system.features.server.dto.request.MuteMemberRequest;
import com.edward.chat_system.features.server.dto.request.ServerPatchUpdateRequest;
import com.edward.chat_system.features.server.dto.response.*;
import com.edward.chat_system.features.server.entity.InviteLink;
import com.edward.chat_system.features.server.entity.Server;
import com.edward.chat_system.features.server.entity.ServerBan;
import com.edward.chat_system.features.server.entity.ServerMember;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.features.server.mapper.InviteLinkMapper;
import com.edward.chat_system.features.server.mapper.ServerMapper;
import com.edward.chat_system.features.server.projection.MemberProjection;
import com.edward.chat_system.features.server.projection.ServerProjection;
import com.edward.chat_system.features.server.repository.InviteLinkRepository;
import com.edward.chat_system.features.server.repository.ServerBanRepository;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.features.server.repository.ServerRepository;
import com.edward.chat_system.features.user.dto.response.UserBasicInfoResponse;
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.features.user.mapper.UserMapper;
import com.edward.chat_system.features.user.repository.UserRepository;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresOwner;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerMember;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerPermission;
import com.edward.chat_system.infrastructure.aop.annotation.ServerId;
import com.edward.chat_system.shared.dto.CursorPageResponse;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import com.edward.chat_system.shared.utils.CursorUtils;
import com.edward.chat_system.shared.utils.InviteUrlBuilder;
import com.edward.chat_system.shared.utils.SecureTokenGenerator;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    RoleMemberRepository roleMemberRepository;
    ServerBanRepository serverBanRepository;
    UserMapper userMapper;
    InviteLinkRepository inviteLinkRepository;
    CursorUtils cursorUtils;
    InviteLinkMapper inviteLinkMapper;
    FileService fileService;

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

        fileService.claimFile(request.getAvatar());

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
    public ServerPatchUpdateResponse serverUpdateResponse(
            @ServerId String serverId, String userId, ServerPatchUpdateRequest request) {
        if (request.getName() != null) checkServerNameDuplicate(userId, request.getName());

        Server server =
                serverRepository
                        .findById(serverId)
                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_EXIST));

        serverMapper.updateServerFromDto(request, server);

        serverRepository.save(server);

        if (request.getAvatar() != null) fileService.claimFile(request.getAvatar());
        if (request.getBanner() != null) fileService.claimFile(request.getBanner());

        return serverMapper.toServerUpdateResponse(server);
    }

    @Transactional
    @RequiresOwner
    public void deleteServer(@ServerId String serverId) {
        serverRepository.deleteById(serverId);
    }

    // AFTER Page Offset
    @Transactional
    @RequiresServerMember
    public ServerMemberResponse getServerMember(@ServerId String serverId, Pageable pageable) {
        //        @PathVariable String serverId,
        //        @PageableDefault(size = 50) Pageable pageable
        Page<MemberProjection> memberPage =
                serverMemberRepository.findMembersByServerId(serverId, pageable);

        List<String> memberIds =
                memberPage.getContent().stream().map(MemberProjection::getMemberId).toList();

        Map<String, List<RoleResponse>> rolesByMember =
                memberIds.isEmpty()
                        ? Map.of()
                        : roleMemberRepository.findRolesByMemberIds(memberIds).stream()
                                .collect(
                                        Collectors.groupingBy(
                                                MemberRoleRow::getMemberId,
                                                Collectors.mapping(
                                                        row ->
                                                                RoleResponse.builder()
                                                                        .id(row.getRoleId())
                                                                        .name(row.getRoleName())
                                                                        .color(row.getRoleColor())
                                                                        .build(),
                                                        Collectors.toList())));

        List<ServerMemberItemResponse> items =
                memberPage.getContent().stream()
                        .map(
                                m ->
                                        ServerMemberItemResponse.builder()
                                                .memberId(m.getMemberId())
                                                .userId(m.getUserId())
                                                .displayName(m.getDisplayName())
                                                .username(m.getUsername())
                                                .avatar(m.getAvatar())
                                                .isOwner(m.getUserId().equals(m.getOwnerId()))
                                                .isMuted(m.getIsMuted())
                                                .joinedAt(m.getJoinedAt())
                                                .roles(
                                                        rolesByMember.getOrDefault(
                                                                m.getMemberId(), List.of()))
                                                .build())
                        .toList();

        return ServerMemberResponse.builder()
                .total(memberPage.getTotalElements())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .items(items)
                .build();
    }

    @Transactional
    @RequiresServerPermission(ServerPermissionKeyEnum.KICK_MEMBER)
    public void kickMember(@ServerId String serverId, String memberId) {
        serverMemberRepository.deleteByIdAndServerId(memberId, serverId);
    }

    @Transactional
    @RequiresOwner
    public void transferOwner(@ServerId String serverId, String newOwnerMemberId) {
        ServerMember newOwnerMemberInfo =
                serverMemberRepository
                        .findByIdAndServerId(newOwnerMemberId, serverId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_A_MEMBER));
        String newOwnerUserId = newOwnerMemberInfo.getUser().getId();
        serverRepository.updateOwner(serverId, newOwnerUserId);
    }

    @Transactional
    @RequiresServerMember
    public void leaveServer(@ServerId String serverId, String userId) {
        serverMemberRepository.deleteByServerIdAndUserId(serverId, userId);
    }

    @Transactional
    @RequiresServerPermission(ServerPermissionKeyEnum.MUTE_MEMBER)
    public void muteMember(@ServerId String serverId, String memberId, MuteMemberRequest request) {
        serverMemberRepository.muteOrUnmuteServerMember(serverId, memberId, request.isMute());
    }

    @Transactional
    @RequiresServerPermission(ServerPermissionKeyEnum.BAN_MEMBER)
    public void banMember(
            @ServerId String serverId,
            String bannedUserId,
            String bannerUserId,
            BanMemberRequest request) {
        if (!serverMemberRepository.existsByServerIdAndUserId(serverId, bannedUserId))
            throw new AppException(ErrorCode.NOT_A_MEMBER);

        Server server = serverRepository.getReferenceById(serverId);
        User bannedUser = userRepository.getReferenceById(bannedUserId);
        User bannerUser = userRepository.getReferenceById(bannerUserId);
        serverMemberRepository.deleteByServerIdAndUserId(serverId, bannedUserId);
        serverBanRepository.save(
                ServerBan.builder()
                        .server(server)
                        .user(bannedUser)
                        .bannedBy(bannerUser)
                        .reason(request.getReason())
                        .build());
    }

    @Transactional
    @RequiresServerPermission(ServerPermissionKeyEnum.BAN_MEMBER)
    public void unbanMember(@ServerId String serverId, String bannedUserId) {
        serverBanRepository.deleteByServerIdAndUserId(serverId, bannedUserId);
    }

    // AFTER Page Offset
    @RequiresServerPermission(ServerPermissionKeyEnum.BAN_MEMBER)
    public List<ServerBanResponse> banList(@ServerId String serverId, Pageable pageable) {
        return serverBanRepository.findByServerId(serverId, pageable).getContent().stream()
                .map(
                        item ->
                                ServerBanResponse.builder()
                                        .id(item.getId())
                                        .user(userMapper.toUserBanInfoResponse(item.getUser()))
                                        .bannedBy(
                                                userMapper.toUserBanInfoResponse(
                                                        item.getBannedBy()))
                                        .reason(item.getReason())
                                        .createdAt(item.getCreatedAt())
                                        .build())
                .toList();
    }

    @RequiresServerPermission(ServerPermissionKeyEnum.CREATE_INVITE)
    public InviteLinkResponse createInviteLink(@ServerId String serverId, String userId) {
        String token = SecureTokenGenerator.generate();
        Server server = serverRepository.getReferenceById(serverId);
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        InviteLink inviteLink = InviteLink.builder().server(server).user(user).token(token).build();
        inviteLink = inviteLinkRepository.save(inviteLink);
        UserBasicInfoResponse userInfo =
                UserBasicInfoResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .displayName(user.getDisplayName())
                        .avatar(user.getAvatar())
                        .build();
        String inviteUrl = InviteUrlBuilder.build(token);
        return InviteLinkResponse.builder()
                .id(inviteLink.getId())
                .token(token)
                .inviteUrl(inviteUrl)
                .createdBy(userInfo)
                .useCount(inviteLink.getUseCount())
                .expiresAt(inviteLink.getExpiresAt())
                .createdAt(inviteLink.getCreatedAt())
                .isRevoked(inviteLink.isRevoked())
                .build();
    }

    private List<InviteLink> fetchWithCursor(String serverId, String cursor, int fetchSize) {
        var payload = cursorUtils.decode(cursor);
        return inviteLinkRepository.findNextPage(
                serverId, payload.createdAt(), payload.id(), fetchSize);
    }

    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_SERVER)
    public CursorPageResponse<InviteLinkResponse> getAllInviteLink(
            @ServerId String serverId, String cursor, int size) {

        int fetchSize = size + 1;

        List<InviteLink> results =
                (cursor == null || cursor.isBlank())
                        ? inviteLinkRepository.findFirstPage(serverId, fetchSize)
                        : fetchWithCursor(serverId, cursor, fetchSize);

        boolean hasNext = results.size() > size;
        List<InviteLink> pageItems = hasNext ? results.subList(0, size) : results;

        String nextCursor = null;
        if (hasNext) {
            InviteLink last = pageItems.getLast();
            nextCursor = cursorUtils.encode(last.getCreatedAt(), last.getId());
        }

        return CursorPageResponse.<InviteLinkResponse>builder()
                .data(pageItems.stream().map(inviteLinkMapper::toResponse).toList())
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_SERVER)
    public void revokeInviteLink(@ServerId String serverId, String inviteLinkId) {
        inviteLinkRepository.deleteByIdAndServerId(inviteLinkId, serverId);
    }

    @Transactional
    public UserJoinServerByLinkResponse jointByLink(String token, String userId) {
        InviteLink inviteLink =
                inviteLinkRepository
                        .findValidInviteLinkByToken(token)
                        .orElseThrow(() -> new AppException(ErrorCode.INVITE_LINK_NOT_FOUND));
        Server server = inviteLink.getServer();
        User user = userRepository.getReferenceById(userId);
        if (serverMemberRepository.existsByServerIdAndUserId(server.getId(), userId))
            throw new AppException(ErrorCode.USER_ALREADY_A_MEMBER);
        if (serverBanRepository.existsByServer_IdAndUser_Id(server.getId(), userId))
            throw new AppException(ErrorCode.USER_BANNED);
        ServerMember serverMember =
                serverMemberRepository.save(
                        ServerMember.builder().server(server).user(user).build());

        inviteLink.setUseCount(inviteLink.getUseCount() + 1);
        inviteLinkRepository.save(inviteLink);
        return UserJoinServerByLinkResponse.builder()
                .serverId(server.getId())
                .serverName(server.getName())
                .joinedAt(serverMember.getJoinedAt())
                .build();
    }
}
