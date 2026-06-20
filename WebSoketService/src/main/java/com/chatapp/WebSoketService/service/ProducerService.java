package com.chatapp.WebSoketService.service;

import com.chatapp.WebSoketService.Model.GroupMessage;
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
    @Value(value = "${spring.kafka.topic.private}")
    private  String private_topic_key;
    @Value(value = "${spring.kafka.topic.group}")
    private  String group_topic_key;

    @Autowired
    private KafkaTemplate<String , Object> kafkaTemplate;


    public CompletableFuture<String> publishMassage(Message message){

        CompletableFuture<SendResult<String,Object>> result = kafkaTemplate.send(private_topic_key,message);

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

    public CompletableFuture<String> publishToGroup(GroupMessage message) {
        CompletableFuture<SendResult<String, Object>> result = kafkaTemplate.send(group_topic_key,message.roomID(),message);
        return result.handle((res,ex)->{

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



}
