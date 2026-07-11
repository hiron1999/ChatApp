package com.chatapp.WebSoketService.controller;

import com.chatapp.WebSoketService.Model.GroupMessage;
import com.chatapp.WebSoketService.Model.Message;
import com.chatapp.WebSoketService.service.*;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
@Slf4j
@Controller
public class socketController {

    @Autowired
    private ProducerService producerService;
    @Autowired
    RedisService redisService;
    @Autowired
    private RoomMessagePublisher roomMessagePublisher;
    @Autowired
    UserConnectHandler connectHandler;
    @Autowired
    RoomManagementService roomManagementService;

    @MessageMapping("/hello/{id}")
//    @SendTo("/topic/greeting")
    public CompletableFuture<String> welcome(Message message, @DestinationVariable("id") String user_id)throws Exception{
        System.out.println("massage recieved from : %s".formatted(user_id));
        System.out.println(message);
//        Thread.sleep(1000);
        CompletableFuture<Boolean> rdisops=redisService.add(user_id,message.toString()).doOnSuccess(res->{
            System.out.println("result added "+ res);
        }).toFuture();
//        String reply ="wlcome %s !".formatted(message);
//        System.out.println(reply);
        return rdisops.thenCompose(chain->producerService.publishMassage(message)) ;
    }
    @MessageMapping("/join/{userId}")
    public Mono<String> join(@DestinationVariable("userId") String user_id , SimpMessageHeaderAccessor headerAccessor)throws Exception{
        String sessionId = headerAccessor.getSessionId();
        log.debug("session id fron join: "+sessionId);
         return connectHandler.addUser(user_id,sessionId);

    }
    @MessageMapping("/room/create/{roomId}")
    public Mono<String> createRoom(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();// get from session

        return roomManagementService.createRoom(roomId, sessionId)
                .map(created -> created
                        ? "Room " + roomId + " created successfully!"
                        : "ERROR: Room " + roomId + " already exists!"
                );
    }

    @MessageMapping("/room/join/{roomId}")
    public Mono<String> joinRoom(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();// get from session

        return roomManagementService.addUserToRoom(roomId, sessionId)
                .map(joined -> joined
                        ? "Joined room " + roomId
                        : "ERROR: Room " + roomId + " does not exist!"
                );
    }
    @MessageMapping("/room")
    public Mono<String> sendToRoom(GroupMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        return roomMessagePublisher.publishMessage(message, sessionId);
    }
}
