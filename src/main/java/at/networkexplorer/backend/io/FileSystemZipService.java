package at.networkexplorer.backend.io;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileSystemZipService implements ZipService {

    private final Path rootLocation;

    @Autowired
    FileSystemZipService(ApplicationContext applicationContext) {
        this.rootLocation = Paths.get(applicationContext.getEnvironment().getProperty("network.path"));
    }

    @Override
    public void init() {

    }

    @Override
    public void zipDir(String baseDir, String dir2zip, ZipOutputStream zos) {
        try {
            File zipDir = new File(dir2zip);

            String[] dirList = zipDir.list(); //read contents
            byte[] readBuffer = new byte[2156];
            int bytesIn = 0;

            for (int i = 0; i < dirList.length; i++) {
                File f = new File(zipDir, dirList[i]);

                if (f.isDirectory()) {
                    //if the File object is a directory, call this
                    //function again to add its content recursively
                    String filePath = f.getPath();
                    zipDir(baseDir, filePath, zos);
                    continue;
                }

                FileInputStream fis = new FileInputStream(f);
                //create entry with relative path, so that subfolders are being created
                ZipEntry anEntry = new ZipEntry(Path.of(baseDir).relativize(Path.of(f.getPath())).toString());
                zos.putNextEntry(anEntry);

                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }

                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void zipFile(File f, ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry(f.getName()));
        FileInputStream fis = new FileInputStream(f);
        IOUtils.copy(fis, zos);

        fis.close();
        zos.closeEntry();
    }

}