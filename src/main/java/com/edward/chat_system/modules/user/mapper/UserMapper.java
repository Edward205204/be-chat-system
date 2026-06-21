package com.edward.chat_system.modules.user.mapper;

import org.mapstruct.Mapper;

import com.edward.chat_system.modules.user.dto.response.UserResponse;
import com.edward.chat_system.modules.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse touUserResponse(User user);
}
