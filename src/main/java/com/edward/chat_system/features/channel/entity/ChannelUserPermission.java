package com.edward.chat_system.features.channel.entity;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKey;
import com.edward.chat_system.features.permission.enums.PermissionValue;
import com.edward.chat_system.features.server.entity.ServerMember;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(
        name = "channel_user_permissions",
        uniqueConstraints = {
            @UniqueConstraint(
                    columnNames = {"channel_id", "server_member_id", "permission"})
        })
public class ChannelUserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_member_id", nullable = false)
    ServerMember serverMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ChannelPermissionKey permission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PermissionValue value;
}
