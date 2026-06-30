package com.edward.chat_system.infrastructure.aop.aspect;

import com.edward.chat_system.infrastructure.security.permission.RequiresServerMemberComponent;
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
public class OwnerPermissionAspect {
    RequiresServerMemberComponent permissionComponent;

    @Before("@annotation(com.edward.chat_system.infrastructure.aop.annotation.RequiresOwner)")
    public void check(JoinPoint jp) {
        String serverId = JoinPointParameterUtils.getServerIdParam(jp);

        if (serverId == null)
            throw new IllegalArgumentException("ServerId is required in @RequiresServerMember");

        permissionComponent.check(serverId);
    }
}
