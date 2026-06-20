package com.chatapp.WebSoketService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoomManagementService {

    @Autowired
    @Qualifier("reactiveRedisTemplate")
    private ReactiveRedisTemplate<String, Object> redisTemplate;
    @Autowired
    RedisService redisService;
    private static final String ROOMS_KEY = "rooms";

    // ── Create room ─────────────────────────────────────────

    public Mono<Boolean> createRoom(String roomId, String sessionId) {
        return roomExists(roomId)
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Room {} already exists!", roomId);
                        return Mono.just(false);  // ← discard duplicate ❌
                    }
                    return redisService.getUserBySession(sessionId)
                            .flatMap(user -> redisTemplate.opsForHash()
                                    .put(ROOMS_KEY, roomId, user)  // rooms → {roomId: createdBy}
                                    .flatMap(success -> redisTemplate.opsForSet()
                                            .add("room-members:" + roomId, user)  // room-members:roomId → {userId}
                                            .map(count -> true)
                                    )
                                    .doOnSuccess(res -> log.info("Room {} created and joined by {}", roomId, user))
                                    .onErrorResume(e -> {
                                        log.error("Failed to create room {}: {}", roomId, e.getMessage());
                                        return Mono.just(false);
                                    })
                            )
                            .switchIfEmpty(Mono.just(false)); // handle case where session/user is not found
                });
    }

    // ── Check if room exists ─────────────────────────────────

    public Mono<Boolean> roomExists(String roomId) {
        return redisTemplate.opsForHash()
                .hasKey(ROOMS_KEY, roomId)
                .onErrorResume(e -> {
                    log.error("Failed to check room {}: {}", roomId, e.getMessage());
                    return Mono.just(false);
                });
    }

    // ── Delete room ──────────────────────────────────────────

    public Mono<Boolean> deleteRoom(String roomId) {
        return roomExists(roomId)
                .flatMap(exists -> {
                    if (!exists) {
                        log.warn("Room {} does not exist!", roomId);
                        return Mono.just(false);
                    }
                    return redisTemplate.opsForHash()
                            .remove(ROOMS_KEY, roomId)
                            .flatMap(count -> redisTemplate.delete("room-members:" + roomId)
                                    .map(delCount -> count > 0)
                            )
                            .doOnSuccess(res -> log.info("Room {} and its member set deleted", roomId))
                            .onErrorResume(e -> {
                                log.error("Failed to delete room {}: {}", roomId, e.getMessage());
                                return Mono.just(false);
                            });
                });
    }

    // ── Get all rooms ────────────────────────────────────────

    public Mono<Map<Object, Object>> getAllRooms() {
        return redisTemplate.opsForHash()
                .entries(ROOMS_KEY)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .doOnSuccess(rooms -> log.info("Fetched {} rooms", rooms.size()))
                .onErrorResume(e -> {
                    log.error("Failed to get rooms: {}", e.getMessage());
                    return Mono.just(Collections.emptyMap());
                });
    }

    // ── Add user to room ─────────────────────────────────────

    public Mono<Boolean> addUserToRoom(String roomId, String sessionId) {
        return roomExists(roomId)
                .flatMap(exists -> {
                    if (!exists) {
                        log.warn("Room {} does not exist!", roomId);
                        return Mono.just(false);
                    }
                    return redisService.getUserBySession(sessionId)
                            .flatMap(userId -> redisTemplate.opsForSet()
                                    .add("room-members:" + roomId, userId)  // room-members:roomId → {userId}
                                    .map(count -> true)
                                    .doOnSuccess(res -> log.info("User {} added to room {}", userId, roomId))
                            )
                            .onErrorResume(e -> {
                                log.error("Failed to add user from session {} to room {}: {}", sessionId, roomId, e.getMessage());
                                return Mono.just(false);
                            })
                            .switchIfEmpty(Mono.just(false));
                });
    }

    // ── Remove user from room ────────────────────────────────

    public Mono<Boolean> removeUserFromRoom(String roomId, String userId) {
        return redisTemplate.opsForSet()
                .remove("room-members:" + roomId, userId)
                .map(count -> count > 0)
                .doOnSuccess(res -> log.info("User {} removed from room {}", userId, roomId))
                .onErrorResume(e -> {
                    log.error("Failed to remove user {} from room {}: {}", userId, roomId, e.getMessage());
                    return Mono.just(false);
                });
    }

    // ── Get room members ─────────────────────────────────────
    public Mono<Set<String>> getRoomMembers(String roomId) {
        return redisTemplate.opsForSet()
                .members("room-members:" + roomId)
                .map(Object::toString)
                .collect(Collectors.toSet())
                .doOnSuccess(members -> log.info("Room {} has {} members", roomId, members.size()))
                .onErrorResume(e -> {
                    log.error("Failed to get members of room {}: {}", roomId, e.getMessage());
                    return Mono.just(Collections.emptySet());
                });
    }

    // ── Check if user is in room ─────────────────────────────

    public Mono<Boolean> isUserInRoom(String roomId, String userId) {
        log.info("searching {} in {}",userId,roomId);
        return redisTemplate.opsForSet()
                .isMember("room-members:" + roomId, userId)
                .onErrorResume(e -> {
                    log.error("Failed to check user {} in room {}: {}", userId, roomId, e.getMessage());
                    return Mono.just(false);
                });
    }
}
