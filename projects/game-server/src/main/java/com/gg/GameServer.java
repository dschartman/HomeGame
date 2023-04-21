package com.gg;

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

    public GameServer(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
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
        return broadcaster.broadcast(String.format("[%s] %s", username, message), isValid(id));
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
