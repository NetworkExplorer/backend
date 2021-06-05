package at.networkexplorer.backend.websocket;

import at.networkexplorer.backend.pojos.Command;
import at.networkexplorer.backend.utils.OSUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@Controller
public class CommandExecutor {

    private static Map<String, Process> processes = new HashMap<>();

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Execute a command for a session
     * @param session WebSocketSession of client
     * @param cmd String command to be executed
     * @param cwd String relative directory inside shared folder to execute the command in
     * @return False if a process for that session already exists; True if process was started
     * @throws IOException
     */
    public static boolean processCommand(WebSocketSession session, String cmd, String cwd) throws IOException {

        if(processes.get(session.getId()) != null)
            return false;

        String prefix = "";
        if (OSUtil.getOS() == OSUtil.OS.WINDOWS)
            prefix = "cmd.exe /c cd " + cwd + " && ";
        else
            prefix = "/bin/bash -i -l cd " + cwd + " && ";
        Process process = Runtime.getRuntime().exec((prefix + cmd)); // https://stackabuse.com/executing-shell-commands-with-java/

        processes.put(session.getId(), process);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        Thread thread = new Thread(() -> {
            try {
                String line = null, line2 = null;
                while (process.isAlive() || (line = reader.readLine()) != null || (line2 = error.readLine()) != null) {

                    if(!session.isOpen())
                        break;

                    if (line != null)
                        session.sendMessage(new TextMessage(mapper.writeValueAsString(new Command(cmd, line, false))));

                    if (line2 != null)
                        session.sendMessage(new TextMessage(mapper.writeValueAsString(new Command(cmd, line, true))));

                }

                if(session.isOpen()) session.sendMessage(new TextMessage(mapper.writeValueAsString(new Command(cmd, null, false, true))));
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                process.destroy();
                processes.remove(session.getId());
            }
        });
        thread.start();

        return true;
    }

    /**
     * Stops the execution of a command by destroying a process.
     * @param session WebSocketSession of the client
     */
    public static void stop(WebSocketSession session) {
        Process process = processes.remove(session.getId());
        process.destroy();
    }

}
