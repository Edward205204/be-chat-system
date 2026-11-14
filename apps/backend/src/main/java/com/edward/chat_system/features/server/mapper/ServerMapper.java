package com.edward.chat_system.features.server.mapper;

import com.edward.chat_system.features.server.dto.request.ServerPatchUpdateRequest;
import com.edward.chat_system.features.server.dto.response.ServerPatchUpdateResponse;
import com.edward.chat_system.features.server.entity.Server;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ServerMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateServerFromDto(ServerPatchUpdateRequest dto, @MappingTarget Server server);

    ServerPatchUpdateResponse toServerUpdateResponse(Server server);
}
