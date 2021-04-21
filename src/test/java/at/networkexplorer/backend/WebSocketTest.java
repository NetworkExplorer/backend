package at.networkexplorer.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketTest {

    @Value("${local.server.port}")
    private int port;
    static String URL;

    BlockingQueue<String> blockingQueue;
    WebSocketStompClient stompClient;

    @BeforeEach
    public void setup() {
        blockingQueue = new LinkedBlockingDeque<>();
        stompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        URL = "ws://localhost:" + port + "/websocket";
    }

    @Test
    public void pingWebsocket() throws Exception {
        StompSession session = stompClient.connect(URL, new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        session.subscribe("/topic/ping", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders stompHeaders) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders stompHeaders, Object o) {
                System.out.println("Received ping: " + o);
                blockingQueue.add((String) o);
            }
        });

        session.send("/app/ping", "");
        String res = blockingQueue.poll(1, TimeUnit.SECONDS);
        assertNotNull(res);
    }

    @Test
    public void shouldReceiveAMessageFromTheServer() throws Exception {
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session = stompClient.connect(URL, new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        session.subscribe("/user/queue/errors", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders stompHeaders) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders stompHeaders, Object payload) {
                System.out.println("Received error: " + payload);
            }
        });

        session.subscribe("/user/queue/output", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders stompHeaders) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders stompHeaders, Object payload) {
                System.out.println("Received message: " + payload);
                blockingQueue.add((String) payload);
            }
        });

        session.send("/app/exec", "ping google.at");
        String res = blockingQueue.poll(1, TimeUnit.SECONDS);
        assertNotNull(res);
    }

}
