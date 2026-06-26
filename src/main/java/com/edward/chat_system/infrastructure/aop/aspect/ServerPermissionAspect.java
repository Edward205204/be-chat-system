package com.edward.chat_system.infrastructure.aop.aspect;

import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerPermission;
import com.edward.chat_system.infrastructure.aop.annotation.ServerId;
import com.edward.chat_system.infrastructure.security.permission.RequiresServerPermissionComponent;
import java.lang.reflect.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ServerPermissionAspect {
    RequiresServerPermissionComponent permissionComponent;

    @Before("@annotation(requiresServerPermission)")
    public void check(JoinPoint jp, RequiresServerPermission requiresServerPermission) {
        Parameter[] params = ((MethodSignature) jp.getSignature()).getMethod().getParameters();
        Object[] args = jp.getArgs();

        String serverId = null;

        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(ServerId.class)) {
                serverId = (String) args[i];
                break;
            }
        }

        permissionComponent.check(serverId, requiresServerPermission.value());
    }
}
