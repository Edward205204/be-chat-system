package com.edward.chat_system.features.server.entity;

import com.edward.chat_system.features.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

// indexes = {@Index(name = "idx_srp_permission_role", columnList = "permission, role_id")},
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Table(
        name = "servers",
        indexes = {@Index(name = "idx_servers_owner_id_name", columnList = "owner_id, name")})
public class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    User user;

    @Column(nullable = false)
    String name;

    String avatar;
    String banner;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;
}
