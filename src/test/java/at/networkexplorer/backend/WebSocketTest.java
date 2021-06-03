package at.networkexplorer.backend;

import at.networkexplorer.backend.pojos.Command;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketTest {

    @Value("${local.server.port}")
    private int port;

    WebSocketClient client;
    BlockingQueue<String> blockingQueue;

    @BeforeEach
    public void setup() {
        blockingQueue = new LinkedBlockingDeque<>();
        try {
            client = new WebSocketClient(new URI("ws://localhost:" + port + "/exec")) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.println("Connected!");
                }

                @Override
                public void onMessage(String s) {
                    blockingQueue.add(s);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    System.out.println(s);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            };
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCommand() throws Exception {
        while(!client.isOpen() && !client.isClosed());
        client.send("{'cwd': '/', 'cmd': 'dir'}");
        String res = blockingQueue.poll(1, TimeUnit.SECONDS);
        System.out.println(res);
        assertNotNull(res);
        client.close();
    }

}
