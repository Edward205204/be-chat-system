package com.edward.chat_system.features.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.edward.chat_system.features.auth.dto.request.LoginRequest;
import com.edward.chat_system.features.auth.dto.request.RegisterRequest;
import com.edward.chat_system.features.auth.dto.response.AuthResponse;
import com.edward.chat_system.features.auth.dto.response.AuthSuccessResponse;
import com.edward.chat_system.features.auth.dto.response.TokenResponse;
import com.edward.chat_system.features.auth.dto.response.UnverifiedResponse;
import com.edward.chat_system.features.auth.entity.RefreshToken;
import com.edward.chat_system.features.auth.entity.VerificationCode;
import com.edward.chat_system.features.auth.enums.VerificationCodeStatusEnum;
import com.edward.chat_system.features.auth.enums.VerificationCodeTypeEnum;
import com.edward.chat_system.features.auth.exception.OtpCooldownException;
import com.edward.chat_system.features.auth.repository.RefreshTokenRepository;
import com.edward.chat_system.features.auth.repository.VerificationCodeRepository;
import com.edward.chat_system.features.user.dto.response.UserResponse;
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.features.user.mapper.UserMapper;
import com.edward.chat_system.features.user.repository.UserRepository;
import com.edward.chat_system.infrastructure.mail.MailServiceImpl;
import com.edward.chat_system.infrastructure.security.jwt.JwtClaimObject;
import com.edward.chat_system.infrastructure.security.jwt.JwtSigner;
import com.edward.chat_system.infrastructure.security.jwt.JwtSignerResponse;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtSigner jwtSigner;
    @Mock UserMapper userMapper;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock MailServiceImpl mailServiceImpl;
    @Mock VerificationCodeRepository verificationCodeRepository;

    @InjectMocks AuthService authService;

    private void injectOtpProps() {
        ReflectionTestUtils.setField(authService, "cooldown", 60L);
        ReflectionTestUtils.setField(authService, "validDuration", 300L);
    }

    private JwtSignerResponse mockJwtResponse(String token) {
        return JwtSignerResponse.builder()
                .token(token)
                .expiresAt(new java.util.Date(System.currentTimeMillis() + 3600000))
                .build();
    }

    @Test
    void login_validCredentials_verifiedUser_returnsAuthSuccessResponse() {
        User user = User.builder().id("u1").email("a@b.com").username("alice").password("hashed").build();
        user.setVerified(true);

        JwtSignerResponse accessJwt = mockJwtResponse("access-token");
        JwtSignerResponse refreshJwt = mockJwtResponse("refresh-token");
        UserResponse userResponse = new UserResponse();
        userResponse.setId("u1");

        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtSigner.generateToken(any())).thenReturn(accessJwt, refreshJwt);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setEmail("a@b.com");
        request.setPassword("pass");

        AuthResponse response = authService.login(request);

        assertThat(response).isInstanceOf(AuthSuccessResponse.class);
        AuthSuccessResponse success = (AuthSuccessResponse) response;
        assertThat(success.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void login_userNotFound_throwsLoginFailed() {
        when(userRepo.findByEmail("x@x.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("x@x.com");
        request.setPassword("pass");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.LOGIN_FAILED));
    }

    @Test
    void login_wrongPassword_throwsLoginFailed() {
        User user = User.builder().id("u1").email("a@b.com").password("hashed").build();
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("a@b.com");
        request.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.LOGIN_FAILED));
    }

    @Test
    void login_unverifiedUser_returnsTmpToken() {
        User user = User.builder().id("u1").email("a@b.com").username("alice").password("hashed").build();
        user.setVerified(false);

        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtSigner.generateToken(any())).thenReturn(mockJwtResponse("tmp-token"));

        LoginRequest request = new LoginRequest();
        request.setEmail("a@b.com");
        request.setPassword("pass");

        AuthResponse response = authService.login(request);

        assertThat(response).isInstanceOf(UnverifiedResponse.class);
        assertThat(((UnverifiedResponse) response).getTmpToken()).isEqualTo("tmp-token");
    }

    @Test
    void register_newUser_returnsUnverifiedResponse() {
        injectOtpProps();
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("a@b.com");
        request.setPassword("pass");

        User savedUser = User.builder().id("u1").username("alice").email("a@b.com").build();

        when(userRepo.existsByUsername("alice")).thenReturn(false);
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(userRepo.save(any())).thenReturn(savedUser);
        when(jwtSigner.generateToken(any())).thenReturn(mockJwtResponse("tmp-token"));

        UnverifiedResponse response = authService.register(request);

        assertThat(response.getTmpToken()).isEqualTo("tmp-token");
    }

    @Test
    void register_usernameTaken_throwsUsernameExisted() {
        when(userRepo.existsByUsername("alice")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.USERNAME_EXISTED));
    }

    @Test
    void register_emailAlreadyVerified_throwsEmailExisted() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("bob");
        request.setEmail("a@b.com");

        User existingUser = User.builder().email("a@b.com").build();
        existingUser.setVerified(true);

        when(userRepo.existsByUsername("bob")).thenReturn(false);
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.EMAIL_EXISTED));
    }

    @Test
    void validateOtp_expired_throwsOtpExpired() {
        VerificationCode code = new VerificationCode();
        code.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        code.setStatus(VerificationCodeStatusEnum.PENDING);
        code.setAttemptCount(0);

        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        authService, "validateOtp", code, "123456"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.OTP_EXPIRED));
    }

    @Test
    void validateOtp_revoked_throwsOtpRevoked() {
        VerificationCode code = new VerificationCode();
        code.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        code.setStatus(VerificationCodeStatusEnum.REVOKED);
        code.setAttemptCount(0);

        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        authService, "validateOtp", code, "123456"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.OTP_REVOKED));
    }

    @Test
    void validateOtp_alreadyVerified_throwsOtpHasBeenUsed() {
        VerificationCode code = new VerificationCode();
        code.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        code.setStatus(VerificationCodeStatusEnum.VERIFIED);
        code.setAttemptCount(0);

        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        authService, "validateOtp", code, "123456"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.OTP_HAS_BEEN_USED));
    }

    @Test
    void validateOtp_wrongCode_incrementsAttemptCount() {
        VerificationCode code = new VerificationCode();
        code.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        code.setStatus(VerificationCodeStatusEnum.PENDING);
        code.setAttemptCount(0);
        code.setCode("111111");

        when(verificationCodeRepository.save(any())).thenReturn(code);

        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        authService, "validateOtp", code, "999999"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.OTP_INCORRECT));

        assertThat(code.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void validateOtp_exceeds5Attempts_revokesAndThrowsMaxAttempts() {
        VerificationCode code = new VerificationCode();
        code.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        code.setStatus(VerificationCodeStatusEnum.PENDING);
        code.setAttemptCount(5);
        code.setCode("111111");

        when(verificationCodeRepository.save(any())).thenReturn(code);

        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        authService, "validateOtp", code, "999999"))
                .isInstanceOf(AppException.class)
                .satisfies(
                        e ->
                                assertThat(((AppException) e).getErrorCode())
                                        .isEqualTo(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED));

        assertThat(code.getStatus()).isEqualTo(VerificationCodeStatusEnum.REVOKED);
    }

    @Test
    void sendOtpVerifyEmail_alreadyVerified_throwsUserVerified() {
        injectOtpProps();
        when(verificationCodeRepository.findByUserIdAndType("u1", VerificationCodeTypeEnum.EMAIL_VERIFY))
                .thenReturn(Optional.empty());
        when(userRepo.getUserStatusByUserId("u1")).thenReturn(true);

        assertThatThrownBy(() -> authService.sendOtpVerifyEmail("u1", "a@b.com"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.USER_VERIFIED));
    }

    @Test
    void sendOtpVerifyEmail_onCooldown_throwsOtpCooldown() {
        injectOtpProps();
        VerificationCode code = new VerificationCode();
        code.setId("vc-1");
        code.setLastSentAt(LocalDateTime.now().minusSeconds(30));

        when(verificationCodeRepository.findByUserIdAndType("u1", VerificationCodeTypeEnum.EMAIL_VERIFY))
                .thenReturn(Optional.of(code));
        when(userRepo.getUserStatusByUserId("u1")).thenReturn(false);

        assertThatThrownBy(() -> authService.sendOtpVerifyEmail("u1", "a@b.com"))
                .isInstanceOf(OtpCooldownException.class);
    }

    @Test
    void logout_deletesRefreshToken() {
        authService.logout("some-refresh-token");
        verify(refreshTokenRepository).deleteByToken("some-refresh-token");
    }

    @Test
    void refreshToken_validToken_returnsNewTokens() {
        User user = User.builder().id("u1").email("a@b.com").username("alice").build();
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token("old-token")
                        .user(user)
                        .expiresAt(LocalDateTime.now().plusHours(1))
                        .build();

        JwtSignerResponse accessJwt = mockJwtResponse("new-access");
        JwtSignerResponse refreshJwt = mockJwtResponse("new-refresh");

        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(refreshToken));
        when(userRepo.findById("u1")).thenReturn(Optional.of(user));
        when(jwtSigner.generateToken(any())).thenReturn(accessJwt, refreshJwt);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        TokenResponse result = authService.refreshToken("old-token");

        assertThat(result.getAccessToken().getToken()).isEqualTo("new-access");
        verify(refreshTokenRepository).deleteByToken("old-token");
    }

    @Test
    void refreshToken_tokenNotFound_throwsUnauthenticated() {
        when(refreshTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("bad-token"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHENTICATED));
    }

    @Test
    void refreshToken_tokenExpired_throwsUnauthenticated() {
        User user = User.builder().id("u1").build();
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token("expired-token")
                        .user(user)
                        .expiresAt(LocalDateTime.now().minusHours(1))
                        .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refreshToken("expired-token"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHENTICATED));
    }

    @Test
    void resetPassword_emailNotFound_throwsEmailNotFound() {
        when(userRepo.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword("x@x.com", "123456", "newpass"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_FOUND));
    }
}
