package com.edward.chat_system.infrastructure.security.jwt;

import com.edward.chat_system.features.channel.enums.ChannelPermissionKeyEnum;
import com.edward.chat_system.infrastructure.security.permission.RequiresChannelPermissionComponent;
import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtChannelInterceptor implements ExecutorChannelInterceptor {
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
                // JwtAuthenticationToken.getName() returns the subject (userId)
                accessor.setUser(new JwtAuthenticationToken(jwt));
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

    // @MessageMapping handlers run on clientInboundChannel worker threads, where the
    // SecurityContext is empty; expose the STOMP session user so CurrentUserProvider works.
    @Override
    public Message<?> beforeHandle(
            @NonNull Message<?> message,
            @NonNull MessageChannel channel,
            @NonNull MessageHandler handler) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && accessor.getUser() instanceof Authentication auth) {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        return message;
    }

    @Override
    public void afterMessageHandled(
            @NonNull Message<?> message,
            @NonNull MessageChannel channel,
            @NonNull MessageHandler handler,
            Exception ex) {
        SecurityContextHolder.clearContext();
    }

    private String extractUserId(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof JwtAuthenticationToken auth))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        return auth.getName();
    }

    private String extractChannelId(String destination) {
        if (destination == null) return null;
        String prefix = "/topic/channel/";
        if (!destination.startsWith(prefix)) return null;
        return destination.substring(prefix.length());
    }
}
