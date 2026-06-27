package com.edward.chat_system.features.permission.mapper;

import com.edward.chat_system.features.permission.dto.request.RolePatchRequest;
import com.edward.chat_system.features.permission.dto.response.RoleResponse;
import com.edward.chat_system.features.permission.entity.Role;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    List<RoleResponse> toRoleResponseList(List<Role> roles);

    RoleResponse toRoleResponse(Role role);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoleFromDto(RolePatchRequest dto, @MappingTarget Role role);
}
