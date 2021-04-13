package at.networkexplorer.backend.component;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class Config {

    @Value("${network.path}")
    private String path;

}
