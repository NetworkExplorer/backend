package at.networkexplorer.backend.websocket;

import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.websocket.OnOpen;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@Controller
public class CommandController {

    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public String ping() throws Exception {
        return "PONG!";
    }

    @MessageMapping("/exec")
    @SendToUser("/queue/output")
    public Command processCommand(String cmd) throws Exception {
        //TODO: working directory
        Process process = Runtime.getRuntime().exec("cmd.exe /c " + cmd); // https://stackabuse.com/executing-shell-commands-with-java/
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        process.waitFor();

        String output = reader.lines().collect(Collectors.joining("\n"));
        //System.out.println(output);

        return new Command(cmd, output);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable throwable) {
        return throwable.getMessage();
    }

}
