package at.networkexplorer.backend.api;

import at.networkexplorer.backend.api.response.Result;
import at.networkexplorer.backend.beans.FileType;
import at.networkexplorer.backend.beans.NetworkFile;
import at.networkexplorer.backend.exceptions.StorageFileNotFoundException;
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
import java.util.Arrays;
import java.util.zip.ZipOutputStream;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST, RequestMethod.PUT})
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

    /**
     * Mapping to get the contents of the root directory of the shared folder.
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @GetMapping("folder")
    Result folder() {
        return new Result(
                200,
                new NetworkFile("/", FileType.FOLDER, storageService.loadAll("/")));
    }

    /**
     * Mapping to get the contents of a folder
     * @param folder Relative path of a directory inside the shared folder
     * @param request HttpServletRequest to get path variables from
     * @return <a href="#{@link}">{@link Result}</a>
     */
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
            throw new StorageFileNotFoundException(String.format(Messages.FOLDER_NOT_FOUND, overallFolder));
        }
    }

    /**
     * Mapping to download a single file via a ResponseEntity
     * @param file Relative path of single file inside the shared folder
     * @return <a href="#{@link}">{@link ResponseEntity}</a>
     */
    @GetMapping("download/file")
    @ResponseBody
    ResponseEntity<?> serveFile(@RequestParam(required = true) String file) {
        Resource res = storageService.loadAsResource(file);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.getFilename() + "\"").body(res);
    }

    /**
     * Mapping that opens a ZipOutputStream to send data to the requesting client.
     * @param files Array of files or directories relative paths inside the shared folder
     * @param response HttpServletResponse to get the OutputStream from
     */
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
            throw new StorageFileNotFoundException(Messages.FILES_NOT_EXIST);
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

    /**
     * Mapping to upload a file to the given directory.
     * @param file Multipart file
     * @param path Relative path of a directory inside the shared folder
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @PostMapping("/upload")
    @ResponseBody
    Result fileUpload(@RequestParam("file")MultipartFile file, @RequestParam("path") String path) {
        storageService.store(file, path);
        return new Result(201, true, String.format(Messages.UPLOAD_SUCCESS, file.getName(), path));
    }

    /**
     * Mapping to rename/move a path inside the shared folder.
     * @param path Relative path of a file or folder inside the shared folder
     * @param newPath Relative path to be
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @PutMapping("/rename")
    @ResponseBody
    Result fileRename(@RequestParam("path") String path, @RequestParam("newPath") String newPath) {
        storageService.rename(path, newPath);
        return new Result(201, true, String.format(Messages.MOVED_SUCCESS, path, newPath));
    }

    /**
     * Mapping to delete a path inside the shared folder.
     * @param paths Relative path of a file or folder inside the shared folder
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @DeleteMapping("/delete")
    @ResponseBody
    Result fileDelete(@RequestBody String[] paths) {
        for(String p : paths) {
            storageService.delete(p);
        }

        return new Result(201, null, Messages.DELETE_SUCCESS);
    }

    /**
     * Mapping for the creation of folders inside the shared folder.
     * @param path Relative path inside the shared folder
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @PostMapping("/mkdir")
    @ResponseBody
    Result mkdir(@RequestParam String path) {
        try {
            storageService.mkdir(path);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(Messages.COULD_NOT_CREATE, path));
        }
        return new Result(201, null, String.format(Messages.CREATE_SUCCESS, path));
    }

}
