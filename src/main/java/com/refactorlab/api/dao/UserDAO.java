package com.refactorlab.api.dao;

import com.refactorlab.api.entity.Role;
import com.refactorlab.api.entity.User;

public interface UserDAO {

    User findByUsername(String theUsername);

    void save(User theUser);

    void saveRole(Role theRole);
}
