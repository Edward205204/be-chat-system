package com.edward.chat_system.shared.aop.annotation;

import com.edward.chat_system.shared.aop.aspect.ValidServerPermissionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE_USE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidServerPermissionValidator.class)
public @interface ValidChannelPermission {
    String message() default "Invalid permission";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
