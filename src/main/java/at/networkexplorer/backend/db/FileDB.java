package at.networkexplorer.backend.db;

import at.networkexplorer.backend.beans.UserPermission;
import at.networkexplorer.backend.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class FileDB {

    private static FileDB instance;
    private List<User> users = new ArrayList<>(Arrays.asList(new User("admin", "005b53e7a54bda22685965b1aca999a8fe89d94cc3abfd3014db35dc1ec7e632", Arrays.asList(new UserPermission[] { UserPermission.CREATE_ACCOUNT, UserPermission.READ, UserPermission.WRITE, UserPermission.TERMINAL }))));
    private ObjectMapper mapper = new ObjectMapper();

    public FileDB() {
        String path = System.getProperty("user.dir") + File.separator + "nwexp.json";
        File db = new File(path);

        try {
            if(db.createNewFile()) {
                mapper.writeValue(db, users);
            } else {
                users = Arrays.asList(mapper.readValue(db, User[].class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static FileDB getInstance() {
        if(instance == null) instance = new FileDB();
        return instance;
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

    public User getUserByUsername(String username) throws NoSuchElementException {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().get();
    }

    public String encrypt(String password) {
        //TODO: change to PBKDF2!
        return Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
    }

    public boolean authenticate(String username, String password) throws NoSuchElementException {
        User user = users.stream().filter(u -> u.getUsername().equals(username)).findFirst().get();
        if(user.getPassword().equals(encrypt(password)))
            return true;
        return false;
    }

}
