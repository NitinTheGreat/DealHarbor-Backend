package com.dealharbor.dealharbor_backend.security;

import com.dealharbor.dealharbor_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * WebSocket Authentication Interceptor
 * Authenticates WebSocket connections using session or token
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract authentication from headers
            String userId = accessor.getFirstNativeHeader("X-User-Id");
            String authToken = accessor.getFirstNativeHeader("Authorization");
            
            log.debug("WebSocket connection attempt - userId: {}, hasToken: {}", userId, authToken != null);
            
            // Authenticate user
            if (userId != null) {
                Authentication authentication = authenticateUser(userId);
                accessor.setUser(authentication);
                log.info("WebSocket authenticated for user: {}", userId);
            } else {
                log.warn("WebSocket connection without userId");
            }
        }
        
        return message;
    }

    private Authentication authenticateUser(String userId) {
        // In production, validate token or session here
        // For now, create basic authentication
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
