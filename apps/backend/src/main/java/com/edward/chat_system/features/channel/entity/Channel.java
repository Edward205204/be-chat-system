package com.edward.chat_system.features.channel.entity;

import com.edward.chat_system.features.server.entity.Server;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Table(
        name = "channels",
        indexes = {@Index(name = "idx_channel_server_id", columnList = "server_id, createdAt, id")},
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_channel_name_server_id",
                    columnNames = {"name", "server_id"})
        })
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    Server server;

    @Column(nullable = false)
    String name;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    boolean isPrivate = false;
}
