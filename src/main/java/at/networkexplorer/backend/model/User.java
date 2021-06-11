package at.networkexplorer.backend.model;

import at.networkexplorer.backend.beans.UserPermission;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String username;
    private String password;
    private List<UserPermission> permissions;
    private List<String> jwts;

    public User(String username, String password, List<UserPermission> permissions) {
        this.username = username;
        this.password = password;
        this.permissions = permissions;
        jwts = new ArrayList<>();
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
