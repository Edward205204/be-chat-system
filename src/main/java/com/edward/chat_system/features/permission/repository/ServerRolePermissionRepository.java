package com.edward.chat_system.features.permission.repository;

import com.edward.chat_system.features.permission.entity.ServerRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRolePermissionRepository extends JpaRepository<ServerRolePermission, String> {
}