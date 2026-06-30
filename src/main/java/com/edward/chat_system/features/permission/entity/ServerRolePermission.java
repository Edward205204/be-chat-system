package com.edward.chat_system.features.permission.entity;

import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(
        name = "server_role_permission",
        indexes = {@Index(name = "idx_srp_permission_role", columnList = "permission, role_id")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "permission"})})
public class ServerRolePermission {
    @Id
    @GeneratedValue(generator = "UUID")
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ServerPermissionKeyEnum permission;
}
