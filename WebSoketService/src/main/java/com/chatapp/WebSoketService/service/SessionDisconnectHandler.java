package com.chatapp.WebSoketService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import reactor.core.publisher.Mono;

@Service
public class SessionDisconnectHandler implements ApplicationListener<SessionDisconnectEvent> {
    @Autowired
    RedisService redisService;
    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = sha.getSessionId();

        System.out.printf("user disconnected sessionid : %s%n", sessionId);
        System.out.printf("close status : %s%n", event.getCloseStatus());
        redisService.removeSession(sessionId)
                .doOnSuccess(v -> System.out.println("Session cleaned up: " + sessionId))  // ← needs lambda
                .doOnError(e -> System.out.println("Cleanup failed: " + e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();

    }


}
