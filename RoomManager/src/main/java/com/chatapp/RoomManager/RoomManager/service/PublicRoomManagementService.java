package com.chatapp.RoomManager.RoomManager.service;

import com.chatapp.RoomManager.RoomManager.model.PublicRoomRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PublicRoomManagementService {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private RoomUtils roomUtils;

    public String createOrJoinRoom(PublicRoomRequest roomRequest) {
        String res = null;
        try {
            redisTemplate.opsForSet().add("room-members:" + roomRequest.roomId(), roomRequest.userId());
            res =roomRequest.roomId();
        }catch (Exception e){
            log.error("Error creating room : ",e);
            res = "Unable to create room " + roomRequest.roomId();
        }

        return res;
    }



}
