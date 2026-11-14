package com.edward.chat_system.features.permission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.features.channel.repository.ChannelRolePermissionRepository;
import com.edward.chat_system.features.channel.repository.ChannelUserPermissionRepository;
import com.edward.chat_system.features.permission.dto.request.AddChannelPermissionForRoleRequest;
import com.edward.chat_system.features.permission.dto.request.AddChannelPermissionForUserRequest;
import com.edward.chat_system.features.permission.dto.request.ChannelPermissionPutUpdateRequest;
import com.edward.chat_system.features.permission.dto.response.ChannelPermissionConfigDataResponse;
import com.edward.chat_system.features.permission.entity.ChannelRolePermission;
import com.edward.chat_system.features.permission.entity.ChannelUserPermission;
import com.edward.chat_system.features.permission.entity.Role;
import com.edward.chat_system.features.permission.projection.RolePermissionRow;
import com.edward.chat_system.features.permission.projection.UserPermissionRow;
import com.edward.chat_system.features.permission.repository.RoleRepository;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
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
class ChannelPermissionServiceTest {

    @Mock ChannelRolePermissionRepository channelRolePermissionRepository;
    @Mock ChannelRepository channelRepository;
    @Mock RoleRepository roleRepository;
    @Mock ChannelUserPermissionRepository channelUserPermissionRepository;
    @Mock ServerMemberRepository serverMemberRepository;

    @InjectMocks ChannelPermissionService channelPermissionService;

    @Test
    void getChannelPermissionConfig_returnsGroupedRoleAndUserPermissions() {
        Channel channel = Channel.builder().id("ch-1").build();
        channel.setPrivate(false);

        RolePermissionRow roleRow = mock(RolePermissionRow.class);
        when(roleRow.getRoleId()).thenReturn("r1");
        when(roleRow.getRoleName()).thenReturn("Admin");
        when(roleRow.getPermission()).thenReturn(ChannelPermissionKeyEnum.VIEW_CHANNEL);

        UserPermissionRow userRow = mock(UserPermissionRow.class);
        when(userRow.getMemberId()).thenReturn("m1");
        when(userRow.getUserId()).thenReturn("u1");
        when(userRow.getDisplayName()).thenReturn("Alice");
        when(userRow.getPermission()).thenReturn(ChannelPermissionKeyEnum.SEND_MESSAGES);

        when(channelRepository.findByIdAndServerId("ch-1", "srv-1")).thenReturn(Optional.of(channel));
        when(channelRolePermissionRepository.findRolePermissionsByChannelId("ch-1"))
                .thenReturn(List.of(roleRow));
        when(channelUserPermissionRepository.findUserPermissionsByChannelId("ch-1"))
                .thenReturn(List.of(userRow));

        ChannelPermissionConfigDataResponse result =
                channelPermissionService.getChannelPermissionConfig("srv-1", "ch-1");

        assertThat(result.getChannelId()).isEqualTo("ch-1");
        assertThat(result.getRolePermissions()).hasSize(1);
        assertThat(result.getRolePermissions().get(0).getRoleName()).isEqualTo("Admin");
        assertThat(result.getUserPermissions()).hasSize(1);
        assertThat(result.getUserPermissions().get(0).getDisplayName()).isEqualTo("Alice");
    }

    @Test
    void getChannelPermissionConfig_channelNotExist_throwsChannelNotExist() {
        when(channelRepository.findByIdAndServerId("ch-99", "srv-1")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () -> channelPermissionService.getChannelPermissionConfig("srv-1", "ch-99"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.CHANNEL_IS_NOT_EXIST));
    }

    @Test
    void addChannelPermissionForRole_notDuplicate_savesPermission() {
        Role role = Role.builder().id("r1").build();
        AddChannelPermissionForRoleRequest request = new AddChannelPermissionForRoleRequest();
        request.setRoleId("r1");
        request.setPermission("VIEW_CHANNEL");

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(channelRolePermissionRepository.existsByUniqueConstraint(
                        "ch-1", "r1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .thenReturn(false);
        when(channelRepository.getReferenceById("ch-1")).thenReturn(null);
        when(roleRepository.getReferenceById("r1")).thenReturn(role);

        channelPermissionService.addChannelPermissionForRole("srv-1", "ch-1", request);

        verify(channelRolePermissionRepository).save(any(ChannelRolePermission.class));
    }

    @Test
    void addChannelPermissionForRole_duplicate_throwsPermissionDuplicate() {
        Role role = Role.builder().id("r1").build();
        AddChannelPermissionForRoleRequest request = new AddChannelPermissionForRoleRequest();
        request.setRoleId("r1");
        request.setPermission("VIEW_CHANNEL");

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(channelRolePermissionRepository.existsByUniqueConstraint(
                        "ch-1", "r1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .thenReturn(true);

        assertThatThrownBy(
                        () ->
                                channelPermissionService.addChannelPermissionForRole(
                                        "srv-1", "ch-1", request))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.PERMISSION_DUPLICATE_FOR_THIS_ROLE));
    }

    @Test
    void removeChannelPermissionForRole_roleExists_deletesPermission() {
        Role role = Role.builder().id("r1").build();
        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));

        channelPermissionService.removeChannelPermissionForRole("srv-1", "ch-1", "r1", "VIEW_CHANNEL");

        verify(channelRolePermissionRepository)
                .deleteByUniqueConstraint("ch-1", "r1", ChannelPermissionKeyEnum.VIEW_CHANNEL);
    }

