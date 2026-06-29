package com.edward.chat_system.shared.aop.aspect;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.shared.aop.annotation.ValidChannelPermission;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidChannelPermissionValidator
        implements ConstraintValidator<ValidChannelPermission, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) return true; // @NotBlank handle null
        try {
            ChannelPermissionKeyEnum e = ChannelPermissionKeyEnum.valueOf(value);
            return e != ChannelPermissionKeyEnum.NONE;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
