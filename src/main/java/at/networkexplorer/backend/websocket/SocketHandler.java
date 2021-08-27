package at.networkexplorer.backend.websocket;

import at.networkexplorer.backend.api.response.ApiError;
import at.networkexplorer.backend.beans.UserPermission;
import at.networkexplorer.backend.config.JwtTokenUtil;
import at.networkexplorer.backend.io.StorageService;
import at.networkexplorer.backend.model.User;
import at.networkexplorer.backend.service.JwtUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SocketHandler extends TextWebSocketHandler {

    // keep log of who has provided a valid JWT and has the right permissions
    Map<String, Boolean> sessions = new HashMap();
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private StorageService storageService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws IOException {
        Map value = new Gson().fromJson(message.getPayload(), Map.class);
        try {
            if (!sessions.get(session.getId())) {
                String bearer = value.get("bearer") == null ? null : value.get("bearer").toString();
                User user = jwtUserDetailsService.loadUserByUsername(jwtTokenUtil.getUsernameFromToken(bearer));

                if (!user.getPermissions().contains(UserPermission.TERMINAL)) {
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(new ApiError(HttpStatus.BAD_REQUEST, "You do not have permission to execute commands!", new Throwable("Insufficient permissions")))));
                    return;
                }

                if (bearer != null && jwtTokenUtil.validateToken(bearer, user)) {
                    sessions.put(session.getId(), true);
                } else {
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(new ApiError(HttpStatus.BAD_REQUEST, "Please provide a valid bearer token!", new Throwable("Invalid or missing JWT")))));
                    return;
                }
            }
        } catch(IllegalArgumentException | SignatureException e) {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(new ApiError(HttpStatus.BAD_REQUEST, "Invalid Bearer token", e))));
            return;
        }

        try {
            if(value.get("cmd") != null)
                CommandExecutor.processCommand(session, value.get("cmd").toString(), storageService.load(value.get("cwd").toString()));
            else if(value.get("exit") != null)
                CommandExecutor.stop(session);
        } catch (Exception e) {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR: Could not execute command", e))));
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), false);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }
}