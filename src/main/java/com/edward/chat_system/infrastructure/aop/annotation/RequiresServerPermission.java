package com.edward.chat_system.infrastructure.aop.annotation;

import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresServerPermission {
    ServerPermissionKeyEnum value() default ServerPermissionKeyEnum.NONE;
}