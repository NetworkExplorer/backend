package at.networkexplorer.backend.db;

import at.networkexplorer.backend.beans.UserPermission;
import at.networkexplorer.backend.model.User;
import at.networkexplorer.backend.utils.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class FileDB {

    private List<User> users = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${pbkdf2.secret}")
    private String secret;

    Logger logger = LoggerFactory.getLogger(FileDB.class);

    @PostConstruct
    private void init() {
        String path = System.getProperty("user.dir") + File.separator + "nwexp.json";
        File db = new File(path);

        User admin = new User("admin", "", Set.of(UserPermission.values()));
        String pw = PasswordUtil.generate(12);
        admin.setPassword(this.encrypt(pw));
        users.add(admin);

        try {
            if(db.createNewFile()) {
                mapper.writeValue(db, users);
                logger.info(String.format("Admin Password: %s", pw));
            } else {
                users = new ArrayList<>(Arrays.asList(mapper.readValue(db, User[].class)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Store the database
     * @throws IOException If the database could not be store
     */
    public void store() throws IOException {
        String path = System.getProperty("user.dir") + File.separator + "nwexp.json";
        File db = new File(path);
        mapper.writeValue(db, users);
    }

    /**
     * Creates a user new user and adds them to the database
     * @param user <a href="#{@link}">{@link User}</a> to create
     * @return True if the user was successfully created and stored, otherwise false
     * @throws IOException If the database could not be stored
     */
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

    /**
     * Removes a user from the database
     * @param user <a href="#{@link}">{@link User}</a> to remove
     * @return True if the user was successfully removed, otherwise false
     * @throws IOException If the database could not be stored
     */
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

    /*
    public boolean updateUser(User user) throws IOException {
        if(!this.removeUser(user))
            return false;
        return this.createUser(user);
    }*/

    /**
     * Returns the user object to the given username
     * @param username Username
     * @return <a href="#{@link}">{@link User}</a>
     * @throws NoSuchElementException
     */
    public User getUserByUsername(String username) throws NoSuchElementException {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().get();
    }

    /**
     * Encrypt the password
     * @param password Unencrypted password
     * @return Password hash as hex string
     */
    public String encrypt(String password) {
        return PasswordUtil.hashPassword(password, secret);
    }

    /**
     * Compare the password of a user with the stored hash
     * @param username Username of login
     * @param password Password of login
     * @return True if the password is correct, otherwise false
     * @throws NoSuchElementException If the given username does not exist
     */
    public boolean authenticate(String username, String password) throws NoSuchElementException {
        User user = users.stream().filter(u -> u.getUsername().equals(username)).findFirst().get();
        if(check(password, user.getPassword()))
            return true;
        return false;
    }

    // actual check function to the public authenticate function
    private boolean check(String password, String hash) {
        return PasswordUtil.checkPasswordHash(password, secret, hash);
    }

    public List<User> getUsers() {
        return this.users;
    }

}
