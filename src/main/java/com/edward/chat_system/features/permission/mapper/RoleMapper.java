package com.edward.chat_system.features.permission.mapper;

import com.edward.chat_system.features.permission.dto.request.RolePatchUpdateRequest;
import com.edward.chat_system.features.permission.dto.response.RoleMemberResponse;
import com.edward.chat_system.features.permission.dto.response.RoleResponse;
import com.edward.chat_system.features.permission.entity.Role;
import java.util.List;

import com.edward.chat_system.features.permission.projection.RoleMemberProjection;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    List<RoleResponse> toRoleResponseList(List<Role> roles);

    RoleResponse toRoleResponse(Role role);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoleFromDto(RolePatchUpdateRequest dto, @MappingTarget Role role);
}
