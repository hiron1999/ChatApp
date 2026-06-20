package com.chatapp.WebSoketService.configration;

import com.chatapp.WebSoketService.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer  {
    @Autowired
    CustomHandshakeInterceptor customHandshakeInterceptor;
    @Autowired
    WebSocketSessionRegistry sessionRegistry;
    @Autowired
    RedisService redisService;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic","/queue","/user","/system");
        registry.setApplicationDestinationPrefixes("/chat");
        registry.setUserDestinationPrefix("/user");


    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
                .addInterceptors(customHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler ->  new SessionRegistryDecorator(handler, sessionRegistry));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String sessionId = accessor.getSessionId();
                    String destination = accessor.getDestination();

                    System.out.println("SUBSCRIBE attempt: " + destination);

                    //default chanel
                    if(destination.contains(sessionId)){
                        return message;
                    }
                    // ✅ RULE 1 — block rejected sessions
                    if (sessionRegistry.isRejected(sessionId)) {
                        log.warn("Blocked rejected session: {} from subscribing to {}", sessionId, destination);
                        return null;
                    }

                    // ✅ RULE 2 — only allow specific destinations
                    if (!isAllowedDestination(destination)) {
                        log.warn("Blocked unauthorized destination: {}", destination);
                        return null;
                    }

                    // ✅ RULE 3 — user can only subscribe to their own channels
                    Boolean isRegistered = redisService.isSessionPresent(sessionId); // need to store redis cache

                    if (!isRegistered) {
                        log.warn("session is not registered in Redis", sessionId, destination);
                        return null;
                    }
                }

                return message;
            }
        });
    }

    // allowed destination patterns
    private boolean isAllowedDestination(String destination) {
        return destination.startsWith("/topic")
                || destination.startsWith("/user/")
                || destination.startsWith("/chat");
    }
}
