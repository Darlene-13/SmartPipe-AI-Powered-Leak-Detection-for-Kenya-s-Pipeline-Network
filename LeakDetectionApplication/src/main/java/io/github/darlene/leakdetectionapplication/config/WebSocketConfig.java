package io.github.darlene.leakdetectionapplication.config;

// Spring configuration class
import org.springframework.context.annotation.Configuration;
// Enables web socket support in springboot
import org.springframework.web.socket.config.annotation.EnableWebSocket;
// Interface where we register handler
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
// Registry where we map api to handlers.
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
// The handler
import io.github.darlene.leakdetectionapplication.websocket.AlertWebSocketHandler;



/***
 * This file tells spring boot that the websocket is enabled
 * Which url the browser connects to
 * which url prefix routes to websocket handlers
 *
 */


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{

    private final AlertWebSocketHandler alertWebSocketHandler;

    public WebSocketConfig(AlertWebSocketHandler alertWebSocketHandler){
        this.alertWebSocketHandler = alertWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){
        registry
                .addHandler(alertWebSocketHandler, "/ws/alerts")
                .setAllowedOrigins("*");
    }
}