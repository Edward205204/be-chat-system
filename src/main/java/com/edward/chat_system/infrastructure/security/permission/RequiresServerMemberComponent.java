package com.edward.chat_system.infrastructure.security.permission;

import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RequiresServerMemberComponent {
    ServerMemberRepository serverMemberRepository;
    CurrentUserProvider currentUserProvider;

    public void check(String serverId) {
        String userId = currentUserProvider.getUserId();
        boolean isMemberServer = serverMemberRepository.existsByServerIdAndUserId(serverId, userId);
        if (!isMemberServer) throw new AppException(ErrorCode.NOT_A_MEMBER);
    }
}
