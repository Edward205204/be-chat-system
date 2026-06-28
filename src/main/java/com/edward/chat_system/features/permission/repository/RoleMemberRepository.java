package com.edward.chat_system.features.permission.repository;

import com.edward.chat_system.features.permission.entity.RoleMember;
import com.edward.chat_system.features.permission.projection.RoleMemberProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleMemberRepository extends JpaRepository<RoleMember, String> {
    @Query(
            """
   SELECT sm.id AS serverMemberId,
          u.id AS userId,
          u.displayName AS displayName,
          u.username AS username,
          u.avatar AS avatar
          FROM RoleMember rm
          JOIN rm.serverMember sm
          JOIN sm.user u
          WHERE rm.role.id = :roleId
""")
    List<RoleMemberProjection> findAllByRoleId(@Param("roleId") String roleId);

    @Modifying
    @Query(
            """
    DELETE FROM RoleMember rm WHERE rm.role.id = :roleId AND rm.serverMember.id = :memberId
""")
    void deleteByRoleIdAndMemberId(
            @Param("roleId") String roleId, @Param("memberId") String memberId);

    boolean existsByRoleIdAndServerMemberId(String roleId, String memberId);
}
