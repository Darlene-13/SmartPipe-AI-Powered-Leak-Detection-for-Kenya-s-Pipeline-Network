package io.github.darlene.leakdetectionapplication.websocket;

import io.github.darlene.leakdetectionapplication.dto.response.FaultAlertResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

/**
 * Manages all active WebSocket browser sessions.
 * Broadcasts fault alerts to every connected dashboard client.
 * Sessions list is initialized inline — not injected by Spring.
 * ObjectMapper is injected by Spring for JSON serialization.
 */
@Slf4j
@Component
public class AlertWebSocketHandler extends TextWebSocketHandler {

    // Initialized inline — Spring does not manage this list
    private final CopyOnWriteArrayList<WebSocketSession> sessions
            = new CopyOnWriteArrayList<>();

    // Injected by Spring — registered as @Bean in RestClientConfig
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("Browser connected. Active sessions: {}", sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("Browser disconnected. Active sessions: {}", sessions.size());
    }

    /**
     * Serializes FaultAlertResponse to JSON and broadcasts
     * to all connected dashboard clients.
     */
    public void broadcastAlert(FaultAlertResponse alertResponse) {
        try {
            String message = objectMapper.writeValueAsString(alertResponse);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
            log.info("Broadcast alert to {} WebSocket clients", sessions.size());
        } catch (Exception e) {
            log.error("Failed to broadcast alert: {}", e.getMessage());
        }
    }

    /**
     * Broadcasts a pre-serialized JSON string directly.
     * Used when alert is already serialized upstream.
     */
    public void sendAlertAll(String alertJson) {
        sessions.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(alertJson));
                    log.info("Alert sent to session: {}", session.getId());
                } catch (IOException e) {
                    log.error("Failed to send alert to session: {}", session.getId(), e);
                }
            }
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("Message from browser: {}", message.getPayload());
    }
}