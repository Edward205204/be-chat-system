package com.edward.chat_system.infrastructure.aop.aspect;

import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerPermission;
import com.edward.chat_system.infrastructure.security.permission.RequiresServerPermissionComponent;
import com.edward.chat_system.shared.utils.JoinPointParameterUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ServerPermissionAspect {
    RequiresServerPermissionComponent permissionComponent;

    @Before("@annotation(requiresServerPermission)")
    public void check(JoinPoint jp, RequiresServerPermission requiresServerPermission) {
        String serverId = JoinPointParameterUtils.getServerIdParam(jp);

        permissionComponent.check(serverId, requiresServerPermission.value());
    }
}
