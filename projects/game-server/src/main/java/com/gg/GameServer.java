package com.gg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gg.messages.ChatMessage;
import com.gg.messages.ConnectionStatus;
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

@ServerWebSocket("/ws/game/{table_id}/{user_id}")
public class GameServer {
    private static final Logger LOG = LoggerFactory.getLogger(GameServer.class);

    private final WebSocketBroadcaster broadcaster;
    private final ObjectMapper objectMapper;

    public GameServer(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
        this.objectMapper = new ObjectMapper();
    }

    @OnOpen
    public Publisher<JsonNode> onOpen(String table_id, String user_id, WebSocketSession session) {
        log("onOpen", session, user_id, table_id);

        ConnectionStatus connectionStatus = new ConnectionStatus(user_id, table_id, "connected...");

        return broadcaster.broadcast(objectMapper.valueToTree(connectionStatus), isValid(table_id));
    }

    @OnMessage
    public Publisher<JsonNode> onMessage(
            String table_id,
            String user_id,
            JsonNode message,
            WebSocketSession session) throws JsonProcessingException {

        log("onMessage", session, user_id, table_id);

        String messageType = message.get("messageType").asText();
        switch (messageType) {
            case "chatMessage" -> {
                ChatMessage chatMessage = objectMapper.treeToValue(message, ChatMessage.class);
                return broadcaster.broadcast(objectMapper.valueToTree(chatMessage), isValid(table_id));
            }
            default -> throw new IllegalStateException("Unexpected value: " + messageType);
        }
    }

    @OnClose
    public Publisher<JsonNode> onClose(
            String table_id,
            String user_id,
            WebSocketSession session) {

        log("onClose", session, user_id, table_id);

        ConnectionStatus connectionStatus = new ConnectionStatus(user_id, table_id, "disconnected...");

        return broadcaster.broadcast(objectMapper.valueToTree(connectionStatus), isValid(table_id));
    }

    private void log(String event, WebSocketSession session, String user_id, String table_id) {
        LOG.info("* WebSocket: {} received for session {} from '{}' regarding '{}'",
                event, session.getId(), user_id, table_id);
    }

    private Predicate<WebSocketSession> isValid(String table_id) {
        return s -> true; //intra-id chat
    }
}
