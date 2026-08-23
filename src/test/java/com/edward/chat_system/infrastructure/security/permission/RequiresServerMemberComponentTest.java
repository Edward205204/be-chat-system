package com.edward.chat_system.infrastructure.security.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.server.repository.ServerMemberRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequiresServerMemberComponentTest {

    @Mock ServerMemberRepository serverMemberRepository;
    @Mock CurrentUserProvider currentUserProvider;

    @InjectMocks RequiresServerMemberComponent serverMemberComponent;

    @Test
    void check_userIsMember_doesNotThrow() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.existsByServerIdAndUserId("srv-1", "user-1")).thenReturn(true);

        assertThatCode(() -> serverMemberComponent.check("srv-1")).doesNotThrowAnyException();
    }

    @Test
    void check_userIsNotMember_throwsNotAMember() {
        when(currentUserProvider.getUserId()).thenReturn("user-1");
        when(serverMemberRepository.existsByServerIdAndUserId("srv-1", "user-1")).thenReturn(false);

        assertThatThrownBy(() -> serverMemberComponent.check("srv-1"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_A_MEMBER));
    }
}
