package com.edward.chat_system.shared.aop.aspect;

import com.edward.chat_system.shared.aop.annotation.AtLeastOneField;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Objects;

public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, Object> {
    @Override
    public boolean isValid(Object req, ConstraintValidatorContext ctx) {
        return Arrays.stream(req.getClass().getDeclaredFields())
                .map(
                        f -> {
                            f.trySetAccessible();
                            try {
                                return f.get(req);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                .anyMatch(Objects::nonNull);
    }
}
