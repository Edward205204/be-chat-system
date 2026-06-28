package com.edward.chat_system.features.auth.repository;

import com.edward.chat_system.features.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    @Modifying
    @Query(
            """
    DELETE FROM RefreshToken r WHERE r.user.id = :userId
""")
    void deleteByUserId(@Param("userId") String userId);

    void deleteByToken(String token);

    Optional<RefreshToken> findByToken(String token);
}
