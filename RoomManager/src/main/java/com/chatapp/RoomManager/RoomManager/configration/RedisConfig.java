package com.chatapp.RoomManager.RoomManager.configration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String hostName;
    @Value("${spring.redis.port}")
    private int port;

    @Bean
    private RedisConnectionFactory redisConnectionFactory(){
        return new LettuceConnectionFactory(hostName,port);
    }

    @Bean
    public RedisTemplate<Object ,Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<Object,Object> redisTemplate =new RedisTemplate<>();
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
            redisTemplate.setConnectionFactory(connectionFactory);
        }catch (Exception e){
            log.error("Error RedisTemplate connection : ",e);
        }
        return redisTemplate;
    }

}
