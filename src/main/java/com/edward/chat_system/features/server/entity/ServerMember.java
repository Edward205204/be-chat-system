package com.edward.chat_system.features.server.entity;

import com.edward.chat_system.features.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Getter
@Setter
@Table(
        name = "server_members",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"server_id", "user_id"})},
        indexes = {@Index(name = "idx_server_members_user_id", columnList = "user_id")})
public class ServerMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    Server server;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime joinedAt;

    @Column(nullable = false)
    @Builder.Default
    boolean isMuted = false;
}
