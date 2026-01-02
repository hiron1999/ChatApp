package com.chatapp.RoomManager.RoomManager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomUtils {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    public  Boolean isExist (String room_id) throws Exception{
        return redisTemplate.hasKey(room_id);
    }

    public List<String> getUsers(String room_id) throws Exception{
        List<String> users = Collections.EMPTY_LIST;
        if(isExist(room_id)){
            users = Objects.requireNonNull(redisTemplate.opsForSet().members(room_id)).stream().map(Object::toString).toList();
        }
        return users;
    }

    public Boolean isUserPresent(String user_id ,String room_id) throws Exception {
        Boolean is_present = Boolean.FALSE;

        if(isExist(room_id)){
            is_present = Objects.requireNonNull(redisTemplate.opsForSet().members(room_id)).contains(user_id);
        }
        return is_present;
    }


}
