package com.edward.chat_system.features.server.mapper;

import com.edward.chat_system.features.server.dto.response.InviteLinkResponse;
import com.edward.chat_system.features.server.entity.InviteLink;
import com.edward.chat_system.features.user.dto.response.UserBasicInfoResponse;
import com.edward.chat_system.features.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InviteLinkMapper {

    @Mapping(target = "createdBy", source = "user")
    @Mapping(
            target = "inviteUrl",
            expression = "java(\"https://yourdomain.com/invite/\" + inviteLink.getToken())")
    InviteLinkResponse toResponse(InviteLink inviteLink);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "avatar", source = "avatar")
    UserBasicInfoResponse toBasicInfo(User user);
}
