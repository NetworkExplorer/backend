package at.networkexplorer.backend.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/v1")
@RestController
public class ApiController {

    @GetMapping("test")
    String test() {
        return "chillig";
    }

}
