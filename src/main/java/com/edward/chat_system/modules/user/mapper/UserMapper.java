package com.edward.chat_system.modules.user.mapper;

import com.edward.chat_system.modules.user.dto.response.UserResponse;
import com.edward.chat_system.modules.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse touUserResponse(User user);
}
