package at.networkexplorer.backend.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Command {
    private String cmd;
    private String result;
}
