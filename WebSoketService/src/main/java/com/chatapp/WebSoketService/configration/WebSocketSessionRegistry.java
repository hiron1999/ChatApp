package com.chatapp.WebSoketService.configration;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Set<String> rejectedSessions = ConcurrentHashMap.newKeySet();
    // called manually from decorator
    public void register(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
        System.out.println("Session registered: " + sessionId);
    }

    // called manually from decorator
    public void unregister(String sessionId) {
        sessions.remove(sessionId);
        rejectedSessions.remove(sessionId);
        System.out.println("Session unregistered: " + sessionId);
    }
    public void rejectSession(String sessionId) {
        rejectedSessions.add(sessionId);  // ← mark as rejected
    }
    public Boolean isRejected(String sessionId) {
        return rejectedSessions.contains(sessionId);  // ← check if rejected
    }
    public void freeSession(String sessionId){
        rejectedSessions.remove(sessionId);
    }
    public void closeSession(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE);
                System.out.println("Force closed session: " + sessionId);
            } catch (IOException e) {
                System.out.println("Failed to close session: " + e.getMessage());
            }
        }
    }

    public boolean isSessionOpen(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        return session != null && session.isOpen();
    }
}