package com.edward.chat_system.features.permission.controller;

import com.edward.chat_system.features.permission.dto.request.AddChannelPermissionForRoleRequest;
import com.edward.chat_system.features.permission.dto.request.AddChannelPermissionForUserRequest;
import com.edward.chat_system.features.permission.dto.request.ChannelPermissionPutUpdateRequest;
import com.edward.chat_system.features.permission.dto.response.ChannelPermissionConfigDataResponse;
import com.edward.chat_system.features.permission.service.ChannelPermissionService;
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
public class ChannelPermissionController {
    ChannelPermissionService channelPermissionService;

    // AFTER
    @GetMapping("/{serverId}/channels/{channelId}/permissions")
    ApiResponse<ChannelPermissionConfigDataResponse> getChannelPermissionConfig(
            @PathVariable String serverId, @PathVariable String channelId) {
        return ApiResponse.<ChannelPermissionConfigDataResponse>builder()
                .message("Get channel permissions successfully")
                .result(channelPermissionService.getChannelPermissionConfig(serverId, channelId))
                .build();
    }

    // AFTER
    @PostMapping("/{serverId}/channels/{channelId}/permissions/roles")
    ApiResponse<Void> addChannelPermissionForRole(
            @PathVariable String serverId,
            @PathVariable String channelId,
            @RequestBody @Valid AddChannelPermissionForRoleRequest request) {
        channelPermissionService.addChannelPermissionForRole(serverId, channelId, request);
        return ApiResponse.<Void>builder()
                .message("Grant channel role permission successfully")
                .build();
    }

    // AFTER
    @DeleteMapping(
            "/{serverId}/channels/{channelId}/permissions/roles/{roleId}/permission/{permission}")
    ApiResponse<Void> removeChannelPermissionForRole(
            @PathVariable String serverId,
            @PathVariable String channelId,
            @PathVariable String roleId,
            @PathVariable String permission) {
        channelPermissionService.removeChannelPermissionForRole(
                serverId, channelId, roleId, permission);
        return ApiResponse.<Void>builder()
                .message("Revoke channel role permission successfully")
                .build();
    }

    // AFTER
    @PutMapping("/{serverId}/channels/{channelId}/permissions/roles/{roleId}")
    ApiResponse<Void> updateChannelPermissionForRole(
            @PathVariable String serverId,
            @PathVariable String channelId,
            @PathVariable String roleId,
            @RequestBody @Valid ChannelPermissionPutUpdateRequest request) {
        channelPermissionService.updateChannelPermissionForRole(
                serverId, channelId, roleId, request);
        return ApiResponse.<Void>builder()
                .message("Set channel role permissions successfully")
                .build();
    }

    // AFTER
    @PostMapping("/{serverId}/channels/{channelId}/permissions/users")
    ApiResponse<Void> addChannelPermissionForUser(
            @PathVariable String serverId,
            @PathVariable String channelId,
            @RequestBody @Valid AddChannelPermissionForUserRequest request) {
        channelPermissionService.addChannelPermissionForUser(serverId, channelId, request);
        return ApiResponse.<Void>builder()
                .message("Grant channel user permission successfully")
                .build();
    }

    // AFTER
    @DeleteMapping(
            "/{serverId}/channels/{channelId}/permissions/users/{memberId}/permission/{permission}")
    ApiResponse<Void> removeChannelPermissionForUser(
            @PathVariable String serverId,
            @PathVariable String channelId,
            @PathVariable String memberId,
            @PathVariable String permission) {
        channelPermissionService.removeChannelPermissionForUser(
                serverId, channelId, memberId, permission);
        return ApiResponse.<Void>builder()
                .message("Revoke channel user permission successfully")
                .build();
    }

    // AFTER
    @PutMapping("/{serverId}/channels/{channelId}/permissions/users/{memberId}")
    ApiResponse<Void> updateChannelPermissionForUser(
            @PathVariable String serverId,
            @PathVariable String channelId,
            @PathVariable String memberId,
            @RequestBody @Valid ChannelPermissionPutUpdateRequest request) {
        channelPermissionService.updateChannelPermissionForUser(
                serverId, channelId, memberId, request);
        return ApiResponse.<Void>builder()
                .message("Set channel user permissions successfully")
                .build();
    }
}
