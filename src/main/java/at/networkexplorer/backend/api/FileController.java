package at.networkexplorer.backend.api;

import at.networkexplorer.backend.api.response.Result;
import at.networkexplorer.backend.beans.FileType;
import at.networkexplorer.backend.beans.NetworkFile;
import at.networkexplorer.backend.io.StorageService;
import at.networkexplorer.backend.io.ZipService;
import at.networkexplorer.backend.messages.Messages;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@RequestMapping("api/v1")
@RestController
public class FileController {
    private final StorageService storageService;
    private final ZipService zipService;

    @Autowired
    public FileController(StorageService storageService, ZipService zipService) {
        this.storageService = storageService;
        this.zipService = zipService;
    }

    @GetMapping("folder")
    Result folder() {
        return new Result(
                200,
                new NetworkFile("/", FileType.FOLDER, storageService.loadAll("/")));
    }

    @GetMapping("folder/{folder}/**")
    Result folder(@PathVariable("folder") String folder, HttpServletRequest request) {
        final String parth =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();

        String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, parth);

        String overallFolder = folder + File.separator + arguments;

        try {
            return new Result(200,
                            new NetworkFile(overallFolder, FileType.FOLDER, storageService.loadAll(overallFolder)));
        }catch(NullPointerException e){
            throw new NullPointerException(String.format(Messages.FOLDER_NOT_FOUND, overallFolder));
        }
    }

    @GetMapping("download/file")
    @ResponseBody
    ResponseEntity<?> serveFile(@RequestParam(required = true) String file) {
        Resource res = storageService.loadAsResource(file);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.getFilename() + "\"").body(res);
    }

    @GetMapping("download/files")
    @ResponseBody
    void serveFiles(@RequestParam(required = true) String[] files, HttpServletResponse response) {
        response.setContentType("application/zip");
        response.setStatus(200);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compressed.zip");

        try(ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {

            for (String f : files) {
                File file = storageService.load(f).toFile();
                try {
                    if(file.isDirectory())
                        zipService.zipDir(file.getPath(), zos);
                    else
                        zipService.zipFile(file, zos);
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
            throw new NullPointerException("One or more files/folders does not exist!");
        }

    }

    /*
    @GetMapping("download/folder/")
    @ResponseBody
    void serveFolder(@RequestParam(required = true) String[] folders, HttpServletResponse response) {
        response.setContentType("application/zip");
        response.setStatus(200);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compressed.zip");

        try(ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {

            for (String f : folders) {
                File file = storageService.load(f).toFile();
                zipService.zipDir(file.getPath(), zos);
            }

            try {
                zos.finish();
                zos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            IOUtils.closeQuietly(zos);
        } catch(IOException e) {
            e.printStackTrace();
            throw new NullPointerException("One or more folders does not exist!");
        }
    }*/

    @PostMapping("/upload")
    Result fileUpload(@RequestParam("file")MultipartFile file, @RequestParam("path") String path) {
        storageService.store(file, path);
        return new Result(201, true, "Uploaded " + file.getName() + " to /" + path);
    }

    @PutMapping("/rename")
    Result fileRename(@RequestParam("path") String path, @RequestParam("newPath") String newPath) {
        storageService.rename(path, newPath);
        return new Result(201, true, "Moved from /" + path + " to /" + newPath);
    }

    @DeleteMapping("/delete")
    @ResponseBody
    Result fileDelete(@RequestBody String[] paths) {
        for(String p : paths) {
            storageService.delete(p);
        }

        return new Result(201, null, "Deleted successfully");
    }

    @PutMapping("/mkdir")
    @ResponseBody
    Result mkdir(@RequestParam String path) {
        try {
            storageService.mkdir(path);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not create folder: " + path);
        }
        return new Result(201, null, "Created " + path + " successfully");
    }

}
