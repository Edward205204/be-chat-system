package com.edward.chat_system.infrastructure.aop.validator;

import com.edward.chat_system.infrastructure.aop.annotation.ChannelId;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresChannelPermission;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ChannelPermissionAnnotationValidator
        implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();

        for (String beanName : ctx.getBeanDefinitionNames()) {
            Object bean = ctx.getBean(beanName);
            Class<?> clazz = AopUtils.getTargetClass(bean);

            for (Method method : clazz.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(RequiresChannelPermission.class)) continue;
                validateMethod(method);
            }
        }
    }

    private void validateMethod(Method method) {
        boolean hasChannelId =
                Arrays.stream(method.getParameters())
                        .anyMatch(p -> p.isAnnotationPresent(ChannelId.class));

        if (!hasChannelId) {
            throw new IllegalStateException(
                    "%s#%s has @RequiresChannelPermission but is missing @ChannelId"
                            .formatted(
                                    method.getDeclaringClass().getSimpleName(), method.getName()));
        }
    }
}
