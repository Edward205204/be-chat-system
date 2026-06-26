package com.edward.chat_system.features.channel.repository;

import com.edward.chat_system.features.channel.entity.Channel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelRepository extends JpaRepository<Channel, String> {
    @Query("SELECT c.server.id FROM Channel c WHERE c.id = :channelId")
    Optional<String> findServerIdByChannelId(@Param("channelId") String channelId);
}
