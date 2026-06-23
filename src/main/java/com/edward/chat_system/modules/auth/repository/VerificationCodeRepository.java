package com.edward.chat_system.modules.auth.repository;

import com.edward.chat_system.modules.auth.entity.VerificationCode;
import com.edward.chat_system.modules.auth.enums.VerificationCodeTypeEnum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, String> {
    Optional<VerificationCode> findByUserId(String userId);

    Optional<VerificationCode> findByUserIdAndType(String userId, VerificationCodeTypeEnum type);
}
