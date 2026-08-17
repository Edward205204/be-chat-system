package com.edward.chat_system.features.server.entity;

import com.edward.chat_system.features.server.enums.InviteStatusEnum;
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
    name = "server_invitations",
    indexes = @Index(name = "idx_invitation_invitee_server_status", columnList = "invitee_id, server_id, status"))
public class ServerInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    Server server;

    //    inviter_id VARCHAR(36) [not null, ref: > users.id]
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    User inviter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id", nullable = false)
    User invitee;

    // this field will be delete, user approve or reject will del this row
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    InviteStatusEnum status = InviteStatusEnum.PENDING;

    @Column(nullable = false)
    LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;
}
