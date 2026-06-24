package com.edward.chat_system.features.permission.entity;

import com.edward.chat_system.features.server.enums.ServerPermissionKey;
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
        uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "permission"})})
public class ServerRolePermission {
    //    id         VARCHAR(36)          [pk]
    //    role_id    VARCHAR(36)          [not null, ref: > roles.id]
    //    permission server_permission_key [not null]
    //
    //    Note: 'Unique theo (role_id, permission)'
    @Id
    @GeneratedValue(generator = "UUID")
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ServerPermissionKey permission;
}
