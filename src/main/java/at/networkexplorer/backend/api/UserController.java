package at.networkexplorer.backend.api;

import at.networkexplorer.backend.api.response.Result;
import at.networkexplorer.backend.config.JwtTokenUtil;
import at.networkexplorer.backend.db.FileDB;
import at.networkexplorer.backend.messages.Messages;
import at.networkexplorer.backend.model.User;
import at.networkexplorer.backend.pojos.JwtResult;
import at.networkexplorer.backend.pojos.Login;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@RequestMapping("api/v1/user")
@RestController
public class UserController {

    // https://www.javainuse.com/spring/boot-jwt

    private FileDB db = FileDB.getInstance();

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PutMapping("")
    @ResponseBody
    Result createUser(@RequestBody User user) {
        //TODO: check whether user from bearer token has permission to create users

        user.setPassword(db.encrypt(user.getPassword()));

        try {
            db.createUser(user);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(Messages.COULD_NOT_CREATE, user.getUsername()));
        }

        return new Result(201, user, Messages.CREATED_ACCOUNT);
    }

    @PostMapping("authenticate")
    @ResponseBody
    Result login(@RequestBody Login login) {
        this.authenticate(login.getUsername(), login.getPassword());
        String token = jwtTokenUtil.generateToken(login);

        return new Result(200, new JwtResult(token));
    }

    private void authenticate(String username, String password) {
        try {
            if(!db.authenticate(username, password))
                throw new Exception("");
        } catch(Exception e) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

}
