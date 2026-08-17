package com.edward.chat_system.features.server.repository;

import com.edward.chat_system.features.server.entity.ServerInvitation;
import com.edward.chat_system.features.server.enums.InviteStatusEnum;
import com.edward.chat_system.features.server.projection.UserDirectInviteProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerInvitationRepository extends JpaRepository<ServerInvitation, String> {
    Optional<ServerInvitation> findByInviteeIdAndServerIdAndInviterId(
            String inviteeId, String serverId, String inviterId);

    Optional<ServerInvitation> findByIdAndServerIdAndInviterId(
            String id, String serverId, String inviterId);

    Optional<ServerInvitation> findByIdAndInviteeId(String id, String inviteeId);

    @Modifying
    @Query(
            """
            DELETE FROM ServerInvitation si WHERE si.id = :id
            """)
    void deleteDirectInviteById(@Param("id") String id);

    @Query(
            """
                SELECT si.id as id,
                s.id as serverId,
                s.name as serverName,
                s.avatar as serverAvatar,
                u.id as inviterId,
                u.username as inviterUserName,
                u.displayName as inviterDisplayName,
                u.avatar as inviterAvatar,
                si.status as status,
                si.expiresAt as expiresAt,
                si.createdAt as createdAt
                FROM ServerInvitation si
                JOIN User u ON u.id = si.inviter.id
                JOIN Server s ON s.id = si.server.id
                WHERE si.invitee.id = :inviteeId
            """)
    List<UserDirectInviteProjection> findUserInvitationByInviteeId(
            @Param("inviteeId") String inviteeId);

    @Modifying
    @Query(
            """
    UPDATE ServerInvitation si SET si.status = :status WHERE si.invitee.id = :inviteeId AND si.server.id = :serverId AND si.status =
    com.edward.chat_system.features.server.enums.InviteStatusEnum.PENDING
""")
    int updateAllPendingByInviteeIdAndServerId(
            @Param("status") InviteStatusEnum status,
            @Param("inviteeId") String inviteeId,
            @Param("serverId") String serverId);

    @Modifying
    @Query(
            """
            UPDATE ServerInvitation si SET si.status = :status WHERE si.id = :id
            """)
    int updateStatusById(@Param("id") String id, @Param("status") InviteStatusEnum status);
}
