package com.edward.chat_system.features.server.entity;

import com.edward.chat_system.features.user.entity.User;
import com.edward.chat_system.shared.utils.DateTimeUtils;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Table(
        name = "invite_links",
        indexes = {
            @Index(
                    name = "idx_invite_server_created",
                    columnList = "server_id, created_at DESC, id DESC")
        })
public class InviteLink {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false, unique = true)
    String token;

    @Column(nullable = false)
    @Builder.Default
    boolean isRevoked = false;

    @Column(nullable = false)
    @Builder.Default
    int useCount = 0;

    @Column(nullable = false)
    LocalDateTime expiresAt; // [not null, note: 'created_at + 3 days']

    @Column(nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = DateTimeUtils.now();
        }

        if (expiresAt == null) {
            expiresAt = createdAt.plusDays(3);
        }
    }
}
