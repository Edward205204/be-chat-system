package com.edward.chat_system.shared.aop.aspect;

import com.edward.chat_system.shared.aop.annotation.UsernameOrEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UsernameOrEmailValidator implements ConstraintValidator<UsernameOrEmail, String> {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9_]{3,20}$");

    Validator validator;

    private boolean isUsername(String value) {
        return USERNAME_PATTERN.matcher(value).matches();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank handle null and blank
        }

        if (isUsername(value)) {
            return true;
        }

        return validator.validateValue(Temp.class, "email", value).isEmpty();
    }

    private static class Temp {
        @Email String email;
    }
}
