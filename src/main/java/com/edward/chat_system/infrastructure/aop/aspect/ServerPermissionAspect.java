package com.edward.chat_system.infrastructure.aop.aspect;

import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerPermission;
import com.edward.chat_system.infrastructure.security.permission.RequiresServerPermissionComponent;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Aspect
@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE,makeFinal = true)
public class ServerPermissionAspect {
    RequiresServerPermissionComponent permissionComponent;

    @Before("@annotation(requiresServerPermission)")
    public void check(JoinPoint jp, RequiresServerPermission requiresServerPermission) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = jp.getArgs();

        String serverId = IntStream.range(0, paramNames.length)
                .filter(i -> paramNames[i].equals("serverId"))
                .mapToObj(i -> (String) args[i])
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Method annotated with @RequiresServerPermission must have a 'serverId' param"
                ));

        permissionComponent.check(serverId, requiresServerPermission.value());
    }
}
