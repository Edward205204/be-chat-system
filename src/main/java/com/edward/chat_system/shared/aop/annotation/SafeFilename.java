package com.edward.chat_system.shared.aop.annotation;

import com.edward.chat_system.shared.aop.aspect.SafeFilenameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeFilenameValidator.class)
public @interface SafeFilename {
    String message() default "Invalid filename";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
