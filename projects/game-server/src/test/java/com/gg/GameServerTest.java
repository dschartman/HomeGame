package com.gg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gg.messages.ConnectionStatus;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketClient;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnMessage;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import javax.validation.constraints.NotBlank;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Property(name = "spec.name", value = "GameServerTest")
@MicronautTest // <2>
class GameServerTest {

    @Inject
    BeanContext beanContext;

    @Inject
    EmbeddedServer embeddedServer;
    @Named("json")
    @Inject
    private ObjectMapper objectMapper;

    @Requires(property = "spec.name", value = "GameServerTest")
    @ClientWebSocket
    static abstract class TestWebSocketClient implements AutoCloseable {

        private final Deque<JsonNode> messageHistory = new ConcurrentLinkedDeque<>();

        public JsonNode getLatestMessage() {
            return messageHistory.peekLast();
        }

        public List<JsonNode> getMessagesChronologically() {
            return new ArrayList<>(messageHistory);
        }

        @OnMessage
            // <5>
        void onMessage(JsonNode message) {
            messageHistory.add(message);
        }

        abstract void send(@NonNull JsonNode message);
    }

    private TestWebSocketClient createWebSocketClient(int port, String user_id, String table_id) {
        WebSocketClient webSocketClient = beanContext.getBean(WebSocketClient.class);
        URI uri = UriBuilder.of("ws://localhost")
                .port(port)
                .path("ws")
                .path("game")
                .path("{table_id}")
                .path("{user_id}")
                .expand(CollectionUtils.mapOf("table_id", table_id, "user_id", user_id));
        Publisher<TestWebSocketClient> client = webSocketClient.connect(TestWebSocketClient.class, uri);

        return Flux.from(client).blockFirst();
    }

    @Test
    void testPlayerConnectAndDisconnect() throws Exception {
        TestWebSocketClient player1 = createWebSocketClient(embeddedServer.getPort(), "player1", "1");
        await().until(() -> {
            var latestMessage = player1.getLatestMessage();
            ConnectionStatus connectionStatus = objectMapper.treeToValue(latestMessage, ConnectionStatus.class);
            return "player1".equals(connectionStatus.getUserId()) && "connected".equals(connectionStatus.getMessage());
        });

        TestWebSocketClient player2 = createWebSocketClient(embeddedServer.getPort(), "player2", "1");
        await().until(() -> "connected".equals(player2.getLatestMessage().get("message").asText()));

        player1.close();
        await().until(() -> "disconnected".equals(player2.getLatestMessage().get("message").asText()));
        player2.close();
    }

    @Test
    void testWebsocketServer() throws Exception {
        String tableId = "potato-table";
        String p1 = "potato-player-1";
        String p2 = "potato-player-2";
        TestWebSocketClient player1 = createWebSocketClient(embeddedServer.getPort(), p1, tableId);
        TestWebSocketClient player2 = createWebSocketClient(embeddedServer.getPort(), p2, tableId);

        player1.send(createChatMessage(p1, tableId,"hi"));
        await().until(() -> "hi".equals(player2.getLatestMessage().get("message").asText()));

        player2.send(createChatMessage(p2, tableId, "bye!"));
        await().until(() -> "bye!".equals(player1.getLatestMessage().get("message").asText()));

        player2.close();
        player1.close();
    }

    private void assertChatMessage(String expectedMessage, TestWebSocketClient player) throws JsonProcessingException {
        await().until(() -> expectedMessage.equals(player.getLatestMessage().get("message").asText()));
    }

    private ObjectNode createChatMessage(String userId, String tableId, String message) {
        ObjectNode chatMessage = objectMapper.createObjectNode();
        chatMessage.put("type", "chatMessage");
        chatMessage.put("message", message);
        chatMessage.put("userId", userId);
        chatMessage.put("tableId", tableId);

        return chatMessage;
    }
}
