package at.networkexplorer.backend.io;

import at.networkexplorer.backend.beans.NetworkFile;
import at.networkexplorer.backend.exceptions.StorageException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    /**
     * Stores a Multipart file on the system at given directory.
     * @param file Multipart file
     * @param path Relative path inside the shared folder
     */
    void store(MultipartFile file, String path);

    /**
     * Reads the files within a specific directory of the shared folder and returns them.
     * @param path Relative path inside the shared folder
     * @return <a href="#{@link}">{@link NetworkFile}</a> array
     */
    NetworkFile[] loadAll(String path);

    /**
     * Resolves a path relative to the shared folder and makes sure that the path is not outside of the shared folder.
     * @param path Relative path inside the shared folder
     * @return Absolute path on filesystem
     * @throws StorageException if the resolved path is outside the shared folder.
     */
    Path load(String path) throws StorageException;

    /**
     * Loads a file as resource.
     * @param filename File to load
     * @return Resource object of file
     */
    Resource loadAsResource(String filename);

    /**
     * Deletes all the files within the shared folder
     */
    void deleteAll();

    /**
     * Deletes a specific file or folder recursively.
     * @param path Relative path of a file or folder inside the shared folder
     */
    void delete(String path);

    /**
     * Moves/Renames a file or folder
     * @param path Relative path of a file or folder inside the shared folder
     * @param newPath Relative path to be inside the shared folder
     */
    void rename(String path, String newPath);

    /**
     * Creates a directory inside the shared folder.
     * @param path Relative path
     * @throws IOException if the directory could not be created
     */
    void mkdir(String path) throws IOException;

    String[] suggest(String path, int max);

}