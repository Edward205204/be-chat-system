package com.edward.chat_system.features.permission.service;

import com.edward.chat_system.features.permission.dto.request.CreateRoleRequest;
import com.edward.chat_system.features.permission.dto.request.RolePatchUpdateRequest;
import com.edward.chat_system.features.permission.dto.response.RoleMemberResponse;
import com.edward.chat_system.features.permission.dto.response.RoleResponse;
import com.edward.chat_system.features.permission.entity.Role;
import com.edward.chat_system.features.permission.entity.RoleMember;
import com.edward.chat_system.features.permission.mapper.RoleMapper;
import com.edward.chat_system.features.permission.mapper.RoleMemberMapper;
import com.edward.chat_system.features.permission.projection.RoleMemberProjection;
import com.edward.chat_system.features.permission.repository.RoleMemberRepository;
import com.edward.chat_system.features.permission.repository.RoleRepository;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.features.server.repository.ServerRepository;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerMember;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerPermission;
import com.edward.chat_system.infrastructure.aop.annotation.ServerId;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleMapper roleMapper;
    RoleMemberMapper roleMemberMapper;
    RoleRepository roleRepository;
    ServerRepository serverRepository;
    ServerMemberRepository serverMemberRepository;
    RoleMemberRepository roleMemberRepository;

    Role checkRoleExist(String serverId, String roleId) {
        return roleRepository
                .findByIdAndServerId(roleId, serverId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
    }

    @RequiresServerMember
    public List<RoleResponse> getServerRoles(@ServerId String serverId) {
        List<Role> role = roleRepository.findAllByServerId(serverId);
        return roleMapper.toRoleResponseList(role);
    }

    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_ROLES)
    public RoleResponse createRole(@ServerId String serverId, CreateRoleRequest request) {
        if (roleRepository.existsByServerIdAndName(serverId, request.getName()))
            throw new AppException(ErrorCode.ROLE_NAME_DUPLICATE);
        Role role =
                Role.builder()
                        .name(request.getName())
                        .color(request.getColor())
                        .server(serverRepository.getReferenceById(serverId))
                        .build();
        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_ROLES)
    public RoleResponse patchUpdateRole(
            @ServerId String serverId, String roleId, RolePatchUpdateRequest request) {
        Role role = checkRoleExist(serverId, roleId);

        if (request.getName() != null
                && !request.getName().equals(role.getName())
                && roleRepository.existsByServerIdAndName(serverId, request.getName())) {
            throw new AppException(ErrorCode.ROLE_NAME_DUPLICATE);
        }

        roleMapper.updateRoleFromDto(request, role);
        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_ROLES)
    public void deleteRole(@ServerId String serverId, String roleId) {
        Role role = checkRoleExist(serverId, roleId);
        roleRepository.delete(role);
    }

    //   AFTER: @RequireRoleMember
    public List<RoleMemberResponse> getRoleMember(@ServerId String serverId, String roleId) {
        checkRoleExist(serverId, roleId);
        List<RoleMemberProjection> roleMembers = roleMemberRepository.findAllByRoleId(roleId);
        return roleMemberMapper.toResponseList(roleMembers);
    }

    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_ROLES)
    public void addRoleMember(@ServerId String serverId, String roleId, String memberId) {
        checkRoleExist(serverId, roleId);
        roleMemberRepository.save(
                RoleMember.builder()
                        .role(roleRepository.getReferenceById(roleId))
                        .serverMember(serverMemberRepository.getReferenceById(memberId))
                        .build());
    }
}
