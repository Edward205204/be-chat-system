package com.edward.chat_system.features.server.repository;

import com.edward.chat_system.features.server.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepository extends JpaRepository<Server, String> {}
