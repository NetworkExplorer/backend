package at.networkexplorer.backend.api;

import at.networkexplorer.backend.api.response.ApiError;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@Controller
public class ApiErrorController implements ErrorController {
    @RequestMapping("/error")
    ResponseEntity<Object> handleError() {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND);
        apiError.setMessage("Mapping not found");
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}
