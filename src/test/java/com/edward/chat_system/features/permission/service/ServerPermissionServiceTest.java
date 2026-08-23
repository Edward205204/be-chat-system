package com.edward.chat_system.features.permission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.permission.dto.request.AddPermissionRequest;
import com.edward.chat_system.features.permission.dto.request.ServerPermissionPutUpdateRequest;
import com.edward.chat_system.features.permission.dto.response.GetPermissionResponse;
import com.edward.chat_system.features.permission.dto.response.RoleWithPermissionResponse;
import com.edward.chat_system.features.permission.entity.Role;
import com.edward.chat_system.features.permission.entity.ServerRolePermission;
import com.edward.chat_system.features.permission.projection.PermissionNameProjection;
import com.edward.chat_system.features.permission.repository.RoleRepository;
import com.edward.chat_system.features.permission.repository.ServerRolePermissionRepository;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServerPermissionServiceTest {

    @Mock ServerRolePermissionRepository serverRolePermissionRepository;
    @Mock RoleRepository roleRepository;

    @InjectMocks ServerPermissionService serverPermissionService;

    @Test
    void getServerPermission_roleExists_returnsResponse() {
        Role role = Role.builder().id("r1").build();
        List<PermissionNameProjection> perms = List.of(mock(PermissionNameProjection.class));

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(serverRolePermissionRepository.findPermissionsByRoleId("r1")).thenReturn(perms);

        GetPermissionResponse result = serverPermissionService.getServerPermission("srv-1", "r1");

        assertThat(result.getRoleId()).isEqualTo("r1");
        assertThat(result.getPermissions()).isEqualTo(perms);
    }

    @Test
    void getServerPermission_roleNotExist_throwsRoleNotExist() {
        when(roleRepository.findByIdAndServerId("r99", "srv-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serverPermissionService.getServerPermission("srv-1", "r99"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_EXIST));
    }

    @Test
    void addPermissionForRole_notDuplicate_savesPermission() {
        Role role = Role.builder().id("r1").build();
        AddPermissionRequest request = new AddPermissionRequest();
        request.setPermission("KICK_MEMBER");

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(serverRolePermissionRepository.existsByRoleIdAndPermission(
                        "r1", ServerPermissionKeyEnum.KICK_MEMBER))
                .thenReturn(false);

        serverPermissionService.addPermissionForRole("srv-1", "r1", request);

        verify(serverRolePermissionRepository).save(any(ServerRolePermission.class));
    }

    @Test
    void addPermissionForRole_duplicate_throwsPermissionDuplicate() {
        Role role = Role.builder().id("r1").build();
        AddPermissionRequest request = new AddPermissionRequest();
        request.setPermission("KICK_MEMBER");

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(serverRolePermissionRepository.existsByRoleIdAndPermission(
                        "r1", ServerPermissionKeyEnum.KICK_MEMBER))
                .thenReturn(true);

        assertThatThrownBy(() -> serverPermissionService.addPermissionForRole("srv-1", "r1", request))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.PERMISSION_DUPLICATE_FOR_THIS_ROLE));
    }

    @Test
    void addPermissionForRole_roleNotExist_throwsRoleNotExist() {
        when(roleRepository.findByIdAndServerId("r99", "srv-1")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                serverPermissionService.addPermissionForRole(
                                        "srv-1", "r99", new AddPermissionRequest()))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_EXIST));
    }

    @Test
    void removePermissionForRole_roleExists_deletesPermission() {
        Role role = Role.builder().id("r1").build();
        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));

        serverPermissionService.removePermissionForRole("srv-1", "r1", "KICK_MEMBER");

        verify(serverRolePermissionRepository)
                .deletePermission("r1", ServerPermissionKeyEnum.KICK_MEMBER);
    }

    @Test
    void removePermissionForRole_roleNotExist_throwsRoleNotExist() {
        when(roleRepository.findByIdAndServerId("r99", "srv-1")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () -> serverPermissionService.removePermissionForRole("srv-1", "r99", "KICK_MEMBER"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_EXIST));
    }

    @Test
    void updatePermissionForRole_roleExists_replacesPermissions() {
        Role role = Role.builder().id("r1").build();
        ServerPermissionPutUpdateRequest request = new ServerPermissionPutUpdateRequest();
        request.setPermission(Set.of("KICK_MEMBER", "BAN_MEMBER"));

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(serverRolePermissionRepository.saveAll(any())).thenReturn(List.of());

        RoleWithPermissionResponse result =
                serverPermissionService.updatePermissionForRole("srv-1", "r1", request);

        verify(serverRolePermissionRepository).deleteByRole_Id("r1");
        verify(serverRolePermissionRepository).flush();
        assertThat(result.getRoleId()).isEqualTo("r1");
        assertThat(result.getPermissions()).containsExactly("KICK_MEMBER", "BAN_MEMBER");
    }

    @Test
    void updatePermissionForRole_roleNotExist_throwsRoleNotExist() {
        when(roleRepository.findByIdAndServerId("r99", "srv-1")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                serverPermissionService.updatePermissionForRole(
                                        "srv-1", "r99", new ServerPermissionPutUpdateRequest()))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_EXIST));
    }
}
