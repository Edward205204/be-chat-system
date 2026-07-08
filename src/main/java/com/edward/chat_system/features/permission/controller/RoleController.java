package com.edward.chat_system.features.permission.controller;

import com.edward.chat_system.features.permission.dto.request.AddRoleMemberRequest;
import com.edward.chat_system.features.permission.dto.request.CreateRoleRequest;
import com.edward.chat_system.features.permission.dto.request.RolePatchUpdateRequest;
import com.edward.chat_system.features.permission.dto.response.RoleMemberResponse;
import com.edward.chat_system.features.permission.dto.response.RoleResponse;
import com.edward.chat_system.features.permission.service.RoleService;
import com.edward.chat_system.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class RoleController {
    RoleService roleService;

    @GetMapping("/{serverId}/roles")
    ApiResponse<List<RoleResponse>> getServerRoles(@PathVariable String serverId) {
        return ApiResponse.<List<RoleResponse>>builder()
                .message("Get roles successfully")
                .result(roleService.getServerRoles(serverId))
                .build();
    }

    @PostMapping("/{serverId}/roles")
    ApiResponse<RoleResponse> createRole(
            @PathVariable String serverId, @RequestBody @Valid CreateRoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .message("Create role successfully")
                .result(roleService.createRole(serverId, request))
                .build();
    }

    @PatchMapping("/{serverId}/roles/{roleId}")
    ApiResponse<RoleResponse> patchUpdateRole(
            @PathVariable String serverId,
            @PathVariable String roleId,
            @RequestBody @Valid RolePatchUpdateRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .message("Update role successfully")
                .result(roleService.patchUpdateRole(serverId, roleId, request))
                .build();
    }

    @DeleteMapping("/{serverId}/roles/{roleId}")
    ApiResponse<Void> deleteRole(@PathVariable String serverId, @PathVariable String roleId) {
        roleService.deleteRole(serverId, roleId);
        return ApiResponse.<Void>builder().message("Delete role successfully").build();
    }

    @GetMapping("/{serverId}/roles/{roleId}/members")
    ApiResponse<List<RoleMemberResponse>> getRoleMember(
            @PathVariable String serverId, @PathVariable String roleId) {
        return ApiResponse.<List<RoleMemberResponse>>builder()
                .message("Get role members successfully")
                .result(roleService.getRoleMember(serverId, roleId))
                .build();
    }

    @PostMapping("/{serverId}/roles/{roleId}/members")
    ApiResponse<Void> addRoleMember(
            @PathVariable String serverId,
            @PathVariable String roleId,
            @RequestBody @Valid AddRoleMemberRequest request) {
        roleService.addRoleMember(serverId, roleId, request.getMemberId());
        return ApiResponse.<Void>builder().message("Assign role successfully").build();
    }

    @DeleteMapping("/{serverId}/roles/{roleId}/members/{memberId}")
    ApiResponse<Void> removeRoleMember(
            @PathVariable String serverId,
            @PathVariable String roleId,
            @PathVariable String memberId) {
        roleService.removeRoleMember(serverId, roleId, memberId);
        return ApiResponse.<Void>builder().message("Unassign role successfully").build();
    }
}
