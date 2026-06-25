package com.edward.chat_system.features.channel.entity;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.features.permission.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Table(
        name = "channel_role_permissions",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"channel_id", "role_id", "permission"})
        })
public class ChannelRolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ChannelPermissionKeyEnum permission;
}
