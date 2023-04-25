package com.gg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

@ServerWebSocket("/ws/game/{id}/{username}")
public class GameServer {
    private static final Logger LOG = LoggerFactory.getLogger(GameServer.class);

    private final WebSocketBroadcaster broadcaster;
    private final ObjectMapper objectMapper;

    public GameServer(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
        this.objectMapper = new ObjectMapper();
    }

    @OnOpen
    public Publisher<String> onOpen(String id, String username, WebSocketSession session) {
        log("onOpen", session, username, id);

        return broadcaster.broadcast(String.format("[%s] connected...", username), isValid(id));
    }

    @OnMessage
    public Publisher<String> onMessage(
            String id,
            String username,
            String message,
            WebSocketSession session) {

        log("onMessage", session, username, id);

        try {
            JsonNode jsonMessage = this.objectMapper.readTree(message);
            String action = jsonMessage.get("action").asText();
            switch (action) {
                case "CHAT" -> {
                    String chatMessage = jsonMessage.get("message").asText();
                    return broadcaster.broadcast(String.format("[%s] %s", username, chatMessage), isValid(id));
                }
                default -> throw new IllegalStateException("Unexpected value: " + action);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @OnClose
    public Publisher<String> onClose(
            String id,
            String username,
            WebSocketSession session) {

        log("onClose", session, username, id);
        return broadcaster.broadcast(String.format("[%s] left...", username), isValid(id));
    }

    private void log(String event, WebSocketSession session, String username, String id) {
        LOG.info("* WebSocket: {} received for session {} from '{}' regarding '{}'",
                event, session.getId(), username, id);
    }

    private Predicate<WebSocketSession> isValid(String id) {
        return s -> true; //intra-id chat
    }
}