    @Test
    void removeChannelPermissionForRole_roleNotExist_throwsRoleNotExist() {
        when(roleRepository.findByIdAndServerId("r99", "srv-1")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                channelPermissionService.removeChannelPermissionForRole(
                                        "srv-1", "ch-1", "r99", "VIEW_CHANNEL"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_EXIST));
    }

    @Test
    void updateChannelPermissionForRole_roleExists_replacesPermissions() {
        Role role = Role.builder().id("r1").build();
        ChannelPermissionPutUpdateRequest request = new ChannelPermissionPutUpdateRequest();
        request.setPermission(Set.of("VIEW_CHANNEL", "SEND_MESSAGES"));

        when(roleRepository.findByIdAndServerId("r1", "srv-1")).thenReturn(Optional.of(role));
        when(roleRepository.getReferenceById("r1")).thenReturn(role);
        when(channelRepository.getReferenceById("ch-1")).thenReturn(null);

        channelPermissionService.updateChannelPermissionForRole("srv-1", "ch-1", "r1", request);

        verify(channelRolePermissionRepository).deleteManyByRoleIdAndChannelId("r1", "ch-1");
        verify(channelRolePermissionRepository).saveAll(any());
    }

    @Test
    void addChannelPermissionForUser_validInput_savesPermission() {
        AddChannelPermissionForUserRequest request = new AddChannelPermissionForUserRequest();
        request.setMemberId("m1");
        request.setPermission("VIEW_CHANNEL");

        when(serverMemberRepository.existsByIdAndServerId("m1", "srv-1")).thenReturn(true);
        when(channelUserPermissionRepository.existsByUniqueConstraint(
                        "ch-1", "m1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .thenReturn(false);
        when(channelRepository.getReferenceById("ch-1")).thenReturn(null);
        when(serverMemberRepository.getReferenceById("m1")).thenReturn(null);

        channelPermissionService.addChannelPermissionForUser("srv-1", "ch-1", request);

        verify(channelUserPermissionRepository).save(any(ChannelUserPermission.class));
    }

    @Test
    void addChannelPermissionForUser_notMember_throwsNotAMember() {
        AddChannelPermissionForUserRequest request = new AddChannelPermissionForUserRequest();
        request.setMemberId("m99");
        request.setPermission("VIEW_CHANNEL");

        when(serverMemberRepository.existsByIdAndServerId("m99", "srv-1")).thenReturn(false);

        assertThatThrownBy(
                        () ->
                                channelPermissionService.addChannelPermissionForUser(
                                        "srv-1", "ch-1", request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_A_MEMBER));
    }

    @Test
    void addChannelPermissionForUser_duplicate_throwsAlreadyAssigned() {
        AddChannelPermissionForUserRequest request = new AddChannelPermissionForUserRequest();
        request.setMemberId("m1");
        request.setPermission("VIEW_CHANNEL");

        when(serverMemberRepository.existsByIdAndServerId("m1", "srv-1")).thenReturn(true);
        when(channelUserPermissionRepository.existsByUniqueConstraint(
                        "ch-1", "m1", ChannelPermissionKeyEnum.VIEW_CHANNEL))
                .thenReturn(true);

        assertThatThrownBy(
                        () ->
                                channelPermissionService.addChannelPermissionForUser(
                                        "srv-1", "ch-1", request))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.USER_ALREADY_ASSIGNED_FOR_THIS_ROLE));
    }

    @Test
    void removeChannelPermissionForUser_memberExists_deletesPermission() {
        when(serverMemberRepository.existsByIdAndServerId("m1", "srv-1")).thenReturn(true);

        channelPermissionService.removeChannelPermissionForUser("srv-1", "ch-1", "m1", "VIEW_CHANNEL");

        verify(channelUserPermissionRepository)
                .deleteByUniqueConstraint("ch-1", "m1", ChannelPermissionKeyEnum.VIEW_CHANNEL);
    }

    @Test
    void removeChannelPermissionForUser_notMember_throwsNotAMember() {
        when(serverMemberRepository.existsByIdAndServerId("m99", "srv-1")).thenReturn(false);

        assertThatThrownBy(
                        () ->
                                channelPermissionService.removeChannelPermissionForUser(
                                        "srv-1", "ch-1", "m99", "VIEW_CHANNEL"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_A_MEMBER));
    }

    @Test
    void updateChannelPermissionForUser_validInput_replacesPermissions() {
        ChannelPermissionPutUpdateRequest request = new ChannelPermissionPutUpdateRequest();
        request.setPermission(Set.of("VIEW_CHANNEL", "SEND_MESSAGES"));

        when(serverMemberRepository.existsByIdAndServerId("m1", "srv-1")).thenReturn(true);
        when(serverMemberRepository.getReferenceById("m1")).thenReturn(null);
        when(channelRepository.getReferenceById("ch-1")).thenReturn(null);

        channelPermissionService.updateChannelPermissionForUser("srv-1", "ch-1", "m1", request);

        verify(channelUserPermissionRepository).deleteManyByChannelIdAndMemberId("ch-1", "m1");
        verify(channelUserPermissionRepository).saveAll(any());
    }

    @Test
    void updateChannelPermissionForUser_notMember_throwsNotAMember() {
        when(serverMemberRepository.existsByIdAndServerId("m99", "srv-1")).thenReturn(false);

        assertThatThrownBy(
                        () ->
                                channelPermissionService.updateChannelPermissionForUser(
                                        "srv-1",
                                        "ch-1",
                                        "m99",
                                        new ChannelPermissionPutUpdateRequest()))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_A_MEMBER));
    }
}
