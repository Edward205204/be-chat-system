package com.edward.chat_system.infrastructure.security.permission;

import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.channel.repository.ChannelRolePermissionRepository;
import com.edward.chat_system.features.channel.repository.ChannelUserPermissionRepository;
import com.edward.chat_system.features.server.projection.ServerMemberInfo;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequiresChannelPermissionComponent {
    CurrentUserProvider currentUserProvider;
    ChannelRepository channelRepository;
    ServerMemberRepository serverMemberRepository;
    ChannelUserPermissionRepository channelUserPermissionRepository;
    ChannelRolePermissionRepository channelRolePermissionRepository;

    public String resolveServerId(String channelId) {
        // AFTER: Replace resolveServerId() with resolveChannelInfo() to fetch both serverId and
        // isPrivate
        // in a single query, avoiding a redundant channel lookup when caller does not pass
        // @ServerId.
        return channelRepository
                .findServerIdByChannelId(channelId)
                .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_IS_NOT_EXIST));
    }

    public void check(String serverId, String channelId, ChannelPermissionKeyEnum permission) {
        String userId = currentUserProvider.getUserId();
        ServerMemberInfo info =
                serverMemberRepository
                        .findServerMemberInfo(serverId, userId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_A_MEMBER));

        if (info.getIsOwner()) return;

        if (permission == ChannelPermissionKeyEnum.NONE) return;

        Channel channel =
                channelRepository
                        .findById(channelId)
                        .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_IS_NOT_EXIST));

        if (!channel.isPrivate()) return;

        if (channelUserPermissionRepository.hasPermission(userId, channelId, permission)) return;

        boolean hasPermission =
                channelRolePermissionRepository.hasPermission(info.getMemberId(), permission);

        if (!hasPermission) throw new AppException(ErrorCode.MISSING_PERMISSION);
    }
}
