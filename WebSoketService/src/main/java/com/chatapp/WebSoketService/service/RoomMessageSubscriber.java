package com.chatapp.WebSoketService.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RoomMessageSubscriber {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    @Qualifier("reactiveRedisTemplate")
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void subscribe() {
        redisTemplate.listenToPattern("room:*")  // ← annotation-like ✅
                .doOnNext(message -> {
                    String roomId = message.getChannel().replace("room:", "");
                    String body = message.getMessage().toString();

                    System.out.printf("Room: %s Message: %s%n", roomId, body);

                    messagingTemplate.convertAndSend("/topic/room/" + roomId, body);
                })
                .doOnError(e -> System.out.println("Redis error: " + e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
}