package com.edward.chat_system.features.user.mapper;

import com.edward.chat_system.features.auth.dto.request.RegisterRequest;
import com.edward.chat_system.features.user.dto.request.UserPatchUpdateRequest;
import com.edward.chat_system.features.user.dto.response.UserBanInfoResponse;
import com.edward.chat_system.features.user.dto.response.UserPublicResponse;
import com.edward.chat_system.features.user.dto.response.UserResponse;
import com.edward.chat_system.features.user.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    UserBanInfoResponse toUserBanInfoResponse(User user);

    UserPublicResponse toUserPublicResponse(User user);

    //    @Mapping(target = "id", ignore = true)
    //    @Mapping(target = "avatar", ignore = true)
    //    @Mapping(target = "banner", ignore = true)
    //    @Mapping(target = "isVerified", ignore = true)
    //    @Mapping(target = "createdAt", ignore = true)
    //    User toUser(RegisterRequest request)

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "banner", ignore = true)
    //    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateUserFromRequest(@MappingTarget User user, RegisterRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserPatchUpdateRequest dto, @MappingTarget User user);
}
