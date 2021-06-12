package at.networkexplorer.backend.service;

import at.networkexplorer.backend.db.FileDB;
import at.networkexplorer.backend.messages.Messages;
import at.networkexplorer.backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class JwtUserDetailsService {

    @Autowired
    private FileDB db;

    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return db.getUserByUsername(username);
        }catch(NoSuchElementException e) {
            throw new UsernameNotFoundException(String.format(Messages.USERNAME_NOT_FOUND, username));
        }
    }

}
