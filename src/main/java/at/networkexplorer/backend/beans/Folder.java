package at.networkexplorer.backend.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Folder {
    private String name;
    private String size;
}
