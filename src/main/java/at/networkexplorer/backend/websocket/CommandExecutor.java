package at.networkexplorer.backend.websocket;

import at.networkexplorer.backend.pojos.Command;
import at.networkexplorer.backend.utils.OSUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.nio.file.Path;
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
    public static boolean processCommand(WebSocketSession session, String cmd, Path cwd) throws IOException {

        // only one command at a time per session
        if(processes.get(session.getId()) != null)
            return false;

        String prefix = "";
        if (OSUtil.getOS() == OSUtil.OS.WINDOWS)
            prefix = "cmd.exe /c \"%s\"";
        else
            prefix = "/bin/bash -c \"%s\"";

        // creates a subprocess and executes the given command in the cwd directory
        Process process = Runtime.getRuntime().exec(String.format(prefix, cmd), null, cwd.toFile()); // https://stackabuse.com/executing-shell-commands-with-java/

        processes.put(session.getId(), process);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // use thread so we don't block the main thread
        Thread thread = new Thread(() -> {
            try {
                String line = null, err = null;
                while ((line = reader.readLine()) != null || (err = error.readLine()) != null) {

                    if(!session.isOpen())
                        break;

                    if (line != null)
                        session.sendMessage(new TextMessage(mapper.writeValueAsString(new Command(cmd, line, false))));

                    if (err != null)
                        session.sendMessage(new TextMessage(mapper.writeValueAsString(new Command(cmd, err, true))));

                }
                process.destroy();
                processes.remove(session.getId());
            } catch(IOException e) {
                //e.printStackTrace(); // Is called everytime, CTRL+C is executed
            } finally {
                process.destroy();
                processes.remove(session.getId());
                if(session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(mapper.writeValueAsString(new Command(cmd, null, false, true))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start(); // use start to actually start a new thread

        return true;
    }

    /**
     * Stops the execution of a command by destroying a process.
     * @param session WebSocketSession of the client
     */
    public static void stop(WebSocketSession session) throws IOException {
        Process process = processes.remove(session.getId());
        process.getInputStream().close();
        process.getOutputStream().close();
        process.getErrorStream().close();
        process.destroy();
    }

}
