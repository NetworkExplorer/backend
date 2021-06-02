package at.networkexplorer.backend.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Command {
    private String cmd;
    private String result;
    private boolean error;
    private boolean end;

    public Command(String cmd, String result, boolean error) {
        this.cmd = cmd;
        this.result = result;
        this.error = error;
        this.end = false;
    }

}
