package at.networkexplorer.backend.api;

import at.networkexplorer.backend.beans.FileType;
import at.networkexplorer.backend.beans.NetworkFile;
import at.networkexplorer.backend.io.StorageService;
import at.networkexplorer.backend.messages.Messages;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@RequestMapping("api/v1")
@RestController
public class ApiController {

    private final StorageService storageService;

    @Autowired
    public ApiController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("ping")
    String test() {
        return "chillig";
    }

    @GetMapping("folder")
    NetworkFile folder() {
        return new NetworkFile("/", FileType.FOLDER, storageService.loadAll("/"));
    }

    @GetMapping("folder/{folder}/**")
    NetworkFile folder(@PathVariable("folder") String folder, HttpServletRequest request) {
        final String parth =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();

        String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, parth);

        String overallFolder = folder + File.separator + arguments;

        try {
            return new NetworkFile(overallFolder, FileType.FOLDER, storageService.loadAll(overallFolder));
        }catch(NullPointerException e){
            throw new NullPointerException(String.format(Messages.FOLDER_NOT_FOUND, overallFolder));
        }
    }

    @GetMapping("download/file/")
    @ResponseBody
    ResponseEntity<?> serveFile(@RequestParam(required = true) String file) {
        Resource res = storageService.loadAsResource(file);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.getFilename() + "\"").body(res);
    }

    @GetMapping("download/files/")
    @ResponseBody
    void serveZip(@RequestParam(required = true) String[] files, HttpServletResponse response) {

        response.setContentType("application/zip");
        response.setStatus(200);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compressed.zip");

        try(ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            ArrayList<File> afiles = new ArrayList<>();

            for (String f : files) {
                afiles.add(new File(f));
            }

            for (File f : afiles) {
                if (!f.exists())
                    continue;
                try {
                    zos.putNextEntry(new ZipEntry(f.getName()));
                    FileInputStream fis = new FileInputStream(f);
                    IOUtils.copy(fis, zos);

                    fis.close();
                    zos.closeEntry();
                } catch (IOException e) {
                    continue;
                }
            }

            try {
                zos.finish();
                zos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            IOUtils.closeQuietly(zos);
        } catch(IOException e) {
            throw new NullPointerException("One or more files does not exist!");
        }
    }

}
