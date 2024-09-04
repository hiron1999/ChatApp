package com.chatapp.WebSoketService.Model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Message {
    String from;
    String to;
    String text;

    @Override
    public String toString() {
        return String.format("[ From: %s, To: %s, Text: %s ]",this.from,this.to,this.text);
    }
}
