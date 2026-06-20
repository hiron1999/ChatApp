package com.chatapp.WebSoketService.service;

import com.chatapp.WebSoketService.Model.User;
import com.chatapp.WebSoketService.Model.WebsocketResponse;
import com.chatapp.WebSoketService.configration.WebSocketSessionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UserConnectHandler {

    @Autowired
    private RedisService redisService;
//    @Autowired
//    private SimpUserRegistry simpUserRegistry;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    WebSocketSessionRegistry sessionRegistry;

    public Mono<String> addUser(String userId , String sessionId){
        String serverId = "server1";
        return redisService.userExists(userId)
                .flatMap(exist->
                        {
                           if(!exist){
                               if(sessionRegistry.isRejected(sessionId)){
                                   sessionRegistry.freeSession(sessionId);
                               }
                            // fresh user → just add
                             return redisService.addUser(userId, sessionId)
                                    .map(res ->{
                                        if(res){

                                        messagingTemplate.convertAndSendToUser(sessionId,"/queue/private",
                                               new WebsocketResponse("SUCCESS", String.format("User : %s successfully connected to %s", userId, serverId)));
                                        }
                                        else {
                                            throw new RuntimeException("not able to write session in redis");
                                        }
                                        return "";
                                    });

                           }else {
                               log.info("User {} already connected, rejecting session {}", userId, sessionId);
                                sessionRegistry.rejectSession(sessionId);
//                               sessionRegistry.closeSession(sessionId);
                               messagingTemplate.convertAndSendToUser(sessionId,"/queue/private", new WebsocketResponse("ERROR",": Username already taken!"));
                               return Mono.just("ERROR: Username already taken!");
                           }
                        }) .onErrorResume(e -> {
                    log.error("Error in addUser for {}: {}", userId, e.getMessage());
                    messagingTemplate.convertAndSendToUser(sessionId,"/queue/private",new WebsocketResponse("ERROR", "Server error, try again!"));
                    return Mono.just("ERROR: Server error, try again!");
                });

    }



}
