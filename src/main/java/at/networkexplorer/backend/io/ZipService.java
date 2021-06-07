package at.networkexplorer.backend.io;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public interface ZipService {

    void init();

    /**
     * Compresses a folder from the shared directory and appends it to the given Stream
     * @param dir2zip Relative path inside the shared folder
     * @param zos ZipOutputStream to write the folder to
     */
    void zipDir(String dir2zip, ZipOutputStream zos);

    /**
     * Compresses a file from the shared directory and appends it to the given Stream
     * @param f File inside the shared folder
     * @param zos ZipOutputStream to write the file to
     * @throws IOException
     */
    void zipFile(File f, ZipOutputStream zos) throws IOException;

}
