package at.networkexplorer.backend.websocket;

import at.networkexplorer.backend.api.response.ApiError;
import at.networkexplorer.backend.exceptions.StorageException;
import at.networkexplorer.backend.io.StorageService;
import at.networkexplorer.backend.io.ZipService;
import at.networkexplorer.backend.pojos.Command;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {

    List sessions = new CopyOnWriteArrayList<>();
    ObjectMapper mapper = new ObjectMapper();

    private final StorageService storageService;

    @Autowired
    public SocketHandler(StorageService storageService) {
        this.storageService = storageService;
        storageService.init();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        Map value = new Gson().fromJson(message.getPayload(), Map.class);
		/*for(WebSocketSession webSocketSession : sessions) {
			Map value = new Gson().fromJson(message.getPayload(), Map.class);
			webSocketSession.sendMessage(new TextMessage("Hello " + value.get("name") + " !"));
		}*/
        try {
            CommandExecutor.processCommand(session, value.get("cmd").toString(), storageService.load(value.get("cwd").toString()).toString());
        } catch (Exception e) {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR: Could not execute command", e))));
            //e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //the messages will be broadcasted to all users.
        sessions.add(session);
    }
}