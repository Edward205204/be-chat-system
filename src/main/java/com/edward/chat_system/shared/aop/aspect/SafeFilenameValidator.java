package com.edward.chat_system.shared.aop.aspect;

import com.edward.chat_system.shared.aop.annotation.SafeFilename;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class SafeFilenameValidator implements ConstraintValidator<SafeFilename, String> {
    private static final Pattern SAFE_PATTERN = Pattern.compile("^[a-zA-Z0-9._\\-]+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        return SAFE_PATTERN.matcher(value).matches();
    }
}
