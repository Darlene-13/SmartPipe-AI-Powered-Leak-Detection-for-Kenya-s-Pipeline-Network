package io.github.darlene.leakdetectionapplication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
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
                .setAllowedOriginPatterns(
                "https://ai-pipeline-leak-detection.onrender.com",
                "http://localhost:3000",
                "https://ai-pipeline-leak-detection.vercel.app/",
                "https://ai-pipeline-leak-detection-git-main-darlene-wendys-projects.vercel.app/",
                "https://ai-pipeline-leak-detection-142mk6nwn-darlene-wendys-projects.vercel.app/"
                );
    }
}
