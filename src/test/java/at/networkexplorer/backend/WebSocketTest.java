package at.networkexplorer.backend;

import at.networkexplorer.backend.websocket.Command;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

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
    public void testConnection() throws Exception {
        StompSession session = stompClient.connect(URL, new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);
        assertTrue(session.isConnected());
    }

    @Test
    public void testCommand() throws Exception {
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session = stompClient.connect(URL, new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        session.subscribe("/user/queue/output", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders stompHeaders) {
                return LinkedHashMap.class;
            }

            @Override
            public void handleFrame(StompHeaders stompHeaders, Object payload) {
                System.out.println("Received Result: " + payload);
                LinkedHashMap<String, String> res = (LinkedHashMap)payload;
                Command cmd = new ObjectMapper().convertValue(res, Command.class);
                blockingQueue.add(cmd.toString());
            }
        });

        session.send("/app/exec", "dir");
        String res = blockingQueue.poll(1, TimeUnit.SECONDS);
        assertNotNull(res);
    }

}
