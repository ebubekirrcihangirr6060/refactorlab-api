package com.refactorlab.api.service;

import com.refactorlab.api.dao.UserDAO;
import com.refactorlab.api.entity.Role;
import com.refactorlab.api.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDAO theUserDAO, PasswordEncoder thePasswordEncoder) {
        userDAO = theUserDAO;
        passwordEncoder = thePasswordEncoder;
    }

    @Override
    @Transactional
    public void createUserIfMissing(String theUsername, String theRawPassword, String... theRoles) {
        if (userDAO.findByUsername(theUsername) != null) {
            return;
        }
        User theUser = new User(theUsername, passwordEncoder.encode(theRawPassword), true);
        userDAO.save(theUser);
        for (String theRole : theRoles) {
            userDAO.saveRole(new Role(theUser, theRole));
        }
    }
}
