package com.refactorlab.api.dao;

import com.refactorlab.api.entity.Role;
import com.refactorlab.api.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDAOJpaImpl implements UserDAO {

    private final EntityManager entityManager;

    public UserDAOJpaImpl(EntityManager theEntityManager) {
        entityManager = theEntityManager;
    }

    @Override
    public User findByUsername(String theUsername) {
        TypedQuery<User> theQuery = entityManager.createQuery("FROM User WHERE username = :theData", User.class);
        theQuery.setParameter("theData", theUsername);
        List<User> theUsers = theQuery.getResultList();
        return theUsers.isEmpty() ? null : theUsers.get(0);
    }

    @Override
    public void save(User theUser) {
        entityManager.persist(theUser);
    }

    @Override
    public void saveRole(Role theRole) {
        entityManager.persist(theRole);
    }
}
