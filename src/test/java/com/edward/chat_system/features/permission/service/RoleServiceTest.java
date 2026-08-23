package com.edward.chat_system.features.permission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.permission.constant.RoleConstants;
import com.edward.chat_system.features.permission.dto.request.CreateRoleRequest;
import com.edward.chat_system.features.permission.dto.request.RolePatchUpdateRequest;
import com.edward.chat_system.features.permission.dto.response.RoleMemberResponse;
import com.edward.chat_system.features.permission.dto.response.RoleResponse;
import com.edward.chat_system.features.permission.entity.Role;
import com.edward.chat_system.features.permission.entity.RoleMember;
import com.edward.chat_system.features.permission.entity.ServerRolePermission;
import com.edward.chat_system.features.permission.mapper.RoleMapper;
import com.edward.chat_system.features.permission.mapper.RoleMemberMapper;
import com.edward.chat_system.features.permission.projection.RoleMemberProjection;
import com.edward.chat_system.features.permission.repository.RoleMemberRepository;
import com.edward.chat_system.features.permission.repository.RoleRepository;
import com.edward.chat_system.features.permission.repository.ServerRolePermissionRepository;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.features.server.repository.ServerRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock RoleMapper roleMapper;
    @Mock RoleMemberMapper roleMemberMapper;
    @Mock RoleRepository roleRepository;
    @Mock ServerRepository serverRepository;
    @Mock ServerMemberRepository serverMemberRepository;
    @Mock RoleMemberRepository roleMemberRepository;
    @Mock ServerRolePermissionRepository serverRolePermissionRepository;

    @InjectMocks RoleService roleService;

    @Test
    void getServerRoles_returnsMappedList() {
        List<Role> roles = List.of(Role.builder().id("r1").name("Admin").build());
        List<RoleResponse> expected = List.of(RoleResponse.builder().id("r1").name("Admin").build());

        when(roleRepository.findAllByServerId("srv-1")).thenReturn(roles);
        when(roleMapper.toRoleResponseList(roles)).thenReturn(expected);

        List<RoleResponse> result = roleService.getServerRoles("srv-1");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void createRole_nameNotDuplicate_savesAndReturnsResponse() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("Moderator");
        request.setColor("#FF0000");

        Role savedRole = Role.builder().id("r1").name("Moderator").build();
        RoleResponse expected = RoleResponse.builder().id("r1").name("Moderator").build();

        when(roleRepository.existsByServerIdAndName("srv-1", "Moderator")).thenReturn(false);
        when(serverRepository.getReferenceById("srv-1")).thenReturn(null);
        when(roleRepository.save(any())).thenReturn(savedRole);
        when(roleMapper.toRoleResponse(savedRole)).thenReturn(expected);

        RoleResponse result = roleService.createRole("srv-1", request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void createRole_nameDuplicate_throwsRoleNameDuplicate() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("Admin");

        when(roleRepository.existsByServerIdAndName("srv-1", "Admin")).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole("srv-1", request))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.ROLE_NAME_DUPLICATE));
    }

    @Test
    void createDefaultEveryoneRoleWithDefaultPermission_savesRoleAndPermission() {
        when(serverRepository.getReferenceById("srv-1")).thenReturn(null);
        when(roleRepository.save(any())).thenReturn(Role.builder().id("r1").build());
        when(serverRolePermissionRepository.save(any())).thenReturn(null);

        roleService.createDefaultEveryoneRoleWithDefaultPermission("srv-1");

        verify(roleRepository).save(argThat(r -> r.getName().equals(RoleConstants.DEFAULT_ROLE_NAME)));
        verify(serverRolePermissionRepository)
                .save(argThat(p -> p.getPermission() == ServerPermissionKeyEnum.CREATE_INVITE));
    }

    @Test
    void patchUpdateRole_roleExists_updatesAndReturns() {
        Role role = Role.builder().id("r1").name("OldName").build();
        RolePatchUpdateRequest request = new RolePatchUpdateRequest();
        request.setName("NewName");
        RoleResponse expected = RoleResponse.builder().id("r1").name("NewName").build();

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(roleRepository.existsByServerIdAndName("srv-1", "NewName")).thenReturn(false);
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toRoleResponse(role)).thenReturn(expected);

        RoleResponse result = roleService.patchUpdateRole("srv-1", "r1", request);

        assertThat(result).isEqualTo(expected);
        verify(roleMapper).updateRoleFromDto(request, role);
    }

    @Test
    void patchUpdateRole_roleNotExist_throwsRoleNotExist() {
        when(roleRepository.findByIdAndServerId("r99", "srv-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.patchUpdateRole("srv-1", "r99", new RolePatchUpdateRequest()))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_EXIST));
    }

    @Test
    void patchUpdateRole_newNameDuplicateOtherRole_throwsRoleNameDuplicate() {
        Role role = Role.builder().id("r1").name("OldName").build();
        RolePatchUpdateRequest request = new RolePatchUpdateRequest();
        request.setName("TakenName");

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(roleRepository.existsByServerIdAndName("srv-1", "TakenName")).thenReturn(true);

        assertThatThrownBy(() -> roleService.patchUpdateRole("srv-1", "r1", request))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.ROLE_NAME_DUPLICATE));
    }

    @Test
    void deleteRole_roleExists_deletesById() {
        Role role = Role.builder().id("r1").build();
        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));

        roleService.deleteRole("srv-1", "r1");

        verify(roleRepository).deleteByRoleId("r1");
    }

    @Test
    void deleteRole_roleNotExist_throwsRoleNotExist() {
        when(roleRepository.findByIdAndServerId("r99", "srv-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.deleteRole("srv-1", "r99"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_EXIST));
    }

    @Test
    void getRoleMember_roleExists_returnsMappedList() {
        Role role = Role.builder().id("r1").build();
        List<RoleMemberProjection> projections = List.of(mock(RoleMemberProjection.class));
        List<RoleMemberResponse> expected = List.of(new RoleMemberResponse());

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(roleMemberRepository.findAllByRoleId("r1")).thenReturn(projections);
        when(roleMemberMapper.toResponseList(projections)).thenReturn(expected);

        List<RoleMemberResponse> result = roleService.getRoleMember("srv-1", "r1");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void addRoleMember_validInput_savesRoleMember() {
        Role role = Role.builder().id("r1").build();

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(serverMemberRepository.existsByIdAndServerId("m1", "srv-1")).thenReturn(true);
        when(roleMemberRepository.existsByRoleIdAndServerMemberId("r1", "m1")).thenReturn(false);
        when(roleRepository.getReferenceById("r1")).thenReturn(role);
        when(serverMemberRepository.getReferenceById("m1")).thenReturn(null);

        roleService.addRoleMember("srv-1", "r1", "m1");

        verify(roleMemberRepository).save(any(RoleMember.class));
    }

    @Test
    void addRoleMember_memberNotInServer_throwsNotAMember() {
        Role role = Role.builder().id("r1").build();

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(serverMemberRepository.existsByIdAndServerId("m1", "srv-1")).thenReturn(false);

        assertThatThrownBy(() -> roleService.addRoleMember("srv-1", "r1", "m1"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_A_MEMBER));
    }

    @Test
    void addRoleMember_alreadyAssigned_throwsAlreadyAssigned() {
        Role role = Role.builder().id("r1").build();

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(serverMemberRepository.existsByIdAndServerId("m1", "srv-1")).thenReturn(true);
        when(roleMemberRepository.existsByRoleIdAndServerMemberId("r1", "m1")).thenReturn(true);

        assertThatThrownBy(() -> roleService.addRoleMember("srv-1", "r1", "m1"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.USER_ALREADY_ASSIGNED_FOR_THIS_ROLE));
    }

    @Test
    void removeRoleMember_roleExists_deletesRoleMember() {
        Role role = Role.builder().id("r1").build();
        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));

        roleService.removeRoleMember("srv-1", "r1", "m1");

        verify(roleMemberRepository).deleteByRoleIdAndMemberId("r1", "m1");
    }
}
