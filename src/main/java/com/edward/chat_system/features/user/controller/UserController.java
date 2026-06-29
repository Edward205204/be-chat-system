package com.edward.chat_system.features.user.controller;

import com.edward.chat_system.features.user.dto.request.UserPatchUpdateRequest;
import com.edward.chat_system.features.user.dto.response.UserPublicResponse;
import com.edward.chat_system.features.user.dto.response.UserResponse;
import com.edward.chat_system.features.user.service.UserService;
import com.edward.chat_system.shared.dto.ApiResponse;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    @GetMapping("/me")
    ApiResponse<UserResponse> getMe(@AuthenticationPrincipal Jwt principal) {
        return ApiResponse.<UserResponse>builder()
                .message("Get profile successfully")
                .result(userService.getMe(principal.getSubject()))
                .build();
    }

    @PatchMapping("/me")
    ApiResponse<UserResponse> updateProfile(
            @AuthenticationPrincipal Jwt principal,
            @RequestBody @Valid UserPatchUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .message("Update profile successfully")
                .result(userService.updateProfile(principal.getSubject(), request))
                .build();
    }

    // AFTER
    @GetMapping("/search")
    ApiResponse<UserResponse> searchUser(
            @AuthenticationPrincipal Jwt principal,
            @RequestParam("q") String q) {
        try {
            UserResponse result = userService.searchUser(principal.getSubject(), q);
            return ApiResponse.<UserResponse>builder()
                    .message("User found")
                    .result(result)
                    .build();
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
                return ApiResponse.<UserResponse>builder()
                        .message("No user found")
                        .result(null)
                        .build();
            }
            throw e;
        }
    }

    //AFTER
    @GetMapping("/{userId}")
    ApiResponse<UserPublicResponse> getOtherUserProfile(@PathVariable String userId) {
        return ApiResponse.<UserPublicResponse>builder()
                .message("Get user profile successfully")
                .result(userService.getOtherUserProfile(userId))
                .build();
    }
}
