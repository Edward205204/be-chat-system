package com.edward.chat_system.features.user.repository;

import com.edward.chat_system.features.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

}
