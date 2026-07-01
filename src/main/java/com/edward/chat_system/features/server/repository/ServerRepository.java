package com.edward.chat_system.features.server.repository;

import com.edward.chat_system.features.server.entity.Server;
import com.edward.chat_system.features.server.projection.ServerProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerRepository extends JpaRepository<Server, String> {

    @Query(
            """
              SELECT s.id as serverId,
              s.name as name,
              s.avatar as avatar,
            s.banner as banner,
            s.user.id as ownerId,
            sm.joinedAt as joinedAt
               FROM Server s
              JOIN ServerMember sm ON sm.server.id = s.id
              WHERE sm.user.id = :userId
            """)
    List<ServerProjection> findAllServerUserJoined(@Param("userId") String userId);

    boolean existsByUserIdAndName(String userId, String name);

    @Query(
            """
              SELECT s.id as serverId,
              s.name as name,
              s.avatar as avatar,
            s.banner as banner,
            s.user.id as ownerId,
            sm.joinedAt as joinedAt
               FROM Server s
              JOIN ServerMember sm ON sm.server.id = s.id
              WHERE sm.server.id = :serverId AND sm.user.id = :userId
            """)
    Optional<ServerProjection> findServerUserJoinedByServerIdAndUserId(
            @Param("serverId") String serverId, @Param("userId") String userId);

    @Modifying
    @Query(
            """
    UPDATE Server s
    SET s.user.id = (
        SELECT sm.user.id
        FROM ServerMember sm
        WHERE sm.id = :newOwnerMemberId
          AND sm.server.id = :serverId
    )
    WHERE s.id = :serverId
""")
    void updateOwnership(
            @Param("serverId") String serverId, @Param("newOwnerMemberId") String newOwnerMemberId);
}
