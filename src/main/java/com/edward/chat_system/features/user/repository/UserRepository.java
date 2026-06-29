package com.edward.chat_system.features.user.repository;

import com.edward.chat_system.features.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query(
            """
            SELECT u FROM User u
            WHERE (u.username = :q OR u.email = :q)
              AND u.id <> :currentUserId
            """)
    Optional<User> searchByUsernameOrEmail(
            @Param("q") String q, @Param("currentUserId") String currentUserId);
}
