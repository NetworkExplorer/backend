package at.networkexplorer.backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {
    String operation;
    String result;
}
