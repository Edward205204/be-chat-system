package com.edward.chat_system.modules.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edward.chat_system.modules.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
}
