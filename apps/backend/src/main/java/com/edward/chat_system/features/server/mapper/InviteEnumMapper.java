package com.edward.chat_system.features.server.mapper;

import com.edward.chat_system.features.server.enums.InviteAction;
import com.edward.chat_system.features.server.enums.InviteStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;

@Mapper(componentModel = "spring")
public interface InviteEnumMapper {

    @ValueMapping(source = "ACCEPT", target = "ACCEPTED")
    @ValueMapping(source = "REJECT", target = "REJECTED")
    InviteStatusEnum map(InviteAction action);
}
