package com.dealharbor.dealharbor_backend.config;

import com.dealharbor.dealharbor_backend.security.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * WebSocket Configuration for Real-Time Messaging
 * Optimized for production with connection pooling, buffering, and performance tuning
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory broker for topic/queue messaging
        // For production scaling, consider using RabbitMQ or Redis
        registry.enableSimpleBroker(
                "/topic",  // For broadcasting (typing indicators, presence)
                "/queue"   // For direct user messages
        )
        .setHeartbeatValue(new long[]{10000, 10000}) // 10s heartbeat
        .setTaskScheduler(taskScheduler()); // Use task scheduler for heartbeat
        
        // Application destination prefix for client messages
        registry.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint with SockJS fallback for older browsers
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:3000", "http://127.0.0.1:3000", "https://*")
                .withSockJS()
                .setHeartbeatTime(25000) // 25s heartbeat
                .setDisconnectDelay(5000) // 5s disconnect delay
                .setStreamBytesLimit(512 * 1024) // 512KB message size limit
                .setHttpMessageCacheSize(1000) // Cache up to 1000 messages
                .setSessionCookieNeeded(true); // Enable session cookies for auth
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add authentication interceptor
        registration.interceptors(webSocketAuthInterceptor);
        
        // Configure thread pool for inbound messages
        registration.taskExecutor()
                .corePoolSize(8)          // Minimum threads
                .maxPoolSize(16)          // Maximum threads
                .queueCapacity(1000)      // Queue size
                .keepAliveSeconds(60);    // Thread keep-alive
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Configure thread pool for outbound messages
        registration.taskExecutor()
                .corePoolSize(8)
                .maxPoolSize(16)
                .queueCapacity(1000)
                .keepAliveSeconds(60);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Performance tuning for WebSocket transport
        registration
                .setMessageSizeLimit(128 * 1024)      // 128KB per message
                .setSendBufferSizeLimit(512 * 1024)   // 512KB send buffer
                .setSendTimeLimit(20 * 1000)          // 20s send timeout
                .setTimeToFirstMessage(30 * 1000);    // 30s handshake timeout
    }
}
