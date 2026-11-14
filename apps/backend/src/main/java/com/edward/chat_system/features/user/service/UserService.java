package com.edward.chat_system.features.user.service;

import com.edward.chat_system.features.file.FileService;
import com.edward.chat_system.features.user.dto.request.UserPatchUpdateRequest;
import com.edward.chat_system.features.user.dto.response.UserPublicResponse;
import com.edward.chat_system.features.user.dto.response.UserResponse;
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.features.user.mapper.UserMapper;
import com.edward.chat_system.features.user.repository.UserRepository;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    FileService fileService;

    public UserResponse getMe(String userId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String userId, UserPatchUpdateRequest request) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USERNAME_EXISTED);

        userMapper.updateUserFromDto(request, user);

        if (request.getAvatar() != null) fileService.claimFile(request.getAvatar());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse searchUser(String currentUserId, String q) {
        return userRepository
                .searchByUsernameOrEmail(q.trim(), currentUserId)
                .map(userMapper::toUserResponse)
                .orElse(null);
    }

    public UserPublicResponse getOtherUserProfile(String userId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserPublicResponse(user);
    }
}
