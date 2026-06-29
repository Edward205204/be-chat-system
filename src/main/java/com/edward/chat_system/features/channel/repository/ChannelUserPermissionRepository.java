package com.edward.chat_system.features.channel.repository;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.features.permission.entity.ChannelUserPermission;
import com.edward.chat_system.features.permission.projection.UserPermissionRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelUserPermissionRepository
        extends JpaRepository<ChannelUserPermission, String> {
    @Query(
            """
    SELECT  COUNT(cup) > 0
    FROM ChannelUserPermission cup
    WHERE cup.serverMember.user.id = :userId
    AND cup.channel.id = :channelId
    AND cup.permission = :permission
""")
    boolean hasPermission(
            @Param("userId") String userId,
            @Param("channelId") String channelId,
            @Param("permission") ChannelPermissionKeyEnum permission);

    @Query(
            """
            SELECT
                cup.serverMember.id          AS memberId,
                cup.serverMember.user.id     AS userId,
                cup.serverMember.user.displayName AS displayName,
                cup.permission               AS permission
            FROM ChannelUserPermission cup
            WHERE cup.channel.id = :channelId
            """)
    List<UserPermissionRow> findUserPermissionsByChannelId(@Param("channelId") String channelId);

    @Query(
            """
    SELECT COUNT(cup) > 0 FROM ChannelUserPermission cup
    WHERE cup.channel.id = :channelId
    AND cup.serverMember.id  = :memberId
    AND cup.permission = :permission
""")
    boolean existsByUniqueConstraint(
            @Param("channelId") String channelId,
            @Param("memberId") String memberId,
            @Param("permission") ChannelPermissionKeyEnum permission);

    @Modifying
    @Query(
            """
    DELETE FROM ChannelUserPermission cup
    WHERE cup.channel.id = :channelId
    AND cup.serverMember.id  = :memberId
    AND cup.permission = :permission
""")
    void deleteByUniqueConstraint(
            @Param("channelId") String channelId,
            @Param("memberId") String memberId,
            @Param("permission") ChannelPermissionKeyEnum permission);

    @Modifying
    @Query(
            """
    DELETE FROM ChannelUserPermission cup
    WHERE cup.channel.id = :channelId
    AND cup.serverMember.id = :memberId
""")
    void deleteManyByChannelIdAndMemberId(
            @Param("channelId") String channelId, @Param("memberId") String memberId);
}
