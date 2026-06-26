package com.edward.chat_system.features.server.repository;

import com.edward.chat_system.features.server.entity.ServerMember;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import com.edward.chat_system.features.server.projection.ServerMemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ServerMemberRepository extends JpaRepository<ServerMember, String> {
    @Query("""
            SELECT sm.id AS memberId,
                   sm.server.id AS serverId,
                   (s.user.id = :userId) AS isOwner
            FROM ServerMember sm
            JOIN Server s ON s.id = sm.server.id
            WHERE sm.server.id = :serverId
            AND sm.user.id = :userId
            """)
    Optional<ServerMemberInfo> findServerMemberInfo(
            @Param("serverId") String serverId,
            @Param("userId") String userId
    );

    @Query("""
    SELECT COUNT(srp) > 0
    FROM ServerRolePermission srp
    JOIN RoleMember rm ON rm.role.id = srp.role.id
    WHERE rm.serverMember.id = :memberId
    AND srp.permission = :permission
    """)
    boolean hasPermission(
            @Param("memberId") String memberId,
            @Param("permission") ServerPermissionKeyEnum permission
    );
}