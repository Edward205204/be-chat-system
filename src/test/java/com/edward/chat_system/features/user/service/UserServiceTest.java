package com.edward.chat_system.features.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.file.FileService;
import com.edward.chat_system.features.user.dto.request.UserPatchUpdateRequest;
import com.edward.chat_system.features.user.dto.response.UserPublicResponse;
import com.edward.chat_system.features.user.dto.response.UserResponse;
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.features.user.mapper.UserMapper;
import com.edward.chat_system.features.user.repository.UserRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @Mock FileService fileService;

    @InjectMocks UserService userService;

    @Test
    void getMe_userExists_returnsUserResponse() {
        User user = User.builder().id("user-1").username("alice").build();
        UserResponse expected = new UserResponse();
        expected.setId("user-1");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        UserResponse result = userService.getMe("user-1");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getMe_userNotFound_throwsAppException() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe("missing"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void updateProfile_usernameNotTaken_updatesAndSaves() {
        User user = User.builder().id("user-1").username("old").build();
        UserPatchUpdateRequest request = new UserPatchUpdateRequest();
        request.setUsername("newname");

        UserResponse expected = new UserResponse();
        expected.setId("user-1");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        UserResponse result = userService.updateProfile("user-1", request);

        assertThat(result).isEqualTo(expected);
        verify(userMapper).updateUserFromDto(request, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_usernameTaken_throwsAppException() {
        User user = User.builder().id("user-1").username("old").build();
        UserPatchUpdateRequest request = new UserPatchUpdateRequest();
        request.setUsername("taken");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile("user-1", request))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.USERNAME_EXISTED));
    }

    @Test
    void updateProfile_withAvatar_claimsFile() {
        User user = User.builder().id("user-1").build();
        UserPatchUpdateRequest request = new UserPatchUpdateRequest();
        request.setAvatar("avatar-file-id");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(new UserResponse());

        userService.updateProfile("user-1", request);

        verify(fileService).claimFile("avatar-file-id");
    }

    @Test
    void updateProfile_userNotFound_throwsAppException() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () -> userService.updateProfile("missing", new UserPatchUpdateRequest()))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void searchUser_found_returnsMappedResponse() {
        User user = User.builder().id("user-2").username("bob").build();
        UserResponse expected = new UserResponse();
        expected.setId("user-2");

        when(userRepository.searchByUsernameOrEmail("bob", "user-1")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        UserResponse result = userService.searchUser("user-1", "  bob  ");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void searchUser_notFound_returnsNull() {
        when(userRepository.searchByUsernameOrEmail("nobody", "user-1")).thenReturn(Optional.empty());

        UserResponse result = userService.searchUser("user-1", "nobody");

        assertThat(result).isNull();
    }

    @Test
    void getOtherUserProfile_userExists_returnsPublicResponse() {
        User user = User.builder().id("user-2").build();
        UserPublicResponse expected = new UserPublicResponse();

        when(userRepository.findById("user-2")).thenReturn(Optional.of(user));
        when(userMapper.toUserPublicResponse(user)).thenReturn(expected);

        UserPublicResponse result = userService.getOtherUserProfile("user-2");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getOtherUserProfile_userNotFound_throwsAppException() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getOtherUserProfile("missing"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }
}
