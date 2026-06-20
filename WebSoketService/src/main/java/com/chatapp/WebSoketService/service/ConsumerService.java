package com.chatapp.WebSoketService.service;

import com.chatapp.WebSoketService.Model.GroupMessage;
import com.chatapp.WebSoketService.Model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    @Autowired
    private  SimpMessagingTemplate messagingTemplate;
    @Autowired
    RoomMessagePublisher roomMessagePublisher;



    @KafkaListener(topics = "${spring.kafka.topic.private}" , containerFactory = "kafkaListenerContainerFactory")

    public void consumeMassage(  @Payload Message message){
        System.out.printf("form consumer service : %s", message.toString());
        String userId= message.getTo();
        String from_msg= "%s : %s".formatted(message.getFrom(),message.getText());
        messagingTemplate.convertAndSendToUser(userId,"/queue/private",from_msg);
//        messagingTemplate.convertAndSend("/topic/hello/"+userId,from_msg);
    }
@KafkaListener(topics = "${spring.kafka.topic.group}" , containerFactory = "groupKafkaListenerContainerFactory")
    public void consumeRoomMessage(@Payload GroupMessage message){
        System.out.printf("form consumer service : %s", message.toString());
        String room_id= message.roomID();
        String from_msg= "%s : %s".formatted(message.from(),message.msg());
//        messagingTemplate.convertAndSendToUser(room_id,"/queue/private",from_msg);
        roomMessagePublisher.publish(room_id,from_msg);
    }
}
