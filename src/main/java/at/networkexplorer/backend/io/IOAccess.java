package at.networkexplorer.backend.io;

import at.networkexplorer.backend.beans.NetworkFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class IOAccess {

    public static NetworkFile[] listFilesForFolder(final String path) throws NullPointerException {
        final File folder = new File(path);
        return Arrays.stream(folder.listFiles()).map(IOAccess::map).collect(Collectors.toList()).toArray(NetworkFile[]::new);
    }

    private static NetworkFile map(File file) {
        try {
            return new NetworkFile(file);
        } catch (IOException e) {
            return null;
        }
    }

}
