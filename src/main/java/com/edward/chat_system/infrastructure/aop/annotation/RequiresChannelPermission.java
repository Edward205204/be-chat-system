package com.edward.chat_system.infrastructure.aop.annotation;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresChannelPermission {
    ChannelPermissionKeyEnum value() default ChannelPermissionKeyEnum.NONE;
}
