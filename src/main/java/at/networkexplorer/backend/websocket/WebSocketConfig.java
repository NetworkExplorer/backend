package at.networkexplorer.backend.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    SocketHandler socketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socketHandler, "/exec").setAllowedOriginPatterns("http://localhost:*", "http://127.#{1,3}.#{1,3}.#{1,3}:*", "http://10.#{1,3}.#{1,3}.#{1,3}", "http://192.168.#{1,3}.#{1,3}", "http://172.(1[6-9] | 2[0-9] | 31).#{1,3}.#{1,3}");
    }
}
