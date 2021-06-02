package at.networkexplorer.backend.websocket;

import at.networkexplorer.backend.pojos.Command;
import at.networkexplorer.backend.utils.OSUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@Controller
public class CommandExecutor {

    private static ObjectMapper mapper = new ObjectMapper();

    public static boolean processCommand(WebSocketSession session, String cmd, String cwd) throws Exception {

        String prefix = "";
        if(OSUtil.getOS() == OSUtil.OS.WINDOWS)
            prefix = "cmd.exe /c cd " + cwd + " && ";
        else
            prefix = "/bin/bash -i -l cd " + cwd + " && ";
        Process process = Runtime.getRuntime().exec((prefix + cmd)); // https://stackabuse.com/executing-shell-commands-with-java/
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line = null, line2 = null;
        while(process.isAlive() || (line = reader.readLine()) != null || (line2 = error.readLine()) != null) {

            if(line != null)
                session.sendMessage(new TextMessage(mapper.writeValueAsString(new Command(cmd, line, false))));

            if(line2 != null)
                session.sendMessage(new TextMessage(mapper.writeValueAsString(new Command(cmd, line, true))));

        }

        session.sendMessage(new TextMessage(mapper.writeValueAsString(new Command(cmd, null, false, true))));

        return true;
    }

}
