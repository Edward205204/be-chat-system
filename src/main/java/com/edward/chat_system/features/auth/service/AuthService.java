package com.edward.chat_system.features.auth.service;

import com.edward.chat_system.infrastructure.jwt.JwtClaimObject;
import com.edward.chat_system.infrastructure.jwt.JwtSigner;
import com.edward.chat_system.infrastructure.jwt.JwtSignerResponse;
import com.edward.chat_system.infrastructure.mail.MailServiceImpl;
import com.edward.chat_system.infrastructure.mail.MailTemplate;
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
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.features.user.mapper.UserMapper;
import com.edward.chat_system.features.user.repository.UserRepository;
import com.edward.chat_system.shared.enums.TokenTypeEnum;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import com.edward.chat_system.shared.utils.DateTimeUtils;
import com.edward.chat_system.shared.utils.OtpUtils;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    @NonFinal
    @Value("${otp.cooldown}")
    long cooldown;

    @NonFinal
    @Value("${otp.valid_duration}")
    long validDuration;

    UserRepository userRepo;
    PasswordEncoder passwordEncoder;
    JwtSigner jwtSigner;
    UserMapper userMapper;
    RefreshTokenRepository refreshTokenRepository;
    MailServiceImpl mailServiceImpl;
    VerificationCodeRepository verificationCodeRepository;

    TokenResponse generateAccessTokenAndRefreshToken(@NonNull JwtClaimObject claim) {
        JwtSignerResponse accessToken =
                jwtSigner.generateToken(
                        claim.toBuilder().tokenType(TokenTypeEnum.ACCESS_TOKEN).build());

        JwtSignerResponse refreshToken =
                jwtSigner.generateToken(
                        claim.toBuilder().tokenType(TokenTypeEnum.REFRESH_TOKEN).build());

        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    UnverifiedResponse unverifiedResponseBuilder(JwtClaimObject claim) {
        claim = claim.toBuilder().tokenType(TokenTypeEnum.TMP_TOKEN).build();
        String tmpTokenString = jwtSigner.generateToken(claim).getToken();
        return UnverifiedResponse.builder().tmpToken(tmpTokenString).build();
    }

    public AuthResponse login(@NonNull LoginRequest request) {
        var user =
                userRepo.findByEmail(request.getEmail())
                        .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }
        JwtClaimObject claim =
                JwtClaimObject.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build();
        if (!user.isVerified()) {
            return unverifiedResponseBuilder(claim);
        }
        TokenResponse tokenResponse = generateAccessTokenAndRefreshToken(claim);
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .token(tokenResponse.getRefreshToken().getToken())
                        .expiresAt(
                                DateTimeUtils.toLocalDateTime(
                                        tokenResponse.getRefreshToken().getExpiresAt()))
                        .build());
        return AuthSuccessResponse.builder()
                .accessToken(tokenResponse.getAccessToken().getToken())
                .refreshToken(tokenResponse.getRefreshToken().getToken())
                .user(userMapper.touUserResponse(user))
                .build();
    }

    public UnverifiedResponse register(@NonNull RegisterRequest request) {
        boolean isExistsByUsername = userRepo.existsByUsername(request.getUsername());
        if (isExistsByUsername) throw new AppException(ErrorCode.USERNAME_EXISTED);
        var user = userRepo.findByEmail(request.getEmail()).orElseGet(User::new);

        if (user.getEmail() != null && user.isVerified()) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        user = userMapper.toUser(request);
        String hashed = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashed);
        user = userRepo.save(user);
        return unverifiedResponseBuilder(
                JwtClaimObject.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build());
    }

    private void validateCanSendCode(@NonNull VerificationCode code) {
        VerificationCodeStatusEnum codeStatus = code.getStatus();
        if (codeStatus == VerificationCodeStatusEnum.VERIFIED)
            throw new AppException(ErrorCode.OTP_HAS_BEEN_USED);
        if ((codeStatus == VerificationCodeStatusEnum.PENDING)
                || (codeStatus == VerificationCodeStatusEnum.REVOKED)) validateCoolDown(code);
    }

    private void validateCoolDown(@NonNull VerificationCode code) {
        LocalDateTime now = DateTimeUtils.now();
        if (now.isBefore(code.getLastSentAt().plusSeconds(cooldown)))
            throw new OtpCooldownException(ErrorCode.OTP_COOLDOWN, code.getLastSentAt());
    }

    @Retryable(value = MailException.class, maxRetries = 3, delay = 500)
    public void sendOtpVerifyEmail(String userId, String email) {
        VerificationCode code =
                verificationCodeRepository.findByUserId(userId).orElseGet(VerificationCode::new);
        if (code.getId() != null) validateCanSendCode(code);
        String otp = OtpUtils.generateOtp();

        code.setUser(userRepo.getReferenceById(userId));
        code.setType(VerificationCodeTypeEnum.EMAIL_VERIFY);
        code.renewOtp(otp, validDuration);

        verificationCodeRepository.save(code);
        mailServiceImpl.sendOtp(email, otp, MailTemplate.OTP_VERIFICATION);
    }

    VerificationCode validateOtp(@NonNull VerificationCode code, String otp) {
        if (DateTimeUtils.now().isAfter(code.getExpiresAt()))
            throw new AppException(ErrorCode.OTP_EXPIRED);

        if (code.getStatus().equals(VerificationCodeStatusEnum.REVOKED))
            throw new AppException(ErrorCode.OTP_REVOKED);
        if (code.getStatus().equals(VerificationCodeStatusEnum.VERIFIED))
            throw new AppException(ErrorCode.OTP_HAS_BEEN_USED);

        if (!otp.equals(code.getCode())) {
            code.setAttemptCount(code.getAttemptCount() + 1);
            verificationCodeRepository.save(code);
            if (code.getAttemptCount() > 5) {
                code.setStatus(VerificationCodeStatusEnum.REVOKED);
                verificationCodeRepository.save(code);
                throw new AppException(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
            }
            throw new AppException(ErrorCode.OTP_INCORRECT);
        }
        return code;
    }

    public TokenResponse verifyEmailOtp(String userId, String otp) {
        VerificationCode code =
                verificationCodeRepository
                        .findByUserIdAndType(userId, VerificationCodeTypeEnum.EMAIL_VERIFY)
                        .orElseThrow(() -> new AppException(ErrorCode.OTP_DOES_NOT_EXIST));

        code = validateOtp(code, otp);

        code.setStatus(VerificationCodeStatusEnum.VERIFIED);
        verificationCodeRepository.save(code);
        User user = code.getUser();
        user.setVerified(true);
        userRepo.save(user);

        TokenResponse tokenResponse =
                generateAccessTokenAndRefreshToken(
                        JwtClaimObject.builder()
                                .userId(user.getId())
                                .email(user.getEmail())
                                .username(user.getUsername())
                                .build());

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .token(tokenResponse.getRefreshToken().getToken())
                        .expiresAt(
                                DateTimeUtils.toLocalDateTime(
                                        tokenResponse.getRefreshToken().getExpiresAt()))
                        .build());
        return tokenResponse;
    }

    @Retryable(value = MailException.class, maxRetries = 3, delay = 500)
    public void sendOtpForgotPassword(String email) {
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) return;
        VerificationCode code =
                verificationCodeRepository
                        .findByUserIdAndType(user.getId(), VerificationCodeTypeEnum.RESET_PASSWORD)
                        .orElseGet(VerificationCode::new);
        if (code.getId() != null) validateCanSendCode(code);
        String otp = OtpUtils.generateOtp();
        code.setUser(user);
        code.setType(VerificationCodeTypeEnum.RESET_PASSWORD);
        code.renewOtp(otp, validDuration);
        verificationCodeRepository.save(code);
        mailServiceImpl.sendOtp(email, otp, MailTemplate.FORGOT_PASSWORD);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        User user =
                userRepo.findByEmail(email)
                        .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));
        VerificationCode code =
                verificationCodeRepository
                        .findByUserIdAndType(user.getId(), VerificationCodeTypeEnum.RESET_PASSWORD)
                        .orElseThrow(() -> new AppException(ErrorCode.OTP_DOES_NOT_EXIST));

        validateOtp(code, otp);
        code.setStatus(VerificationCodeStatusEnum.VERIFIED);
        user.setPassword(passwordEncoder.encode(newPassword));
        refreshTokenRepository.deleteByUserId(user.getId());
        userRepo.save(user);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    public TokenResponse refreshToken(String oldToken) {
        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(oldToken)
                        .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        if (DateTimeUtils.now().isAfter(refreshToken.getExpiresAt()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        User user = refreshToken.getUser();
        JwtClaimObject claim =
                JwtClaimObject.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .userId(user.getId())
                        .build();
        TokenResponse tokenResponse = generateAccessTokenAndRefreshToken(claim);
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .token(tokenResponse.getRefreshToken().getToken())
                        .expiresAt(
                                DateTimeUtils.toLocalDateTime(
                                        tokenResponse.getRefreshToken().getExpiresAt()))
                        .build());

        return tokenResponse;
    }
}
