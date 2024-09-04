package com.chatapp.WebSoketService.controller;

import com.chatapp.WebSoketService.Model.Message;
import com.chatapp.WebSoketService.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class socketController {

    @Autowired
    private ProducerService producerService;

    @MessageMapping("/hello/{id}")
//    @SendTo("/topic/greeting")
    public CompletableFuture<String> welcome(Message message, @DestinationVariable("id") String user_id)throws Exception{
        System.out.println("massage recieved from : %s".formatted(user_id));
        System.out.println(message);
//        Thread.sleep(1000);

        String reply ="wlcome %s !".formatted(message);
        System.out.println(reply);
        return producerService.publishMassage(message);
    }
}
