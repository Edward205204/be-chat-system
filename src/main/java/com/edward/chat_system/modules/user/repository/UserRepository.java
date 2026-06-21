package com.edward.chat_system.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edward.chat_system.modules.user.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

}
