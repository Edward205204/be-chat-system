package com.edward.chat_system.modules.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.edward.chat_system.infrastructure.jwt.JwtSigner;
import com.edward.chat_system.infrastructure.jwt.JwtSignerResponse;
import com.edward.chat_system.modules.auth.dto.request.AuthRequest;
import com.edward.chat_system.modules.auth.dto.response.AuthResponse;
import com.edward.chat_system.modules.auth.dto.response.AuthSuccessResponse;
import com.edward.chat_system.modules.auth.dto.response.UnverifiedResponse;
import com.edward.chat_system.modules.auth.entity.RefreshToken;
import com.edward.chat_system.modules.auth.repository.RefreshTokenRepository;
import com.edward.chat_system.modules.user.mapper.UserMapper;
import com.edward.chat_system.modules.user.repository.UserRepository;
import com.edward.chat_system.shared.enums.TokenTypeEnum;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import com.edward.chat_system.shared.utils.DateTimeUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    UserRepository userRepo;
    PasswordEncoder passwordEncoder;
    JwtSigner jwtSigner;
    UserMapper userMapper;
    RefreshTokenRepository refreshTokenRepository;

    public AuthResponse login(AuthRequest request) {
        var user = userRepo.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }
        if (!user.isVerified()) {
            String tmpTokenString = jwtSigner.generateToken(user.getUsername(), TokenTypeEnum.TMP_TOKEN).getToken();
            return UnverifiedResponse.builder().tmpToken(tmpTokenString).build();
        }
        JwtSignerResponse accessToken = jwtSigner.generateToken(user.getUsername(), TokenTypeEnum.ACCESS_TOKEN);
        JwtSignerResponse refreshToken = jwtSigner.generateToken(user.getUsername(), TokenTypeEnum.REFRESH_TOKEN);
        refreshTokenRepository.save(
                RefreshToken.builder().user(user).token(refreshToken.getToken())
                        .expiresAt(DateTimeUtils.toLocalDateTime(refreshToken.getExpiresAt()))
                        .build());
        return AuthSuccessResponse.builder().accessToken(accessToken.getToken()).refreshToken(refreshToken.getToken())
                .user(userMapper.touUserResponse(user)).build();
    }
}
