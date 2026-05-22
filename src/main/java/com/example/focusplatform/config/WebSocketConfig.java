package com.example.focusplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This is the URL Vite (React) will connect to in order to start the socket
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*") // Allows your React frontend to connect
                .withSockJS(); // Fallback protocol if raw websockets fail
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/topic" is for broadcasting messages (e.g., /topic/general or /topic/class/5)
        registry.enableSimpleBroker("/topic");

        // "/app" is the prefix for messages sent FROM React TO the Spring Boot server
        registry.setApplicationDestinationPrefixes("/app");
    }
}