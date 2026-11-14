package com.edward.chat_system.infrastructure.security.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class CurrentUserProviderTest {

    @InjectMocks CurrentUserProvider currentUserProvider;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserId_authenticatedUser_returnsUserId() {
        var auth = new UsernamePasswordAuthenticationToken("user-123", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = currentUserProvider.getUserId();

        assertThat(result).isEqualTo("user-123");
    }

    @Test
    void getUserId_noAuthentication_throwsUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> currentUserProvider.getUserId())
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHENTICATED));
    }
}
