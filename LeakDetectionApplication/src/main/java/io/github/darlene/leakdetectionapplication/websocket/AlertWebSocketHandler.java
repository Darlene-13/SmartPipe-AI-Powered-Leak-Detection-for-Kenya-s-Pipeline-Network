package io.github.darlene.leakdetectionapplication.websocket;

// Spring components
import org.springframework.stereotype.Component;
// Base class to handle low-level WebSocket protocol
import org.springframework.web.socket.handler.TextWebSocketHandler;
// Representing one browser connection
import org.springframework.web.socket.WebSocketSession;
// Message to be sent to the browser
import org.springframework.web.socket.TextMessage;
// Thread-safe list where multiple browsers can connect simultaneously
import java.util.concurrent.CopyOnWriteArrayList;

// Lombok for logging
import lombok.extern.slf4j.Slf4j;

// IO exceptions
import java.io.IOException;
import org.springframework.web.socket.CloseStatus;

/**
 * Keeps track of all connected browsers
 * Pushes alert messages to every connected browser
 * Handles browser connections and disconnections
 */
@Slf4j
@Component
public class AlertWebSocketHandler extends TextWebSocketHandler {

    // Thread-safe list of all connected browser sessions
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    // Called when a browser connects
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("Browser connected. Active sessions: {}", sessions.size());
    }

    // Called when a browser disconnects
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("Browser disconnected. Active sessions: {}", sessions.size());
    }

    // Handle messages from browser
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Message from browser: {}", message.getPayload());
    }

    // Send alert to all connected browsers
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
}