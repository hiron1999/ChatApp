package com.chatapp.WebSoketService.controller;

import com.chatapp.WebSoketService.Model.GroupMessage;
import com.chatapp.WebSoketService.Model.Message;
import com.chatapp.WebSoketService.service.ProducerService;
import com.chatapp.WebSoketService.service.RedisService;
import com.chatapp.WebSoketService.service.UserConnectHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Controller
public class socketController {

    @Autowired
    private ProducerService producerService;
    @Autowired
    private RedisService redisService;
    @Autowired
    UserConnectHandler connectHandler;

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
    public Mono<String> join( @DestinationVariable("userId") String user_id)throws Exception{
        return connectHandler.addUser(user_id);
    }
    @MessageMapping("/room")
    public CompletableFuture<String> sendToRoom(GroupMessage message){
        return producerService.publishToGroup(message);
    }
}
