package com.edward.chat_system.features.permission.entity;

import com.edward.chat_system.features.server.entity.ServerMember;
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
        name = "role_members",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "server_member_id"})})
public class RoleMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_member_id", nullable = false)
    ServerMember serverMember;
}
