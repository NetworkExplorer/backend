package at.networkexplorer.backend.api;

import at.networkexplorer.backend.api.response.Result;
import at.networkexplorer.backend.beans.FileType;
import at.networkexplorer.backend.beans.NetworkFile;
import at.networkexplorer.backend.beans.UserPermission;
import at.networkexplorer.backend.config.JwtAuthenticationEntryPoint;
import at.networkexplorer.backend.exceptions.InsufficientPermissionsException;
import at.networkexplorer.backend.exceptions.StorageFileNotFoundException;
import at.networkexplorer.backend.io.StorageService;
import at.networkexplorer.backend.io.ZipService;
import at.networkexplorer.backend.messages.Messages;
import at.networkexplorer.backend.model.User;
import at.networkexplorer.backend.pojos.Token;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST, RequestMethod.PUT})
@RequestMapping("api/v1")
@RestController
public class FileController {
    private final StorageService storageService;
    private final ZipService zipService;

    private final Set<String> fileTokens = new HashSet<>();

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    public FileController(StorageService storageService, ZipService zipService) {
        this.storageService = storageService;
        this.zipService = zipService;
        storageService.init();
        zipService.init();
    }

    /**
     * Mapping to get the contents of the root directory of the shared folder.
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @GetMapping("folder")
    Result folder() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.READ)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.READ));
        }
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
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.READ)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.READ));
        }
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

    @GetMapping("token")
    @ResponseBody
    Result getToken() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.READ)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.READ));
        }

        String token;
        do {
            token = UUID.randomUUID().toString();
        } while(fileTokens.contains(token));
        fileTokens.add(token);
        return new Result(200, new Token(token));
    }

    /**
     * Mapping to download a single file via a ResponseEntity
     * @param file Relative path of single file inside the shared folder
     * @return <a href="#{@link}">{@link ResponseEntity}</a>
     */
    @GetMapping("download/file")
    @ResponseBody
    ResponseEntity<?> serveFile(@RequestParam(required = true) String file, @RequestParam(required = true) String token) {
        if(!fileTokens.remove(token))
            throw new IllegalArgumentException("Invalid File-Token");
        Resource res = storageService.loadAsResource(file);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + res.getFilename()).body(res);
    }

    /**
     * Mapping that opens a ZipOutputStream to send data to the requesting client.
     * @param files Array of files or directories relative paths inside the shared folder
     * @param response HttpServletResponse to get the OutputStream from
     */
    @GetMapping("download/files")
    @ResponseBody
    void serveFiles(@RequestParam(required = true) String[] files, @RequestParam(required = true) String token, HttpServletResponse response) {
        if(!fileTokens.remove(token))
            throw new IllegalArgumentException("Invalid File-Token");

        response.setContentType("application/zip");
        response.setStatus(200);
        String filename = (files.length > 1 ? "compressed" : storageService.load(files[0]).getFileName()) + ".zip";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+filename);

        try(ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {

            for (String f : files) {
                File file = storageService.load(f).toFile();
                try {
                    if(file.isDirectory())
                        zipService.zipDir(file.getParent(), file.getPath(), zos);
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

    /**
     * Mapping to upload a file to the given directory.
     * @param file Multipart file
     * @param path Relative path of a directory inside the shared folder
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @PostMapping("/upload")
    @ResponseBody
    Result fileUpload(@RequestParam("file")MultipartFile file, @RequestParam("path") String path) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.WRITE)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.WRITE));
        }
        storageService.store(file, path);
        return new Result(201, true, String.format(Messages.UPLOAD_SUCCESS, file.getOriginalFilename(), path));
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
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.WRITE)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.WRITE));
        }
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
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.WRITE)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.WRITE));
        }
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
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.WRITE)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.WRITE));
        }
        try {
            storageService.mkdir(path);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(Messages.COULD_NOT_CREATE, path));
        }
        return new Result(201, null, String.format(Messages.CREATE_SUCCESS, path));
    }

    /**
     * Mapping for the autocomplete suggestions when navigating
     * @param path Relative path inside the shared folder
     * @param max Maximum Results to return
     * @return <a href="#{@link}">{@link Result}</a> An array of suggestions
     */
    @GetMapping("/suggest")
    @ResponseBody
    Result suggest(@RequestParam String path, @RequestParam int max) {
        return new Result(200, storageService.suggest(path, max), Messages.DISCOVER);
    }

}
