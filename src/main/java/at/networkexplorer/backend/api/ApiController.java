package at.networkexplorer.backend.api;

import at.networkexplorer.backend.api.response.Result;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@RequestMapping("api/v1")
@RestController
public class ApiController {

    /**
     * Mapping to check whether the server is online or not.
     * @param model ModelMap of the request
     * @return <a href="#{@link}">{@link ModelAndView}</a>
     */
    @GetMapping("ping")
    ModelAndView pong(ModelMap model) {
        model.addAttribute("attribute", "redirectWithRedirectPrefix");
        return new ModelAndView("redirect:https://www.youtube.com/watch?v=dQw4w9WgXcQ", model);
    }

}
