package com.chatapp.WebSoketService.service;

import com.chatapp.WebSoketService.Model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
@Service
public class ProducerService {
    @Value(value = "${spring.kafka.topic}")
    private String topicKey;
    @Autowired
    private KafkaTemplate<String , Message> kafkaTemplate;


    public CompletableFuture<String> publishMassage(Message message){
        CompletableFuture<SendResult<String,Message>> result = kafkaTemplate.send(topicKey,message);

       return result.handle((res, ex)->{

            if (ex == null) {
                System.out.println("Sent message=[" + message +
                        "] with offset=[" + res.getRecordMetadata().offset() + "]");
                return "massage sent";
            } else {
                System.out.println("Unable to send message=[" +
                        message + "] due to : " + ex.getMessage());
                return "massage  not sent";
            }

        });
    }

//    @SendToUser("")
//    private String acknoledge(String status){
//
//    }

}
