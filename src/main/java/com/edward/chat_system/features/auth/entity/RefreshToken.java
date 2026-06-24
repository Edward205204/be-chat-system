package com.edward.chat_system.features.auth.entity;

import com.edward.chat_system.features.user.entity.User;
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

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        name = "refresh_tokens",
        indexes = {@Index(name = "idx_refreshtoken_user_id", columnList = "user_id")})
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // user_id FK users.id

    @Column(nullable = false, unique = true)
    String token;

    @Column(nullable = false)
    LocalDateTime expiresAt;

    @Column(nullable = false)
    @CreationTimestamp
    LocalDateTime createdAt;
}
