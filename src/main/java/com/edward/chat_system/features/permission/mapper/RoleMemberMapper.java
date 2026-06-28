package com.edward.chat_system.features.permission.mapper;

import com.edward.chat_system.features.permission.dto.response.RoleMemberResponse;
import com.edward.chat_system.features.permission.projection.RoleMemberProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMemberMapper {
    @Mapping(target = "memberId", source = "serverMemberId")
    RoleMemberResponse toResponse(RoleMemberProjection projection);

    List<RoleMemberResponse> toResponseList(List<RoleMemberProjection> projections);
}
