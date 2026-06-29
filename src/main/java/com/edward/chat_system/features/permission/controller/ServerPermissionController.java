package com.edward.chat_system.features.permission.controller;

import com.edward.chat_system.features.permission.dto.request.AddPermissionRequest;
import com.edward.chat_system.features.permission.dto.request.ServerPermissionPutUpdateRequest;
import com.edward.chat_system.features.permission.dto.response.GetPermissionResponse;
import com.edward.chat_system.features.permission.dto.response.RoleWithPermissionResponse;
import com.edward.chat_system.features.permission.service.ServerPermissionService;
import com.edward.chat_system.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ServerPermissionController {
    ServerPermissionService serverPermissionService;

    // AFTER
    @GetMapping("/{serverId}/roles/{roleId}/permissions")
    ApiResponse<GetPermissionResponse> getServerPermission(
            @PathVariable String serverId, @PathVariable String roleId) {
        return ApiResponse.<GetPermissionResponse>builder()
                .message("Get role permissions successfully")
                .result(serverPermissionService.getServerPermission(serverId, roleId))
                .build();
    }

    // AFTER
    @PostMapping("/{serverId}/roles/{roleId}/permissions")
    ApiResponse<Void> addPermissionForRole(
            @PathVariable String serverId,
            @PathVariable String roleId,
            @RequestBody @Valid AddPermissionRequest request) {
        serverPermissionService.addPermissionForRole(serverId, roleId, request);
        return ApiResponse.<Void>builder().message("Grant permission successfully").build();
    }

    // AFTER
    @DeleteMapping("/{serverId}/roles/{roleId}/permissions/{permission}")
    ApiResponse<Void> removePermissionForRole(
            @PathVariable String serverId,
            @PathVariable String roleId,
            @PathVariable String permission) {
        serverPermissionService.removePermissionForRole(serverId, roleId, permission);
        return ApiResponse.<Void>builder().message("Revoke permission successfully").build();
    }

    // AFTER
    @PutMapping("/{serverId}/roles/{roleId}/permissions")
    ApiResponse<RoleWithPermissionResponse> updatePermissionForRole(
            @PathVariable String serverId,
            @PathVariable String roleId,
            @RequestBody @Valid ServerPermissionPutUpdateRequest request) {
        return ApiResponse.<RoleWithPermissionResponse>builder()
                .message("Set role permissions successfully")
                .result(serverPermissionService.updatePermissionForRole(serverId, roleId, request))
                .build();
    }
}
