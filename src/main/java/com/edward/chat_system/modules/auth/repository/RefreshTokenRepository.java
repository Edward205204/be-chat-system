package com.edward.chat_system.modules.auth.repository;

import com.edward.chat_system.modules.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {}
