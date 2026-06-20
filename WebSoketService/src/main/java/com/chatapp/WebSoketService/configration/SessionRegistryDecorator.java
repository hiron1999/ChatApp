package com.chatapp.WebSoketService.configration;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketSessionDecorator;

import java.io.IOException;

@Component
public class SessionRegistryDecorator extends WebSocketHandlerDecorator {

    private final WebSocketSessionRegistry sessionRegistry;

    public SessionRegistryDecorator(WebSocketHandler delegate,
                                    WebSocketSessionRegistry sessionRegistry) {
        super(delegate);
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // anonymous inner class — no need for separate class ✅
        WebSocketSessionDecorator wrappedSession = new WebSocketSessionDecorator(session) {
            @Override
            public void sendMessage(WebSocketMessage<?> message) throws IOException {
                String payload = message.getPayload().toString();

                if (payload.contains("CONNECTED")) {
                    String sessionId = session.getId();
                    String modified = payload.replace(
                            "\n\n",
                            "\nsession-id:" + sessionId + "\n\n"
                    );
                    System.out.println("intercept connected: " + sessionId);
                    super.sendMessage(new TextMessage(modified));
                    return;
                }
                super.sendMessage(message);
            }
        };

        sessionRegistry.register(session.getId(), session);
        super.afterConnectionEstablished(wrappedSession);  // ← pass wrapped session
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus closeStatus) throws Exception {
        sessionRegistry.unregister(session.getId());
        super.afterConnectionClosed(session, closeStatus);
    }

}