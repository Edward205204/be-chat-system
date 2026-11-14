package com.edward.chat_system.features.channel.repository;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.features.permission.entity.ChannelRolePermission;
import com.edward.chat_system.features.permission.projection.RolePermissionRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelRolePermissionRepository
        extends JpaRepository<ChannelRolePermission, String> {
    @Query(
            """
            SELECT COUNT(crp) > 0
            FROM ChannelRolePermission crp
            JOIN RoleMember rm ON rm.role.id = crp.role.id
            WHERE rm.serverMember.user.id = :userId
            AND crp.permission = :permission
            """)
    boolean hasPermission(
            @Param("userId") String userId,
            @Param("permission") ChannelPermissionKeyEnum permission);

    @Query(
            """
            SELECT
                crp.role.id        AS roleId,
                crp.role.name      AS roleName,
                crp.permission     AS permission
            FROM ChannelRolePermission crp
            WHERE crp.channel.id = :channelId
            """)
    List<RolePermissionRow> findRolePermissionsByChannelId(@Param("channelId") String channelId);

    @Query(
            """
    SELECT COUNT(crp) > 0 FROM ChannelRolePermission crp
    WHERE crp.channel.id = :channelId
    AND crp.role.id = :roleId
    AND crp.permission = :permission
""")
    boolean existsByUniqueConstraint(
            @Param("channelId") String channelId,
            @Param("roleId") String roleId,
            @Param("permission") ChannelPermissionKeyEnum permission);

    @Modifying
    @Query(
            """
    DELETE FROM ChannelRolePermission crp WHERE crp.channel.id = :channelId
    AND crp.role.id = :roleId AND crp.permission = :permission
""")
    void deleteByUniqueConstraint(
            @Param("channelId") String channelId,
            @Param("roleId") String roleId,
            @Param("permission") ChannelPermissionKeyEnum permission);

    @Modifying
    @Query(
            """
    DELETE FROM ChannelRolePermission crp
    WHERE crp.role.id = :roleId
    AND crp.channel.id = :channelId
""")
    void deleteManyByRoleIdAndChannelId(
            @Param("roleId") String roleId, @Param("channelId") String channelId);
}
