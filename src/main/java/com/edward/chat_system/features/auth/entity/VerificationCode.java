package com.edward.chat_system.features.auth.entity;

import com.edward.chat_system.features.auth.enums.VerificationCodeStatusEnum;
import com.edward.chat_system.features.auth.enums.VerificationCodeTypeEnum;
import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.shared.utils.DateTimeUtils;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        name = "verification_codes",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_verification_user_type",
                    columnNames = {"user_id", "type"})
        })
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false, length = 6)
    String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VerificationCodeTypeEnum type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    VerificationCodeStatusEnum status = VerificationCodeStatusEnum.PENDING;

    @Column(nullable = false)
    @Builder.Default
    Integer attemptCount = 0;

    @Column(nullable = false)
    LocalDateTime expiresAt;

    @UpdateTimestamp
    @Column(nullable = false)
    LocalDateTime lastSentAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    public void renewOtp(String otp, long validDuration) {
        this.attemptCount = 0;
        this.status = VerificationCodeStatusEnum.PENDING;
        this.code = otp;
        this.expiresAt = DateTimeUtils.now().plusSeconds(validDuration);
    }
}
