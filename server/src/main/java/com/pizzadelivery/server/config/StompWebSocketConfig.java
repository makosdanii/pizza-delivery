package com.pizzadelivery.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for opening STOMP based Websocket
 */
@Configuration
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/delivery")
                .setAllowedOrigins("http://127.0.0.1:5173")
                .withSockJS();
    }

    /**
     * @param registry configures mediator
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/delivery");
        registry.setApplicationDestinationPrefixes("/app");
    }
}