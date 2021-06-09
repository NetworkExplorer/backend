package at.networkexplorer.backend.api;

import at.networkexplorer.backend.api.response.Result;
import at.networkexplorer.backend.beans.UserPermission;
import at.networkexplorer.backend.config.JwtTokenUtil;
import at.networkexplorer.backend.db.FileDB;
import at.networkexplorer.backend.exceptions.InsufficientPermissionsException;
import at.networkexplorer.backend.messages.Messages;
import at.networkexplorer.backend.model.User;
import at.networkexplorer.backend.pojos.JwtResult;
import at.networkexplorer.backend.pojos.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@RequestMapping("api/v1/user")
@RestController
public class UserController {

    // https://www.javainuse.com/spring/boot-jwt

    private FileDB db = FileDB.getInstance();

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("")
    @ResponseBody
    Result createUser(@RequestBody User toAdd) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.MANAGE_USER)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.MANAGE_USER));
        }

        toAdd.setPassword(db.encrypt(toAdd.getPassword()));

        try {
            db.createUser(toAdd);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(Messages.USER_CREATE_ERROR, toAdd.getUsername()));
        }

        return new Result(201, toAdd, Messages.CREATED_ACCOUNT);
    }

    @DeleteMapping("")
    @ResponseBody
    Result deleteUser(@RequestBody User toRem) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.MANAGE_USER)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.MANAGE_USER));
        }

        toRem.setPassword(db.encrypt(toRem.getPassword()));

        try {
            db.removeUser(toRem);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(Messages.USER_REMOVE_ERROR, toRem.getUsername()));
        }

        return new Result(201, toRem, Messages.REMOVED_ACCOUNT);
    }

    @PutMapping("")
    @ResponseBody
    Result changeUser(@RequestBody User toChange) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.MANAGE_USER)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.MANAGE_USER));
        }

        toChange.setPassword(db.encrypt(toChange.getPassword()));

        try {
            db.updateUser(toChange);
        } catch(IOException e) {
            throw new IllegalArgumentException(String.format(Messages.USER_UPDATE_ERROR, toChange.getUsername()));
        }

        return new Result(200, toChange, Messages.UPDATED_ACCOUNT);
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
