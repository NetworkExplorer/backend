package at.networkexplorer.backend.api;

import at.networkexplorer.backend.beans.FileType;
import at.networkexplorer.backend.beans.NetworkFile;
import at.networkexplorer.backend.component.Config;
import at.networkexplorer.backend.io.IOAccess;
import at.networkexplorer.backend.messages.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Path;

@RequestMapping("api/v1")
@RestController
public class ApiController {

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("ping")
    String test() {
        return "chillig";
    }

    @GetMapping("folder")
    NetworkFile folder() {
        return new NetworkFile("/", FileType.FOLDER, IOAccess.listFilesForFolder(applicationContext.getBean(Config.class).getPath()));
    }

    @GetMapping("folder/{folder}/**")
    NetworkFile folder(@PathVariable("folder") String folder, HttpServletRequest request) {

        final String parth =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();

        String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, parth);

        String overallFolder = folder + "/" + arguments;

        String path = (applicationContext.getBean(Config.class)).getPath() + File.separator + overallFolder;

        try {
            return new NetworkFile(overallFolder, FileType.FOLDER, IOAccess.listFilesForFolder(path));
        }catch(NullPointerException e){
            throw new NullPointerException(String.format(Messages.FOLDER_NOT_FOUND, overallFolder));
        }
    }

}
