package com.edward.chat_system.features.chat.repository;

import com.edward.chat_system.features.chat.entity.Message;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, String> {

    @Query(
            """
                SELECT m FROM Message m
                JOIN FETCH m.sender
                WHERE m.channel.id = :channelId
                ORDER BY m.createdAt DESC, m.id DESC
                LIMIT :limit
            """)
    List<Message> findFirstPage(@Param("channelId") String channelId, @Param("limit") int limit);

    @Query(
            """
                SELECT m FROM Message m
                JOIN FETCH m.sender
                WHERE m.channel.id = :channelId
                AND (m.createdAt < :cursorCreatedAt OR (
                m.createdAt = :cursorCreatedAt AND m.id < :cursorId
                ))
                ORDER BY m.createdAt DESC, m.id DESC LIMIT :limit
            """)
    List<Message> findNextPage(
            @Param("channelId") String channelId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") String cursorId,
            @Param("limit") int limit);
}
