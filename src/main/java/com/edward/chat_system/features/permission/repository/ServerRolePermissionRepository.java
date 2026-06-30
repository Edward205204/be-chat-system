package com.edward.chat_system.features.permission.repository;

import com.edward.chat_system.features.permission.entity.ServerRolePermission;
import com.edward.chat_system.features.permission.projection.PermissionNameProjection;
import com.edward.chat_system.features.server.enums.ServerPermissionKeyEnum;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerRolePermissionRepository
        extends JpaRepository<ServerRolePermission, String> {
    @Query(
            """
            SELECT COUNT(srp) > 0
            FROM ServerRolePermission srp
            JOIN srp.role r
            WHERE srp.permission = :permission
            AND (
            r.isDefault = true AND r.server.id = :serverId
            OR EXISTS (
                SELECT 1 FROM RoleMember rm
                WHERE rm.role.id = r.id
                AND rm.serverMember.user.id = :memberId
                        )
               )
            """)
    boolean hasPermission(
            @Param("serverId") String serverId,
            @Param("memberId") String memberId,
            @Param("permission") ServerPermissionKeyEnum permission);

    @Query(
            "SELECT s.permission as permission FROM ServerRolePermission s WHERE s.role.id ="
                    + " :roleId")
    List<PermissionNameProjection> findPermissionsByRoleId(@Param("roleId") String roleId);

    @Modifying
    @Query("DELETE FROM ServerRolePermission s WHERE s.role.id = :roleId")
    void deleteByRole_Id(@Param("roleId") String roleId);

    @Modifying
    @Query(
            "DELETE FROM ServerRolePermission s WHERE s.role.id = :roleId AND s.permission ="
                    + " :permission")
    void deletePermission(
            @Param("roleId") String roleId,
            @Param("permission") ServerPermissionKeyEnum permission);

    boolean existsByRoleIdAndPermission(String roleId, ServerPermissionKeyEnum permission);
}
