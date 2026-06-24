package com.edward.chat_system.features.permission.entity;

import com.edward.chat_system.features.server.entity.Server;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    Server server;

    @Length(min = 7, max = 7)
    String color;

    @Column(nullable = false) // true = role everyone
    boolean isEveryone;

    @Column(nullable = false)
    @Builder.Default
    int position = 0;

    @Column(nullable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    private void ensureEveryonePosition() {
        //        đảm bảo position = 0 cho role @everyone
        if (isEveryone) {
            position = 0;
        }
    }
}
