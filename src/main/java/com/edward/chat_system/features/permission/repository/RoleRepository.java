package com.edward.chat_system.features.permission.repository;

import com.edward.chat_system.features.permission.entity.Role;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
    List<Role> findAllByServerId(String serverId);

    boolean existsByServerIdAndName(String serverId, String name);
}
