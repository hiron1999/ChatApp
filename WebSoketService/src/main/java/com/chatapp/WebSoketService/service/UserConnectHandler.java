package com.chatapp.WebSoketService.service;

import com.chatapp.WebSoketService.Model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UserConnectHandler {

    @Autowired
    private RedisService redisService;

    public Mono<String> addUser(String userId){
        String serverId = "server1";
       return redisService.add(userId,new User(userId,userId,serverId)).map(res->{
            if(res){
                return String.format("User : %s successfully connected to %s",userId,serverId);
            }
            return "Unable to add user";
        });

    }
}
