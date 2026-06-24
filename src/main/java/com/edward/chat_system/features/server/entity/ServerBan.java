package com.edward.chat_system.features.server.entity;

import com.edward.chat_system.features.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Table(
        name = "server_bans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"server_id", "user_id"}))
public class ServerBan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banned_by", nullable = false)
    User bannedBy;

    String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;
}
