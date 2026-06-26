package com.edward.chat_system.infrastructure.aop.validator;

import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerPermission;
import com.edward.chat_system.infrastructure.aop.annotation.ServerId;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ServerPermissionAnnotationValidator
        implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();

        for (String beanName : ctx.getBeanDefinitionNames()) {
            Object bean = ctx.getBean(beanName);
            Class<?> clazz = AopUtils.getTargetClass(bean);

            for (Method method : clazz.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(RequiresServerPermission.class)) continue;
                validateMethod(method);
            }
        }
    }

    private void validateMethod(Method method) {
        boolean hasServerId =
                Arrays.stream(method.getParameters())
                        .anyMatch(p -> p.isAnnotationPresent(ServerId.class));

        if (!hasServerId) {
            throw new IllegalStateException(
                    "%s#%s có @RequiresServerPermission but missing @ServerId"
                            .formatted(
                                    method.getDeclaringClass().getSimpleName(), method.getName()));
        }
    }
}
