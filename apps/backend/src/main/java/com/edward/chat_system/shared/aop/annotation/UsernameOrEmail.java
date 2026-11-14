package com.edward.chat_system.shared.aop.annotation;

import com.edward.chat_system.shared.aop.aspect.UsernameOrEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameOrEmailValidator.class)
public @interface UsernameOrEmail {
    String message() default "Must be a valid username or email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
