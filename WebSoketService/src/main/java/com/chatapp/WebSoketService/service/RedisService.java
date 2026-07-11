package com.chatapp.WebSoketService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
@Slf4j
@Service
public class RedisService {
    @Autowired
    private ObjectMapper objectMapper;
    private final ReactiveValueOperations<String,Object> valueOperations;
    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;
    @Autowired
    @Qualifier("redisTemplate")
    private  RedisTemplate<String, String>  simpleredisTemplate;

    public RedisService(@Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String,Object> redisTemplate) {
        this.valueOperations = redisTemplate.opsForValue();
    }

    public Mono<Boolean> add(String key , Object val){
        log.info("adding to rdis store.......");
        return valueOperations.set(key,val);

    }

    public <T> Mono<T> getval(String key, Class<T> type) {
        return valueOperations
                .get(key)
                .map(res -> objectMapper.convertValue(res, type));  // ← convert here
    }
    // add user — stores both directions
    public Mono<Boolean> addUser(String userId, String sessionId) {
        return redisTemplate.opsForHash()
                .putIfAbsent("users", userId, sessionId)
                .flatMap(result->
                {
                    if(result){
                       return redisTemplate.opsForHash()
                                .put("sessions", sessionId, userId);
                    }
                    return Mono.just(false);
                })
                .onErrorResume(e -> {
                    System.out.println("Redis addUser failed: " + e.getMessage());
                    return Mono.just(false);
                });
    }

    // get sessionId by userId
    public Mono<String> getSessionByUser(String userId) {
        return redisTemplate.opsForHash()
                .get("users", userId)
                .map(Object::toString)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found: " + userId)))
                .onErrorResume(e -> {
                    System.out.println("Redis getSessionByUser failed: " + e.getMessage());
                    return Mono.empty();
                });
    }
    public Boolean isSessionPresent(String sessionId){
        return simpleredisTemplate.opsForHash()
                .hasKey("sessions", sessionId);
    }
    // get userId by sessionId
    public Mono<String> getUserBySession(String sessionId) {
        return redisTemplate.opsForHash()
                .get("sessions", sessionId)
                .map(Object::toString)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found: " + sessionId)))
                .onErrorResume(e -> {
                    System.out.println("Redis getUserBySession failed: " + e.getMessage());
                    return Mono.empty();
                });
    }

    // check if userId exists
    public Mono<Boolean> userExists(String userId) {
        return redisTemplate.opsForHash()
                .hasKey("users", userId)
                .onErrorResume(e -> {
                    System.out.println("Redis userExists failed: " + e.getMessage());
                    return Mono.just(false);
                });
    }

    // remove session — cleans both directions
    public Mono<Void> removeSession(String sessionId) {
        return getUserBySession(sessionId)
                .flatMap(userId ->
                        redisTemplate.opsForHash().remove("users", userId)
                                .then(redisTemplate.opsForHash()
                                        .remove("sessions", sessionId))
                )
                .switchIfEmpty(Mono.fromRunnable(() ->
                        System.out.println("Session already removed: " + sessionId)
                ))
                .onErrorResume(e -> {
                    System.out.println("Redis removeSession failed: " + e.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}
