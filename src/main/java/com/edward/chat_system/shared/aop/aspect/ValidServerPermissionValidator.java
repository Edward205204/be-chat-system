package com.edward.chat_system.shared.aop.aspect;

import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.shared.aop.annotation.ValidServerPermission;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidServerPermissionValidator
        implements ConstraintValidator<ValidServerPermission, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) return true; // @NotBlank handle null
        try {
            ServerPermissionKeyEnum e = ServerPermissionKeyEnum.valueOf(value);
            return e != ServerPermissionKeyEnum.NONE;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
