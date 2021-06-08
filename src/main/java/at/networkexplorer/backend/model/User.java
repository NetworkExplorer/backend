package at.networkexplorer.backend.model;

import at.networkexplorer.backend.beans.UserPermission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String username;
    private String password; // this is the hash (sha256)
    private List<UserPermission> permissions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    public boolean hasPermission(UserPermission permission) {
        return permissions.contains(permission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
