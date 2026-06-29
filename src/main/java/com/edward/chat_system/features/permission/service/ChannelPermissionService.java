package com.edward.chat_system.features.permission.service;

import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.channel.repository.ChannelRolePermissionRepository;
import com.edward.chat_system.features.channel.repository.ChannelUserPermissionRepository;
import com.edward.chat_system.features.permission.dto.request.AddChannelPermissionForRoleRequest;
import com.edward.chat_system.features.permission.dto.request.AddChannelPermissionForUserRequest;
import com.edward.chat_system.features.permission.dto.request.ChannelPermissionPutUpdateRequest;
import com.edward.chat_system.features.permission.dto.response.ChannelPermissionByRoleResponse;
import com.edward.chat_system.features.permission.dto.response.ChannelPermissionByUserResponse;
import com.edward.chat_system.features.permission.dto.response.ChannelPermissionConfigDataResponse;
import com.edward.chat_system.features.permission.entity.ChannelRolePermission;
import com.edward.chat_system.features.permission.entity.ChannelUserPermission;
import com.edward.chat_system.features.permission.entity.Role;
import com.edward.chat_system.features.permission.projection.RolePermissionRow;
import com.edward.chat_system.features.permission.projection.UserPermissionRow;
import com.edward.chat_system.features.permission.repository.RoleRepository;
import com.edward.chat_system.features.server.entity.ServerMember;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.infrastructure.aop.annotation.ChannelId;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresChannelPermission;
import com.edward.chat_system.infrastructure.aop.annotation.ServerId;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChannelPermissionService {
    ChannelRolePermissionRepository channelRolePermissionRepository;
    ChannelRepository channelRepository;
    RoleRepository roleRepository;
    ChannelUserPermissionRepository channelUserPermissionRepository;
    ServerMemberRepository serverMemberRepository;

    void checkRoleExist(String serverId, String roleId) {
        roleRepository
                .findByIdAndServerId(roleId, serverId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
    }

    void isMemberOfServer(String serverId, String memberId) {
        if (!serverMemberRepository.existsByIdAndServerId(memberId, serverId))
            throw new AppException(ErrorCode.NOT_A_MEMBER);
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.MANAGE_CHANNEL_PERMISSIONS)
    public ChannelPermissionConfigDataResponse getChannelPermissionConfig(
            @ServerId String serverId, @ChannelId String channelId) {
        Channel channel =
                channelRepository
                        .findByIdAndServerId(channelId, serverId)
                        .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_IS_NOT_EXIST));

        List<RolePermissionRow> roleRows =
                channelRolePermissionRepository.findRolePermissionsByChannelId(channelId);
        List<UserPermissionRow> userRows =
                channelUserPermissionRepository.findUserPermissionsByChannelId(channelId);

        List<ChannelPermissionByRoleResponse> rolePermissions =
                roleRows.stream()
                        .collect(
                                Collectors.groupingBy(
                                        RolePermissionRow::getRoleId,
                                        LinkedHashMap::new,
                                        Collectors.toList()))
                        .entrySet()
                        .stream()
                        .map(
                                e ->
                                        new ChannelPermissionByRoleResponse(
                                                e.getKey(),
                                                e.getValue().getFirst().getRoleName(),
                                                e.getValue().stream()
                                                        .map(RolePermissionRow::getPermission)
                                                        .collect(Collectors.toSet())))
                        .toList();

        List<ChannelPermissionByUserResponse> userPermissions =
                userRows.stream()
                        .collect(
                                Collectors.groupingBy(
                                        UserPermissionRow::getMemberId,
                                        LinkedHashMap::new,
                                        Collectors.toList()))
                        .entrySet()
                        .stream()
                        .map(
                                entry -> {
                                    List<UserPermissionRow> rows = entry.getValue();
                                    return new ChannelPermissionByUserResponse(
                                            entry.getKey(),
                                            rows.getFirst().getUserId(),
                                            rows.getFirst().getDisplayName(),
                                            rows.stream()
                                                    .map(UserPermissionRow::getPermission)
                                                    .collect(Collectors.toSet()));
                                })
                        .toList();

        return ChannelPermissionConfigDataResponse.builder()
                .channelId(channelId)
                .isPrivate(channel.isPrivate())
                .rolePermissions(rolePermissions)
                .userPermissions(userPermissions)
                .build();
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.MANAGE_CHANNEL_PERMISSIONS)
    public void addChannelPermissionForRole(
            @ServerId String serverId,
            @ChannelId String channelId,
            AddChannelPermissionForRoleRequest request) {
        checkRoleExist(serverId, request.getRoleId());
        boolean isExist =
                channelRolePermissionRepository.existsByUniqueConstraint(
                        channelId,
                        request.getRoleId(),
                        ChannelPermissionKeyEnum.valueOf(request.getPermission()));
        if (isExist) throw new AppException(ErrorCode.PERMISSION_DUPLICATE_FOR_THIS_ROLE);
        channelRolePermissionRepository.save(
                ChannelRolePermission.builder()
                        .channel(channelRepository.getReferenceById(channelId))
                        .role(roleRepository.getReferenceById(request.getRoleId()))
                        .permission(ChannelPermissionKeyEnum.valueOf(request.getPermission()))
                        .build());
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.MANAGE_CHANNEL_PERMISSIONS)
    public void removeChannelPermissionForRole(
            @ServerId String serverId,
            @ChannelId String channelId,
            String roleId,
            String permission) {
        checkRoleExist(serverId, roleId);
        channelRolePermissionRepository.deleteByUniqueConstraint(
                channelId, roleId, ChannelPermissionKeyEnum.valueOf(permission));
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.MANAGE_CHANNEL)
    public void updateChannelPermissionForRole(
            @ServerId String serverId,
            @ChannelId String channelId,
            String roleId,
            ChannelPermissionPutUpdateRequest request) {
        checkRoleExist(serverId, roleId);
        channelRolePermissionRepository.deleteManyByRoleIdAndChannelId(roleId, channelId);

        Role role = roleRepository.getReferenceById(roleId);
        Channel channel = channelRepository.getReferenceById(channelId);

        List<ChannelRolePermission> channelRolePermissionEntity =
                request.getPermission().stream()
                        .distinct()
                        .map(
                                permission ->
                                        ChannelRolePermission.builder()
                                                .role(role)
                                                .channel(channel)
                                                .permission(
                                                        ChannelPermissionKeyEnum.valueOf(
                                                                permission))
                                                .build())
                        .toList();

        channelRolePermissionRepository.saveAll(channelRolePermissionEntity);
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.MANAGE_CHANNEL_PERMISSIONS)
    public void addChannelPermissionForUser(
            @ServerId String serverId,
            @ChannelId String channelId,
            AddChannelPermissionForUserRequest request) {

        isMemberOfServer(serverId, request.getMemberId());

        if (channelUserPermissionRepository.existsByUniqueConstraint(
                channelId,
                request.getMemberId(),
                ChannelPermissionKeyEnum.valueOf(request.getPermission())))
            throw new AppException(ErrorCode.USER_ALREADY_ASSIGNED_FOR_THIS_ROLE);

        channelUserPermissionRepository.save(
                ChannelUserPermission.builder()
                        .channel(channelRepository.getReferenceById(channelId))
                        .serverMember(
                                serverMemberRepository.getReferenceById(request.getMemberId()))
                        .permission(ChannelPermissionKeyEnum.valueOf(request.getPermission()))
                        .build());
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.MANAGE_CHANNEL_PERMISSIONS)
    public void removeChannelPermissionForUser(
            @ServerId String serverId,
            @ChannelId String channelId,
            String memberId,
            String permission) {
        isMemberOfServer(serverId, memberId);
        channelUserPermissionRepository.deleteByUniqueConstraint(
                channelId, memberId, ChannelPermissionKeyEnum.valueOf(permission));
    }

    @RequiresChannelPermission(ChannelPermissionKeyEnum.MANAGE_CHANNEL)
    public void updateChannelPermissionForUser(
            @ServerId String serverId,
            @ChannelId String channelId,
            String memberId,
            ChannelPermissionPutUpdateRequest request) {

        isMemberOfServer(serverId, memberId);

        channelUserPermissionRepository.deleteManyByChannelIdAndMemberId(channelId, memberId);

        ServerMember serverMember = serverMemberRepository.getReferenceById(memberId);
        Channel channel = channelRepository.getReferenceById(channelId);

        List<ChannelUserPermission> channelUserPermissionEntity =
                request.getPermission().stream()
                        .distinct()
                        .map(
                                permission ->
                                        ChannelUserPermission.builder()
                                                .serverMember(serverMember)
                                                .channel(channel)
                                                .permission(
                                                        ChannelPermissionKeyEnum.valueOf(
                                                                permission))
                                                .build())
                        .toList();

        channelUserPermissionRepository.saveAll(channelUserPermissionEntity);
    }
}
