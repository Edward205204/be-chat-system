package com.edward.chat_system.infrastructure.security.permission;

import com.edward.chat_system.features.permission.repository.ServerRolePermissionRepository;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.features.server.projection.ServerMemberInfoProjection;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequiresServerPermissionComponent {
    ServerMemberRepository serverMemberRepository;
    ServerRolePermissionRepository serverRolePermissionRepository;

    // AFTER
    // Owner-only until the role position hierarchy is implemented.
    // Delegating MANAGE_ROLES without position constraints risks privilege escalation.
    boolean isManageRolePermission(ServerPermissionKeyEnum permission) {
        return permission == ServerPermissionKeyEnum.MANAGE_ROLES;
    }

    public void check(String serverId, ServerPermissionKeyEnum permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        String userId = auth.getName();

        ServerMemberInfoProjection info =
                serverMemberRepository
                        .findServerMemberInfo(serverId, userId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_A_MEMBER));

        if (info.getIsOwner()) return;

        if (isManageRolePermission(permission))
            throw new AppException(ErrorCode.NOW_DO_NOT_HAVE_PERMISSION);

        if (permission == ServerPermissionKeyEnum.NONE) return;

        boolean hasPermission =
                serverRolePermissionRepository.hasPermission(
                        serverId, info.getUserId(), permission);

        if (!hasPermission) throw new AppException(ErrorCode.NOW_DO_NOT_HAVE_PERMISSION);
    }
}
