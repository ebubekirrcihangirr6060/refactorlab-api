package com.refactorlab.api.dao;

import com.refactorlab.api.entity.CodeSnippet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

// Veri erişimi EntityManager ile elle yazılır (Spring Data JPA KULLANILMAZ)
@Repository
public class CodeSnippetDAOJpaImpl implements CodeSnippetDAO {

    private final EntityManager entityManager;

    // constructor injection
    public CodeSnippetDAOJpaImpl(EntityManager theEntityManager) {
        entityManager = theEntityManager;
    }

    // EKLEME: persist -> INSERT
    @Override
    public void save(CodeSnippet theSnippet) {
        entityManager.persist(theSnippet);
    }

    // KİMLİKLE BULMA: find -> SELECT ... WHERE id=?
    @Override
    public CodeSnippet findById(int theId) {
        return entityManager.find(CodeSnippet.class, theId);
    }

    // LİSTELEME: JPQL
    @Override
    public List<CodeSnippet> findAll() {
        TypedQuery<CodeSnippet> theQuery = entityManager.createQuery("FROM CodeSnippet ORDER BY id", CodeSnippet.class);
        return theQuery.getResultList();
    }

    // FİLTRELEME: parametreli JPQL + setParameter
    @Override
    public List<CodeSnippet> findByLanguage(String theLanguage) {
        TypedQuery<CodeSnippet> theQuery = entityManager.createQuery(
                "FROM CodeSnippet WHERE language = :theData ORDER BY id", CodeSnippet.class);
        theQuery.setParameter("theData", theLanguage);
        return theQuery.getResultList();
    }

    // GÜNCELLEME: merge -> UPDATE, güncel nesneyi döner
    @Override
    public CodeSnippet update(CodeSnippet theSnippet) {
        return entityManager.merge(theSnippet);
    }

    // SİLME: find + remove -> DELETE
    @Override
    public void deleteById(int theId) {
        CodeSnippet theSnippet = entityManager.find(CodeSnippet.class, theId);
        if (theSnippet != null) {
            entityManager.remove(theSnippet);
        }
    }
}
