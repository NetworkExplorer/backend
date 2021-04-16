package at.networkexplorer.backend.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetworkFile {
    private String name;
    private long size;
    private FileType type;
    private String modified;
    private String created;
    private String owner;
    private NetworkFile[] files;

    public NetworkFile(Path path) throws IOException {
        this(path.toFile());
    }

    public NetworkFile(String name, FileType type, NetworkFile[] files) {
        this.name = name;
        this.type = type;
        this.files = files;
    }

    public NetworkFile(File f) throws IOException {
        final BasicFileAttributes attr = Files.readAttributes(Paths.get(f.getPath()), BasicFileAttributes.class);
        final FileOwnerAttributeView own = Files.getFileAttributeView(Paths.get(f.getPath()), FileOwnerAttributeView.class);
        this.name = f.getName();
        this.type = f.isDirectory()?FileType.FOLDER:FileType.FILE;
        this.size = f.length();
        this.modified = attr.lastModifiedTime().toString();
        this.created = attr.creationTime().toString();
        this.owner = own.getOwner().getName();
    }
}
