package io.github.darlene.leakdetectionapplication.websocket;


// Spring components
import org.springframework.stereotype.Component;
// Base class to handle low level web scoket protocol
imoort org.springframework.web.socket.handler.TextWebSocketHandler;
// Representing one browser connection
import org.springframework.web.seocket.WebSocketSession;
// Message to be sent to the browser
import org.springframework.web.socket.TextMessage;
// Thread safe list where multiple browsers can connect simulatenously
import org.util.concurrent.CopyOnWriteArrayList;

// Lombok for logging
import lombok.Slf4j;

// IO exceptions
import java.io.IOException;



/**
 * This file keeps track of all the connected browsers
 * Pushes the alert messsages to every connected browser
 * Handles the browser that connect and disconnect.
 */

public class AlertWebSocketHandler extends TextWebSocketHandler{

    // Thread safe list of all connected browser sessions
    // Copy on writearraylist because multple array thrreads may connect and disconnect simultaneously.

    private final CopyOnWriteArrayLIST<WebSocketSession> sessions = new CopyOnWriteArrayLists<>();

    // Called when the browser connects
    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        sessions.add(session);
        log.info("Browser connected. Active sessions: {}", sessions.size());
    }

    //Called when a broswer disconnects
    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloneStatus status){
        session.remove(session);
        log.info("Browser disconnected. Active sessions: {}", sessions.size());
    }

    @Override
    protected void handleMessage(WebSocketSession session, TextMessage textMessage){
        log.info("Message from browser: {}", message.getPayload());
    }

    // Alert service method
    @Override void sendAlertAll(String alertJson){
        sessions.forEach(session -> {
            if(session.isOpen()){
                try {
                    session.sendMessage(new TextMessage(alertJson));
                    log.info("Alert sent to session: {}", session.getId());
                } catch (IOException e){
                    log.error("Failed to send alert to session: {}", session.getId(), e);
                }
            }
        });
    }

}
