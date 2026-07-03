package com.edward.chat_system.features.chat.repository;

import com.edward.chat_system.features.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, String> {}
