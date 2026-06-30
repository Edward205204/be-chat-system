package com.edward.chat_system.infrastructure.security.permission;

import com.edward.chat_system.features.server.projection.ServerMemberInfoProjection;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RequiresOwnerPermission {
    ServerMemberRepository serverMemberRepository;
    CurrentUserProvider currentUserProvider;

    public void check(String serverId) {
        String userId = currentUserProvider.getUserId();
        ServerMemberInfoProjection memberInfo =
                serverMemberRepository
                        .findServerMemberInfo(serverId, userId)
                        .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED));
        if (!memberInfo.getIsOwner()) throw new AppException(ErrorCode.UNCATEGORIZED);
    }
}
