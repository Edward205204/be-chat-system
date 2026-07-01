package com.edward.chat_system.features.server.repository;

import com.edward.chat_system.features.server.entity.ServerBan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerBanRepository extends JpaRepository<ServerBan, String> {
    @Modifying
    @Query(
            """
    DELETE FROM ServerBan sb WHERE sb.server.id = :serverId AND sb.user.id = :userId
""")
    void deleteByServerIdAndUserId(
            @Param("serverId") String serverId, @Param("userId") String userId);

    @Query(
            """
    SELECT sb FROM ServerBan sb
    JOIN FETCH sb.user
    JOIN FETCH sb.bannedBy
    WHERE sb.server.id = :serverId
    ORDER BY sb.createdAt DESC
""")
    Page<ServerBan> findByServerId(@Param("serverId") String serverId, Pageable pageable);
}
