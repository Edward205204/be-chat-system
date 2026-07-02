package com.edward.chat_system.features.channel.service;

import com.edward.chat_system.features.channel.dto.response.ChannelResponse;
import com.edward.chat_system.features.channel.projection.ChannelInfoRaw;
import com.edward.chat_system.features.channel.repository.ChannelRepository;
import com.edward.chat_system.shared.dto.CursorPageResponse;
import com.edward.chat_system.shared.utils.CursorUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ChannelService {
    CursorUtils cursorUtils;
    ChannelRepository channelRepository;

    public CursorPageResponse<ChannelResponse> getChannelList(
            String serverId, String userId, String cursor, int size) {
        int fetchSize = size + 1;

        List<ChannelInfoRaw> channelInfoRaw;
        if (cursor == null || cursor.isBlank()) {
            channelInfoRaw =
                    channelRepository.findFirstPageVisibleChannels(serverId, userId, fetchSize);
        } else {
            CursorUtils.CursorPayload payload = cursorUtils.decode(cursor);
            channelInfoRaw =
                    channelRepository.findVisibleChannelsByServerWithCursor(
                            serverId, userId, cursor, payload.createdAt(), payload.id(), fetchSize);
        }

        boolean hasNext = channelInfoRaw.size() > size;
        List<ChannelInfoRaw> pageItems = hasNext ? channelInfoRaw.subList(0, size) : channelInfoRaw;

        String nextCursor = null;
        if (hasNext) {
            ChannelInfoRaw last = pageItems.getLast();
            nextCursor = cursorUtils.encode(last.getCreatedAt(), last.getChannelId());
        }

        return CursorPageResponse.<ChannelResponse>builder()
                .data(
                        pageItems.stream()
                                .map(
                                        item ->
                                                ChannelResponse.builder()
                                                        .name(item.getName())
                                                        .isPrivate(item.getIsPrivate())
                                                        .createdAt(item.getCreatedAt())
                                                        .build())
                                .toList())
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}
