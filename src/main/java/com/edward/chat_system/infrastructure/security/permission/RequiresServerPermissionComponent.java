package com.edward.chat_system.infrastructure.security.permission;

import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.features.server.projection.ServerMemberInfo;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequiresServerPermissionComponent {
    ServerMemberRepository serverMemberRepository;

    public void check(String serverId, ServerPermissionKeyEnum permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        String userId = auth.getName();

        // 2. Check: Is a User member? If not, throw with Error code: NOT_A_MEMBER
        ServerMemberInfo info = serverMemberRepository.findServerMemberInfo(serverId, userId).orElseThrow(() -> new AppException(ErrorCode.NOT_A_MEMBER));

        // 4.  If the user is an owner, ends check here.
        if (info.getIsOwner()) return;

        // 5. Permission = None, ends check here.
        if (permission == ServerPermissionKeyEnum.NONE) return;

        // 6. Check role permission
        boolean hasPermission = serverMemberRepository.hasPermission(info.getMemberId(), permission);

        if (!hasPermission) throw new AppException(ErrorCode.MISSING_PERMISSION);
    }

}
