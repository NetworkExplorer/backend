package at.networkexplorer.backend.io;

import at.networkexplorer.backend.beans.NetworkFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file, String path);

    NetworkFile[] loadAll(String path);

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

}