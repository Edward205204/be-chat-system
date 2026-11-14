package com.edward.chat_system.infrastructure.security.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.server.projection.ServerMemberInfoProjection;
import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequiresOwnerPermissionTest {

    @Mock ServerMemberRepository serverMemberRepository;
    @Mock CurrentUserProvider currentUserProvider;

    @InjectMocks RequiresOwnerPermission requiresOwnerPermission;

    private ServerMemberInfoProjection memberInfo(boolean isOwner) {
        return new ServerMemberInfoProjection() {
            @Override public String getUserId() { return "user-1"; }
            @Override public String getServerId() { return "srv-1"; }
            @Override public boolean getIsOwner() { return isOwner; }
        };
    }

    @Test
    void check_userIsOwner_doesNotThrow() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "user-1"))
                .thenReturn(Optional.of(memberInfo(true)));

        assertThatCode(() -> requiresOwnerPermission.check("srv-1")).doesNotThrowAnyException();
    }

    @Test
    void check_userIsNotOwner_throwsUncategorized() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "user-1"))
                .thenReturn(Optional.of(memberInfo(false)));

        assertThatThrownBy(() -> requiresOwnerPermission.check("srv-1"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.UNCATEGORIZED));
    }

    @Test
    void check_notAMember_throwsUncategorized() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.findServerMemberInfo("srv-1", "user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requiresOwnerPermission.check("srv-1"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.UNCATEGORIZED));
    }
}
