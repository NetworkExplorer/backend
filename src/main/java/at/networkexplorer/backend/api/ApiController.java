package at.networkexplorer.backend.api;

import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@RequestMapping("api/v1")
@RestController
public class ApiController {

    @GetMapping("ping")
    String test() {
        return "chillig";
    }

}
