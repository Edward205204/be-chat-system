package com.edward.chat_system.infrastructure.configuration;

import com.edward.chat_system.infrastructure.security.jwt.JwtChannelInterceptor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    JwtChannelInterceptor jwtChannelInterceptor;
    String frontendUrl;

    public WebSocketConfig(
            JwtChannelInterceptor jwtChannelInterceptor,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Browsers send an Origin header; without this the handshake from the FE is rejected (403)
        registry.addEndpoint("/ws").setAllowedOriginPatterns(frontendUrl);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.setApplicationDestinationPrefixes("/app");

        registry.enableSimpleBroker("/topic", "/queue");

        registry.setUserDestinationPrefix("/user");
    }
}
