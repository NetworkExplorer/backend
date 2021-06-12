package at.networkexplorer.backend.api;

import at.networkexplorer.backend.api.response.Result;
import at.networkexplorer.backend.beans.UserPermission;
import at.networkexplorer.backend.config.JwtTokenUtil;
import at.networkexplorer.backend.db.FileDB;
import at.networkexplorer.backend.exceptions.InsufficientPermissionsException;
import at.networkexplorer.backend.messages.Messages;
import at.networkexplorer.backend.model.User;
import at.networkexplorer.backend.pojos.Token;
import at.networkexplorer.backend.pojos.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.NoSuchElementException;

@CrossOrigin(origins = "http://localhost:15000", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST})
@RequestMapping("api/v1/user")
@RestController
public class UserController {

    // https://www.javainuse.com/spring/boot-jwt

    @Autowired
    private FileDB db;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Creates a new User
     * @param toAdd <a href="#{@link}">{@link User}</a> to create
     * @return <a href="#{@link}">{@link Result}</a>
     */
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

    /**
     * Deletes a User
     * @param toRem <a href="#{@link}">{@link User}</a> to delete
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @DeleteMapping("")
    @ResponseBody
    Result deleteUser(@RequestBody User toRem) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.MANAGE_USER)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.MANAGE_USER));
        }

        try {
            db.removeUser(toRem);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(Messages.USER_REMOVE_ERROR, toRem.getUsername()));
        }

        return new Result(201, toRem, Messages.REMOVED_ACCOUNT);
    }

    /**
     * Changes a User's attributes
     * @param toChange <a href="#{@link}">{@link User}</a> object that only contains the values that should be changed; the others should be null
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @PutMapping("")
    @ResponseBody
    Result changeUser(@RequestBody User toChange) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.MANAGE_USER)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.MANAGE_USER));
        }

        User changed;
        try {
            changed = db.getUserByUsername(toChange.getUsername());
        }catch (NoSuchElementException e) {
            throw new IllegalArgumentException(String.format(Messages.USER_UPDATE_ERROR, toChange.getUsername()));
        }

        if(toChange.getPassword() != null)
            changed.setPassword(db.encrypt(toChange.getPassword()));
        if(toChange.getPermissions() != null)
            changed.setPermissions(toChange.getPermissions());

        try {
            db.store();
        } catch (IOException e) {
            throw new NullPointerException(Messages.ERROR_DB);
        }

        return new Result(201, toChange, Messages.UPDATED_ACCOUNT);
    }

    /**
     * Method to login. Checks username and password and returns a JWT
     * @param login <a href="#{@link}">{@link Login}</a>
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @PostMapping("authenticate")
    @ResponseBody
    Result login(@RequestBody Login login) {
        this.authenticate(login.getUsername(), login.getPassword());
        String token = null;
        try {
            token = jwtTokenUtil.generateToken(login);
        } catch (IOException | NullPointerException e) {
            throw new IllegalArgumentException(Messages.ERROR_JWT);
        }

        return new Result(200, new Token(token));
    }

    /**
     * Logs a user out by invalidating the JWT
     * @param jwt <a href="#{@link}">{@link Token}</a> to invalidate
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @PostMapping("logout")
    @ResponseBody
    Result logout(@RequestBody Token jwt) {
        this.invalidate(jwt.getToken());
        return new Result(200, null);
    }

    /**
     * Validates a given JWT to check whether it's expired
     * @param jwt <a href="#{@link}">{@link Token}</a> to validate
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @PostMapping("validate")
    @ResponseBody
    Result validate(@RequestBody Token jwt) {
        try {
            User user = db.getUserByUsername(jwtTokenUtil.getUsernameFromToken(jwt.getToken()));
            boolean valid = this.jwtTokenUtil.validateToken(jwt.getToken(), user);
            if(!valid) {
                user.removeJwt(jwt.getToken());
                db.store();
                throw new NullPointerException();
            }
        } catch (NullPointerException | IOException e) {
            throw new IllegalArgumentException(Messages.INVALID_JWT);
        }

        return new Result(200, null);
    }

    /**
     * Returns all users from the database. Requires the MANAGE_USER permission.
     * @return <a href="#{@link}">{@link Result}</a>
     */
    @GetMapping
    @ResponseBody
    Result getUsers() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.hasPermission(UserPermission.MANAGE_USER)) {
            throw new InsufficientPermissionsException(String.format(Messages.MISSING_PERMISSION, UserPermission.MANAGE_USER));
        }

        return new Result(200, db.getUsers());
    }

    /**
     * Authentication method to validate the username and password combination.
     * @param username Username to check
     * @param password Password to check
     * @throws IllegalArgumentException If the combination is incorrect
     */
    private void authenticate(String username, String password) throws IllegalArgumentException {
        try {
            if(!db.authenticate(username, password))
                throw new Exception("");
        } catch(Exception e) {
            throw new IllegalArgumentException(Messages.INVALID_CREDS);
        }
    }

    /**
     * Invalidates a JWT token
     * @param jwt The token to invalidate
     * @throws IllegalArgumentException If the JWT is invalid
     */
    private void invalidate(String jwt) throws IllegalArgumentException {
        try {
            db.getUserByUsername(jwtTokenUtil.getUsernameFromToken(jwt)).removeJwt(jwt);
            db.store();
        } catch (NullPointerException | IOException e) {
            throw new IllegalArgumentException(Messages.INVALID_JWT);
        }
    }

}
