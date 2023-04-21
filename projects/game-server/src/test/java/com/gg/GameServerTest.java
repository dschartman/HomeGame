package com.gg;

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

    @Requires(property = "spec.name", value = "GameServerTest")
    @ClientWebSocket
    static abstract class TestWebSocketClient implements AutoCloseable {

        private final Deque<String> messageHistory = new ConcurrentLinkedDeque<>();

        public String getLatestMessage() {
            return messageHistory.peekLast();
        }

        public List<String> getMessagesChronologically() {
            return new ArrayList<>(messageHistory);
        }

        @OnMessage
            // <5>
        void onMessage(String message) {
            messageHistory.add(message);
        }

        abstract void send(@NonNull @NotBlank String message);
    }

    private TestWebSocketClient createWebSocketClient(int port, String username, String id) {
        WebSocketClient webSocketClient = beanContext.getBean(WebSocketClient.class);
        URI uri = UriBuilder.of("ws://localhost")
                .port(port)
                .path("ws")
                .path("game")
                .path("{id}")
                .path("{username}")
                .expand(CollectionUtils.mapOf("id", id, "username", username));
        Publisher<TestWebSocketClient> client = webSocketClient.connect(TestWebSocketClient.class, uri);

        return Flux.from(client).blockFirst();
    }

    @Test
    void testWebsocketServer() throws Exception {
        TestWebSocketClient player1 = createWebSocketClient(embeddedServer.getPort(), "player1", "1");
        await().until(() ->
                Collections.singletonList("[player1] connected...")
                        .equals(player1.getMessagesChronologically()));

        TestWebSocketClient player2 = createWebSocketClient(embeddedServer.getPort(), "player2", "1");
        await().until(() ->
                Collections.singletonList("[player2] connected...")
                        .equals(player2.getMessagesChronologically()));

        await().until(() ->
                Arrays.asList("[player1] connected...", "[player2] connected...")
                        .equals(player1.getMessagesChronologically()));

        player1.send("hi");
        await().until(() -> "[player1] hi".equals(player2.getLatestMessage()));

        player2.send("bye!");
        await().until(() -> "[player2] bye!".equals(player1.getLatestMessage()));

        player2.close();
        await().until(() -> "[player2] left...".equals(player1.getLatestMessage()));

        player1.close();
    }
}
