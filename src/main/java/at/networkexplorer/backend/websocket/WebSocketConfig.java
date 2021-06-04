package at.networkexplorer.backend.websocket;

import at.networkexplorer.backend.io.FileSystemStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(new FileSystemStorageService(applicationContext)), "/exec").setAllowedOriginPatterns("http://localhost:*", "http://127.#{1,3}.#{1,3}.#{1,3}:*", "http://10.#{1,3}.#{1,3}.#{1,3}", "http://192.168.#{1,3}.#{1,3}", "http://172.(1[6-9] | 2[0-9] | 31).#{1,3}.#{1,3}");
    }
}
