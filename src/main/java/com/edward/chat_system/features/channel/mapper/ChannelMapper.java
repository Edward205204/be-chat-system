package com.edward.chat_system.features.channel.mapper;

import com.edward.chat_system.features.channel.dto.request.ChannelPatchUpdateRequest;
import com.edward.chat_system.features.channel.entity.Channel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ChannelMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateChannelFromDto(ChannelPatchUpdateRequest dto, @MappingTarget Channel channel);
}
