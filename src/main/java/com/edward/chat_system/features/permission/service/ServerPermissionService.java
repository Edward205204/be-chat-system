package com.edward.chat_system.features.permission.service;

import com.edward.chat_system.features.permission.dto.request.AddPermissionRequest;
import com.edward.chat_system.features.permission.dto.request.GetPermissionRequest;
import com.edward.chat_system.features.permission.dto.request.RolePermissionPutUpdate;
import com.edward.chat_system.features.permission.dto.response.RoleWithPermissionResponse;
import com.edward.chat_system.features.permission.entity.Role;
import com.edward.chat_system.features.permission.entity.ServerRolePermission;
import com.edward.chat_system.features.permission.projection.PermissionNameProjection;
import com.edward.chat_system.features.permission.repository.RoleRepository;
import com.edward.chat_system.features.permission.repository.ServerRolePermissionRepository;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerMember;
import com.edward.chat_system.infrastructure.aop.annotation.RequiresServerPermission;
import com.edward.chat_system.infrastructure.aop.annotation.ServerId;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServerPermissionService {
    ServerRolePermissionRepository serverRolePermissionRepository;
    RoleRepository roleRepository;

    Role checkRoleExist(String serverId, String roleId) {
        return roleRepository
                .findByIdAndServerId(roleId, serverId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
    }

    @RequiresServerMember
    public GetPermissionRequest getServerPermission(@ServerId String serverId, String roleId) {
        checkRoleExist(serverId, roleId);
        List<PermissionNameProjection> permissionList =
                serverRolePermissionRepository.findPermissionsByRoleId(roleId);
        return GetPermissionRequest.builder().roleId(roleId).permissions(permissionList).build();
    }

    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_ROLES)
    public void addPermissionForRole(
            @ServerId String serverId, String roleId, AddPermissionRequest request) {
        Role role = checkRoleExist(serverId, roleId);
        ServerPermissionKeyEnum permission =
                ServerPermissionKeyEnum.valueOf(request.getPermission());
        serverRolePermissionRepository.save(
                ServerRolePermission.builder().role(role).permission(permission).build());
    }

    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_ROLES)
    public void removePermissionForRole(
            @ServerId String serverId, String roleId, String permission) {
        checkRoleExist(serverId, roleId);
        ServerPermissionKeyEnum permissionEnum = ServerPermissionKeyEnum.valueOf(permission);
        serverRolePermissionRepository.deletePermission(roleId, permissionEnum);
    }

    @Transactional
    @RequiresServerPermission(ServerPermissionKeyEnum.MANAGE_ROLES)
    public RoleWithPermissionResponse updatePermissionForRole(
            @ServerId String serverId, String roleId, RolePermissionPutUpdate request) {
        Role role = checkRoleExist(serverId, roleId);
        serverRolePermissionRepository.deleteByRole_Id(roleId);
        serverRolePermissionRepository.flush();
        List<ServerRolePermission> permissions =
                request.getPermission().stream()
                        .map(
                                permission ->
                                        ServerRolePermission.builder()
                                                .role(role)
                                                .permission(
                                                        ServerPermissionKeyEnum.valueOf(permission))
                                                .build())
                        .toList();

        serverRolePermissionRepository.saveAll(permissions);

        return RoleWithPermissionResponse.builder()
                .roleId(roleId)
                .permissions(request.getPermission())
                .build();
    }
}
