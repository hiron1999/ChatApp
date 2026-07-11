package com.chatapp.RoomManager.RoomManager.controller;

import com.chatapp.RoomManager.RoomManager.model.PublicRoomRequest;
import com.chatapp.RoomManager.RoomManager.service.PublicRoomManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/room/public")
public class PublicRoomController {
    @Autowired
    private PublicRoomManagementService roomService;

    @PostMapping("/enter")
    public ResponseEntity<String> enterRoom(@RequestBody PublicRoomRequest request){
        return  ResponseEntity.status(HttpStatus.CREATED).body(roomService.createOrJoinRoom(request));
    }


}
