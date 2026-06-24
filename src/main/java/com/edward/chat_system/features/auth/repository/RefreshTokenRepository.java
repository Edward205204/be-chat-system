package com.edward.chat_system.features.auth.repository;

import com.edward.chat_system.features.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    void deleteByUserId(String userId);

    void deleteByToken(String token);

    Optional<RefreshToken> findByToken(String token);
}
