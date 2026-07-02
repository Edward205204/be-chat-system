package com.edward.chat_system.features.channel.repository;

import com.edward.chat_system.features.channel.entity.Channel;
import com.edward.chat_system.features.channel.projection.ChannelInfoRaw;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelRepository extends JpaRepository<Channel, String> {
    @Query("SELECT c.server.id FROM Channel c WHERE c.id = :channelId")
    Optional<String> findServerIdByChannelId(@Param("channelId") String channelId);

    Optional<Channel> findByIdAndServerId(String id, String serverId);

    boolean existsByNameAndServer_Id(String name, String serverId);

    @Query(
            """
            SELECT c.id        AS channelId,
                   c.name      AS name,
                   c.isPrivate AS isPrivate,
                   c.createdAt AS createdAt
            FROM Channel c
            JOIN c.server s
            LEFT JOIN ServerMember sm
                ON sm.server.id = :serverId
                AND sm.user.id = :userId
            WHERE c.server.id = :serverId
              AND (
                    c.isPrivate = false
                    OR s.user.id = :userId
                    OR EXISTS (
                        SELECT 1 FROM ChannelUserPermission cup
                        WHERE cup.channel.id = c.id
                          AND cup.serverMember.id = sm.id
                    )
                    OR EXISTS (
                        SELECT 1 FROM ChannelRolePermission crp
                        JOIN RoleMember rm
                            ON rm.role.id = crp.role.id
                        WHERE crp.channel.id = c.id
                          AND rm.serverMember.id = sm.id
                    )
                  )
            ORDER BY c.createdAt ASC, c.id ASC
            LIMIT :limit
            """)
    List<ChannelInfoRaw> findFirstPageVisibleChannels(
            @Param("serverId") String serverId,
            @Param("userId") String userId,
            @Param("limit") int limit);

    @Query(
            """
            SELECT c.id        AS channelId,
                   c.name      AS name,
                   c.isPrivate AS isPrivate,
                   c.createdAt AS createdAt
            FROM Channel c
            JOIN c.server s
            LEFT JOIN ServerMember sm
                ON sm.server.id = :serverId
                AND sm.user.id = :userId
            WHERE c.server.id = :serverId
              AND (
                    :cursor IS NULL
                    OR c.createdAt > :cursorCreatedAt
                    OR (c.createdAt = :cursorCreatedAt AND c.id > :cursorId)
                  )
              AND (
                    c.isPrivate = false
                    OR s.user.id = :userId
                    OR EXISTS (
                        SELECT 1 FROM ChannelUserPermission cup
                        WHERE cup.channel.id = c.id
                          AND cup.serverMember.id = sm.id
                    )
                    OR EXISTS (
                        SELECT 1 FROM ChannelRolePermission crp
                        JOIN  RoleMember rm
                            ON rm.role.id = crp.role.id
                        WHERE crp.channel.id = c.id
                          AND rm.serverMember.id = sm.id
                    )
                  )
            ORDER BY c.createdAt ASC, c.id ASC
            LIMIT :limit
            """)
    List<ChannelInfoRaw> findVisibleChannelsByServerWithCursor(
            @Param("serverId") String serverId,
            @Param("userId") String userId,
            @Param("cursor") String cursor,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") String cursorId,
            @Param("limit") int limit);

    @Modifying
    @Query(
            """
    DELETE FROM Channel c WHERE c.id = :id AND c.server.id = :serverId
""")
    void deleteByIdAndServer_Id(@Param("id") String id, @Param("serverId") String serverId);
}
