package com.chatapp.WebSoketService.service;

import com.chatapp.WebSoketService.Model.GroupMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RoomMessagePublisher {

    @Autowired
    @Qualifier("reactiveRedisTemplate")
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RoomManagementService roomManagementService;

    @Autowired
    private ProducerService producerService;

    public void publish(String roomId, String message) {

        log.info("inside publish to redis.......");
        redisTemplate.convertAndSend("room:" + roomId, message)  // ← reactive publish ✅
                .doOnSuccess(count ->
                        System.out.printf("Published to room: %s, subscribers: %d%n", roomId, count))
                .doOnError(e ->
                        System.out.println("Publish failed: " + e.getMessage()))
                .onErrorResume(e -> Mono.just(0L))
                .subscribe();
    }

    public Mono<String> publishMessage(GroupMessage message, String sessionId) {
        return redisService.getUserBySession(sessionId)
                .flatMap(userId -> roomManagementService.isUserInRoom(message.roomID(), userId)
                        .flatMap(inRoom -> {
                            if (inRoom) {
                                GroupMessage verifiedMessage = new GroupMessage(userId, message.msg(), message.roomID());
                                return Mono.fromFuture(producerService.publishToGroup(verifiedMessage));
                            } else {
                                log.warn("User {} is not in room {}", userId, message.roomID());
                                return Mono.just("ERROR: User is not a member of room " + message.roomID());
                            }
                        })
                )
                .switchIfEmpty(Mono.just("ERROR: User session not found"));
    }
}
