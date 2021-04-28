package at.networkexplorer.backend.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Command {
    private String cmd;
    private String result;

    public Command(Map<String,String> map) {
        this.cmd = map.get("cmd").trim();
        this.result = map.get("result").trim();
    }

}
