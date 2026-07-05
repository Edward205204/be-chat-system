package com.edward.chat_system.features.server.repository;

import com.edward.chat_system.features.server.entity.ServerMember;
import com.edward.chat_system.features.server.projection.MemberProjection;
import com.edward.chat_system.features.server.projection.ServerMemberInfoProjection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerMemberRepository extends JpaRepository<ServerMember, String> {
    @Query(
            """
            SELECT sm.user.id AS userId,
                   sm.server.id AS serverId,
                   (s.user.id = :userId) AS isOwner
            FROM ServerMember sm
            JOIN Server s ON s.id = sm.server.id
            WHERE sm.server.id = :serverId
            AND sm.user.id = :userId
            """)
    Optional<ServerMemberInfoProjection> findServerMemberInfo(
            @Param("serverId") String serverId, @Param("userId") String userId);

    boolean existsByServerIdAndUserId(String serverId, String userId);

    boolean existsByIdAndServerId(String memberId, String serverId);

    @Query(
            """
                SELECT
                    sm.id as memberId,
                    u.id as userId,
                    u.displayName as displayName,
                    u.username as username,
                    u.avatar as avatar,
                    sm.isMuted as isMuted,
                    sm.joinedAt as joinedAt,
                    s.user.id as ownerId
                FROM ServerMember sm
                JOIN sm.user u
                JOIN sm.server s
                WHERE sm.server.id = :serverId
                ORDER BY sm.joinedAt ASC, sm.id ASC
            """)
    Page<MemberProjection> findMembersByServerId(
            @Param("serverId") String serverId, Pageable pageable);

    @Modifying
    @Query(
            """
    DELETE FROM ServerMember sm WHERE sm.id = :memberId AND sm.server.id = :serverId
""")
    void deleteByIdAndServerId(
            @Param("memberId") String memberId, @Param("serverId") String serverId);

    @Modifying
    @Query(
            """
DELETE FROM ServerMember sm WHERE sm.server.id = :serverId AND sm.user.id = :userId
""")
    void deleteByServerIdAndUserId(
            @Param("serverId") String serverId, @Param("userId") String userId);

    @Modifying
    @Query(
            """
    UPDATE ServerMember sm
    SET sm.isMuted = :mute
    WHERE sm.id = :memberId
    AND sm.server.id = :serverId
""")
    void muteOrUnmuteServerMember(
            @Param("serverId") String serverId,
            @Param("memberId") String memberId,
            @Param("mute") boolean mute);

    //    OPTIMIZATION
    Optional<ServerMember> findByIdAndServerId(String memberId, String serverId);
}
