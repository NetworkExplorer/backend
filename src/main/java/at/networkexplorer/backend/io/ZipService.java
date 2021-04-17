package at.networkexplorer.backend.io;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public interface ZipService {

    void init();

    void zipDir(String dir2zip, ZipOutputStream zos);
    void zipFile(File f, ZipOutputStream zos) throws IOException;

}
