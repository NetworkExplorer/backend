package at.networkexplorer.backend.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
    @JsonProperty("status_code")
    int statusCode;
    Object data;
    String message;

    public Result(int statusCode, Object data) {
        this.statusCode = statusCode;
        this.data = data;
    }

}
