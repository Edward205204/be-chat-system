package com.edward.chat_system.infrastructure.aop.aspect;

import com.edward.chat_system.infrastructure.aop.annotation.ChannelId;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresChannelPermission;
import com.edward.chat_system.infrastructure.aop.annotation.ServerId;
import com.edward.chat_system.infrastructure.security.permission.RequiresChannelPermissionComponent;
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
public class ChannelPermissionAspect {
    RequiresChannelPermissionComponent permissionComponent;

    @Before("@annotation(requiresChannelPermission)")
    public void check(JoinPoint jp, RequiresChannelPermission requiresChannelPermission) {
        Parameter[] params = ((MethodSignature) jp.getSignature()).getMethod().getParameters();
        Object[] args = jp.getArgs();

        String serverId = null;
        String channelId = null;

        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(ServerId.class)) serverId = (String) args[i];
            if (params[i].isAnnotationPresent(ChannelId.class)) channelId = (String) args[i];
        }

        if (serverId == null) {
            serverId = permissionComponent.resolveServerId(channelId);
        }
        permissionComponent.check(serverId, channelId, requiresChannelPermission.value());
    }
}
