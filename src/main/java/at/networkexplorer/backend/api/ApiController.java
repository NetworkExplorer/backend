package at.networkexplorer.backend.api;

import at.networkexplorer.backend.beans.Folder;
import at.networkexplorer.backend.component.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/v1")
@RestController
public class ApiController {

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("test")
    String test() {
        return "chillig";
    }

    @GetMapping("folder/{folder}")
    Folder folder() {
        String path = (applicationContext.getBean(Config.class)).getPath();
        return new Folder("ROOT", path);
    }

}
