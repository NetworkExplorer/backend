package at.networkexplorer.backend.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.OnOpen;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@Controller
public class CommandExecutor {

    private static ObjectMapper mapper = new ObjectMapper();

    public static boolean processCommand(WebSocketSession session, String cmd) throws Exception {
        //TODO: working directory
        Process process = Runtime.getRuntime().exec("cmd.exe /c " + cmd); // https://stackabuse.com/executing-shell-commands-with-java/
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        while(process.isAlive()) {
            String line = reader.readLine();
            if(line != null)
                session.sendMessage(new TextMessage(mapper.writeValueAsString(new Command(cmd, line))));
        }

        return true;
    }

}
