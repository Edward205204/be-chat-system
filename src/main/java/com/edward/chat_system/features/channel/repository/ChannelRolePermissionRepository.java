package com.edward.chat_system.features.channel.repository;

import com.edward.chat_system.features.channel.entity.ChannelRolePermission;
import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelRolePermissionRepository
        extends JpaRepository<ChannelRolePermission, String> {
    @Query(
            """
            SELECT COUNT(crp) > 0
            FROM ChannelRolePermission crp
            JOIN RoleMember rm ON rm.role.id = crp.role.id
            WHERE rm.serverMember.user.id = :memberId
            AND crp.permission = :permission
            """)
    boolean hasPermission(
            @Param("memberId") String memberId,
            @Param("permission") ChannelPermissionKeyEnum permission);
}
