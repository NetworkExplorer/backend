package at.networkexplorer.backend.db;

import at.networkexplorer.backend.beans.UserPermission;
import at.networkexplorer.backend.model.User;
import at.networkexplorer.backend.utils.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class FileDB {

    private List<User> users = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${pbkdf2.secret}")
    private String secret;

    Logger logger = LoggerFactory.getLogger(FileDB.class);

    @PostConstruct
    public void init() {
        String path = System.getProperty("user.dir") + File.separator + "nwexp.json";
        File db = new File(path);

        User admin = new User("admin", "", Arrays.asList(UserPermission.values()));
        String pw = PasswordUtil.generate(12);
        admin.setPassword(this.encrypt(pw));
        users.add(admin);

        try {
            if(db.createNewFile()) {
                mapper.writeValue(db, users);
                logger.info(String.format("Admin Password: %s", pw));
            } else {
                users = Arrays.asList(mapper.readValue(db, User[].class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void store() throws IOException {
        String path = System.getProperty("user.dir") + File.separator + "nwexp.json";
        File db = new File(path);
        mapper.writeValue(db, users);
    }

    public boolean createUser(User user) throws IOException {
        if(users.contains(user))
            return false;

        users.add(user);
        try {
            this.store();
        }catch (IOException e) {
            users.remove(user);
            throw e;
        }
        return true;
    }

    public boolean removeUser(User user) throws IOException {
        if(!users.contains(user))
            return false;

        users.remove(user);
        try {
            this.store();
        } catch(IOException e) {
            users.add(user);
            throw e;
        }

        return true;
    }

    public boolean updateUser(User user) throws IOException {
        if(!this.removeUser(user))
            return false;
        return this.createUser(user);
    }

    public User getUserByUsername(String username) throws NoSuchElementException {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().get();
    }

    public String encrypt(String password) {
        return PasswordUtil.hashPassword(password, secret);
    }

    private boolean check(String password, String hash) {
        return PasswordUtil.checkPasswordHash(password, secret, hash);
    }

    public boolean authenticate(String username, String password) throws NoSuchElementException {
        User user = users.stream().filter(u -> u.getUsername().equals(username)).findFirst().get();
        if(check(password, user.getPassword()))
            return true;
        return false;
    }

    public List<User> getUsers() {
        return this.users;
    }

}
