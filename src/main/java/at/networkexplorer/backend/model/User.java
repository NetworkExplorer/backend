package at.networkexplorer.backend.model;

import at.networkexplorer.backend.beans.UserPermission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @NotEmpty(message = "Username may not be empty")
    private String username;
    private String password;
    private Set<UserPermission> permissions;
    private Set<String> jwts;

    public User(String username, String password, Set<UserPermission> permissions) {
        this.username = username;
        this.password = password;
        this.permissions = permissions;
        jwts = new HashSet<>();
    }

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

    public boolean addJwt(String jwt) {
        return this.jwts.add(jwt);
    }

    public boolean removeJwt(String jwt) {
        return this.jwts.remove(jwt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
