package com.edward.chat_system.modules.auth.service;

import com.edward.chat_system.infrastructure.jwt.JwtClaimObject;
import com.edward.chat_system.infrastructure.jwt.JwtSigner;
import com.edward.chat_system.infrastructure.jwt.JwtSignerResponse;
import com.edward.chat_system.infrastructure.mail.MailServiceImpl;
import com.edward.chat_system.modules.auth.dto.request.LoginRequest;
import com.edward.chat_system.modules.auth.dto.request.RegisterRequest;
import com.edward.chat_system.modules.auth.dto.response.AuthResponse;
import com.edward.chat_system.modules.auth.dto.response.AuthSuccessResponse;
import com.edward.chat_system.modules.auth.dto.response.UnverifiedResponse;
import com.edward.chat_system.modules.auth.entity.RefreshToken;
import com.edward.chat_system.modules.auth.entity.VerificationCode;
import com.edward.chat_system.modules.auth.enums.VerificationCodeStatusEnum;
import com.edward.chat_system.modules.auth.exception.OtpCooldownException;
import com.edward.chat_system.modules.auth.repository.RefreshTokenRepository;
import com.edward.chat_system.modules.auth.repository.VerificationCodeRepository;
import com.edward.chat_system.modules.user.entity.User;
import com.edward.chat_system.modules.user.mapper.UserMapper;
import com.edward.chat_system.modules.user.repository.UserRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    UnverifiedResponse unverifiedResponseBuilder(JwtClaimObject claim) {
        claim.toBuilder().tokenType(TokenTypeEnum.TMP_TOKEN).build();
        String tmpTokenString = jwtSigner.generateToken(claim).getToken();
        return UnverifiedResponse.builder().tmpToken(tmpTokenString).build();
    }

    public AuthResponse login(LoginRequest request) {
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
        JwtSignerResponse accessToken =
                jwtSigner.generateToken(
                        claim.toBuilder().tokenType(TokenTypeEnum.ACCESS_TOKEN).build());

        JwtSignerResponse refreshToken =
                jwtSigner.generateToken(
                        claim.toBuilder().tokenType(TokenTypeEnum.REFRESH_TOKEN).build());
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .token(refreshToken.getToken())
                        .expiresAt(DateTimeUtils.toLocalDateTime(refreshToken.getExpiresAt()))
                        .build());
        return AuthSuccessResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .user(userMapper.touUserResponse(user))
                .build();
    }

    public UnverifiedResponse register(RegisterRequest request) {
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

    private void validateCanSendCode(VerificationCode code) {
        VerificationCodeStatusEnum codeStatus = code.getStatus();
        if (codeStatus == VerificationCodeStatusEnum.VERIFIED)
            throw new AppException(ErrorCode.ACCOUNT_VERIFIED);
        if ((codeStatus == VerificationCodeStatusEnum.PENDING)
                || (codeStatus == VerificationCodeStatusEnum.REVOKED)) validateCoolDown(code);
    }

    private void validateCoolDown(VerificationCode code) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(code.getLastSentAt().plusSeconds(cooldown)))
            throw new OtpCooldownException(ErrorCode.OTP_COOLDOWN, code.getLastSentAt());
    }

    @Retryable(value = MailException.class, maxRetries = 3, delay = 500)
    public void sendOtp(String userId, String email) {
        VerificationCode code =
                verificationCodeRepository.findByUserId(userId).orElseGet(VerificationCode::new);
        if (code.getStatus() != null) validateCanSendCode(code);
        String otp = OtpUtils.generateOtp();

        code.setUser(userRepo.getReferenceById(userId));
        code.renewOtp(otp, validDuration);

        verificationCodeRepository.save(code);
        mailServiceImpl.sendOtp(email, otp);
    }
}
