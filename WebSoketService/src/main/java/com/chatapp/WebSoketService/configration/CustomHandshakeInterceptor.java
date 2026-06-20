package com.chatapp.WebSoketService.configration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
@Component
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private WebSocketSessionRegistry sessionRegistry;
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String path= request.getURI().getPath();
        System.out.println("From custhandshake requestpath : "+path);
        attributes.put("socket-path",path);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        String path= request.getURI().getPath();
    }
}
