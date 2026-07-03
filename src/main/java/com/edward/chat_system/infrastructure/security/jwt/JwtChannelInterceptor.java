package com.edward.chat_system.infrastructure.security.jwt;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.infrastructure.security.permission.RequiresChannelPermissionComponent;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {
    JwtDecoder jwtDecoder;
    RequiresChannelPermissionComponent permissionComponent;

    public JwtChannelInterceptor(
            @Qualifier("accessTokenDecoder") JwtDecoder jwtDecoder,
            RequiresChannelPermissionComponent permissionComponent) {
        this.jwtDecoder = jwtDecoder;
        this.permissionComponent = permissionComponent;
    }

    @Override
    @SuppressWarnings("java:S2638")
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Jwt jwt = jwtDecoder.decode(token);
                accessor.setUser(new UsernamePasswordAuthenticationToken(jwt, null, List.of()));
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String userId = extractUserId(accessor);
            String channelId = extractChannelId(accessor.getDestination());

            if (channelId == null) return message;

            String serverId = permissionComponent.resolveServerId(channelId);
            permissionComponent.check(userId, serverId, channelId, ChannelPermissionKeyEnum.NONE);
        }

        return message;
    }

    private String extractUserId(StompHeaderAccessor accessor) {
        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) accessor.getUser();
        if (auth == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        Jwt jwt = (Jwt) auth.getPrincipal();
        if (jwt == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        return jwt.getSubject();
    }

    private String extractChannelId(String destination) {
        if (destination == null) return null;
        String prefix = "/topic/channel/";
        if (!destination.startsWith(prefix)) return null;
        return destination.substring(prefix.length());
    }
}
