package at.networkexplorer.backend.io;

import at.networkexplorer.backend.beans.NetworkFile;
import at.networkexplorer.backend.bl.SuggestionComparator;
import at.networkexplorer.backend.component.Config;
import at.networkexplorer.backend.exceptions.StorageException;
import at.networkexplorer.backend.exceptions.StorageFileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;
    private List<String> discoveries = new ArrayList<>();

    @Autowired
    public FileSystemStorageService(ApplicationContext applicationContext) {
        this.rootLocation = Paths.get(applicationContext.getBean(Config.class).getPath());
    }

    @Override
    public void store(MultipartFile file, String path) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = load(Paths.get(path,file.getOriginalFilename()).toString());

            if(!Files.exists(destinationFile))
                Files.createDirectories(destinationFile);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public NetworkFile[] loadAll(String path) {
        try {
            return Files.walk(Paths.get(rootLocation.toString(),path), 1)
                    .filter(p -> !p.equals(Paths.get(this.rootLocation.toString(), path)))
                    .map(this::map).collect(Collectors.toList()).toArray(NetworkFile[]::new);
        }
        catch (IOException e) {
            throw new StorageFileNotFoundException("Failed to read stored files", e);
        }
    }

    private NetworkFile map(Path path) {
        try {
            return new NetworkFile(path);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void delete(String path) { FileSystemUtils.deleteRecursively(load(path).toFile()); }

    @Override
    public Path load(String path) throws StorageException{
        Path absolute = this.rootLocation.resolve(Paths.get(path)).normalize().toAbsolutePath();

        if(!absolute.toString().contains(rootLocation.toString()))
            throw new StorageException("Cannot operate outside of root directory.");

        return absolute;
    }

    @Override
    public void rename(String path, String path2) {
        try {
            Path oldPath = load(path);
            Path newPath = load(path2);

            if(!Files.exists(newPath))
                Files.createDirectories(newPath);

            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw new StorageException("Failed to rename file.", e);
        }
    }

    @Override
    public void mkdir(String path) throws IOException {
        Files.createDirectories(load(path));
    }

    private void discover() throws IOException {
        List<String> temp = Files.walk(rootLocation).filter(p -> p.toFile().isDirectory()).map(this::discMap).collect(Collectors.toList());
        Collections.sort(temp, new SuggestionComparator());
        this.discoveries = temp;
    }

    private String discMap(Path path) {
        return rootLocation.relativize(path).toString();
    }

    @Override
    public String[] suggest(String path, int max) {
        return discoveries.stream().filter(s -> s.contains(path)).limit(max).toArray(String[]::new);
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }

        // Discover directory every 5 seconds
        Timer discoverer = new Timer();
        discoverer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    discover();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 5000);
    }

}