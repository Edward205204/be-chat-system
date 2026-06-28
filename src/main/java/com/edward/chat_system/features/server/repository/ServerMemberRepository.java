package com.edward.chat_system.features.server.repository;

import com.edward.chat_system.features.server.entity.ServerMember;
import com.edward.chat_system.features.server.projection.ServerMemberInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerMemberRepository extends JpaRepository<ServerMember, String> {
    @Query(
            """
            SELECT sm.user.id AS memberId,
                   sm.server.id AS serverId,
                   (s.user.id = :userId) AS isOwner
            FROM ServerMember sm
            JOIN Server s ON s.id = sm.server.id
            WHERE sm.server.id = :serverId
            AND sm.user.id = :userId
            """)
    Optional<ServerMemberInfo> findServerMemberInfo(
            @Param("serverId") String serverId, @Param("userId") String userId);

    boolean existsByServerIdAndUserId(String serverId, String userId);

    boolean existsByIdAndServerId(String memberId, String serverId);
}
