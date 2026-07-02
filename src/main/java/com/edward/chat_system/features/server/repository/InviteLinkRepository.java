package com.edward.chat_system.features.server.repository;

import com.edward.chat_system.features.server.entity.InviteLink;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InviteLinkRepository extends JpaRepository<InviteLink, String> {

    @Query(
            """
            SELECT i FROM InviteLink i
            JOIN FETCH i.user
            WHERE i.server.id = :serverId
            ORDER BY i.createdAt DESC, i.id DESC
            LIMIT :limit
            """)
    List<InviteLink> findFirstPage(@Param("serverId") String serverId, @Param("limit") int limit);

    @Query(
            """
            SELECT i FROM InviteLink i
            JOIN FETCH i.user
            WHERE i.server.id = :serverId
              AND (i.createdAt < :cursorCreatedAt
                   OR (i.createdAt = :cursorCreatedAt AND i.id < :cursorId))
            ORDER BY i.createdAt DESC, i.id DESC
            LIMIT :limit
            """)
    List<InviteLink> findNextPage(
            @Param("serverId") String serverId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") String cursorId,
            @Param("limit") int limit);

    @Modifying
    @Query(
            """
            DELETE FROM InviteLink il WHERE il.id = :inviteLinkId AND il.server.id = :serverId
            """)
    void deleteByIdAndServerId(
            @Param("inviteLinkId") String inviteLinkId, @Param("serverId") String serverId);

    // AFTER OPTIMIZATION
    @Query(
            """
    SELECT il FROM InviteLink il
    JOIN FETCH il.server
    WHERE il.token = :token
    AND il.isRevoked = false
    AND CURRENT_TIMESTAMP <  il.expiresAt
""")
    Optional<InviteLink> findValidInviteLinkByToken(@Param("token") String token);
}
