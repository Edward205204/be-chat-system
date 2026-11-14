package com.edward.chat_system.features.permission.repository;

import com.edward.chat_system.features.permission.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, String> {
    List<Role> findAllByServerId(String serverId);

    boolean existsByServerIdAndName(String serverId, String name);

    Optional<Role> findByIdAndServerId(String id, String serverId);

    @Modifying
    @Query(
            """
    DELETE FROM Role r WHERE r.id = :roleId
""")
    void deleteByRoleId(@Param("roleId") String roleId);
}
