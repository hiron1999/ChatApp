package com.chatapp.WebSoketService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
@Slf4j
@Service
public class RedisService {

    private final ReactiveValueOperations<String,Object> valueOperations;

    public RedisService(@Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String,Object> redisTemplate) {
        this.valueOperations = redisTemplate.opsForValue();
    }

    public Mono<Boolean> add(String key , Object val){
        log.info("adding ro rdis store.......");
        return valueOperations.set(key,val);

    }

    public Mono<Object> getval(String key){
        return valueOperations.get(key);
    }

}
