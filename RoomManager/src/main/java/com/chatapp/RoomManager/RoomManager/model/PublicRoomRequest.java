package com.chatapp.RoomManager.RoomManager.model;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PublicRoomRequest(
        @NotNull @NotEmpty String roomId , @NotNull @NotEmpty String userId) { }
