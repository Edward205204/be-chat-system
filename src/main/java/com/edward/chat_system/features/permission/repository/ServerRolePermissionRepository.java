package com.edward.chat_system.features.permission.repository;

import com.edward.chat_system.features.permission.entity.ServerRolePermission;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerRolePermissionRepository
        extends JpaRepository<ServerRolePermission, String> {
    @Query(
            """
            SELECT COUNT(srp) > 0
            FROM ServerRolePermission srp
            JOIN RoleMember rm ON rm.role.id = srp.role.id
            WHERE rm.serverMember.user.id = :memberId
            AND srp.permission = :permission
            """)
    boolean hasPermission(
            @Param("memberId") String memberId,
            @Param("permission") ServerPermissionKeyEnum permission);
}
