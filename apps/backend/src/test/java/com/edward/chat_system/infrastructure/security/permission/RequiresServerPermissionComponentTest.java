package com.edward.chat_system.infrastructure.security.permission;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.features.server.projection.ServerMemberInfoProjection;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.features.permission.repository.ServerRolePermissionRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RequiresServerPermissionComponentTest {

    @Mock ServerMemberRepository serverMemberRepository;
    @Mock ServerRolePermissionRepository serverRolePermissionRepository;

    @InjectMocks RequiresServerPermissionComponent permissionComponent;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(String userId) {
        var auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private ServerMemberInfoProjection memberInfo(String userId, boolean isOwner) {
        return new ServerMemberInfoProjection() {
            @Override public String getUserId() { return userId; }
            @Override public String getServerId() { return "srv-1"; }
            @Override public boolean getIsOwner() { return isOwner; }
        };
    }

    @Test
    void check_noAuthentication_throwsUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> permissionComponent.check("srv-1", ServerPermissionKeyEnum.KICK_MEMBER))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHENTICATED));
    }

    @Test
    void check_ownerUser_passes() {
        setAuthentication("owner-user");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "owner-user"))
                .thenReturn(Optional.of(memberInfo("owner-user", true)));

        assertThatCode(() -> permissionComponent.check("srv-1", ServerPermissionKeyEnum.KICK_MEMBER))
                .doesNotThrowAnyException();

        verifyNoInteractions(serverRolePermissionRepository);
    }

    @Test
    void check_nonOwner_withManageRolesPermission_throwsNoPermission() {
        setAuthentication("regular-user");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "regular-user"))
                .thenReturn(Optional.of(memberInfo("regular-user", false)));

        assertThatThrownBy(() -> permissionComponent.check("srv-1", ServerPermissionKeyEnum.MANAGE_ROLES))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.NOW_DO_NOT_HAVE_PERMISSION));
    }

    @Test
    void check_nonOwner_withNonePermission_passes() {
        setAuthentication("regular-user");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "regular-user"))
                .thenReturn(Optional.of(memberInfo("regular-user", false)));

        assertThatCode(() -> permissionComponent.check("srv-1", ServerPermissionKeyEnum.NONE))
                .doesNotThrowAnyException();

        verifyNoInteractions(serverRolePermissionRepository);
    }

    @Test
    void check_nonOwner_hasRequiredPermission_passes() {
        setAuthentication("regular-user");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "regular-user"))
                .thenReturn(Optional.of(memberInfo("regular-user", false)));
        when(serverRolePermissionRepository.hasPermission("srv-1", "regular-user", ServerPermissionKeyEnum.KICK_MEMBER))
                .thenReturn(true);

        assertThatCode(() -> permissionComponent.check("srv-1", ServerPermissionKeyEnum.KICK_MEMBER))
                .doesNotThrowAnyException();
    }

    @Test
    void check_nonOwner_missingPermission_throwsNoPermission() {
        setAuthentication("regular-user");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "regular-user"))
                .thenReturn(Optional.of(memberInfo("regular-user", false)));
        when(serverRolePermissionRepository.hasPermission("srv-1", "regular-user", ServerPermissionKeyEnum.KICK_MEMBER))
                .thenReturn(false);

        assertThatThrownBy(() -> permissionComponent.check("srv-1", ServerPermissionKeyEnum.KICK_MEMBER))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.NOW_DO_NOT_HAVE_PERMISSION));
    }

    @Test
    void check_notAMember_throwsNotAMember() {
        setAuthentication("non-member");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "non-member")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionComponent.check("srv-1", ServerPermissionKeyEnum.KICK_MEMBER))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_A_MEMBER));
    }
}
