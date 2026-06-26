package com.edward.chat_system.features.channel.repository;

import com.edward.chat_system.features.channel.entity.ChannelUserPermission;
import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelUserPermissionRepository
        extends JpaRepository<ChannelUserPermission, String> {
    @Query(
            """
    SELECT  COUNT(cup) > 0
    FROM ChannelUserPermission cup
    WHERE cup.serverMember.user.id = :memberId
    AND cup.channel.id = :channelId
    AND cup.permission = :permission
""")
    boolean hasPermission(
            @Param("memberId") String memberId,
            @Param("channelId") String channelId,
            @Param("permission") ChannelPermissionKeyEnum permission);
}
