package com.refactorlab.api.dao;

import com.refactorlab.api.entity.AnalysisResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AnalysisResultDAOJpaImpl implements AnalysisResultDAO {

    private final EntityManager entityManager;

    public AnalysisResultDAOJpaImpl(EntityManager theEntityManager) {
        entityManager = theEntityManager;
    }

    @Override
    public void save(AnalysisResult theResult) {
        entityManager.persist(theResult);
    }

    @Override
    public List<AnalysisResult> findBySnippetId(int theSnippetId) {
        TypedQuery<AnalysisResult> theQuery = entityManager.createQuery(
                "FROM AnalysisResult WHERE snippet.id = :theData ORDER BY id DESC", AnalysisResult.class);
        theQuery.setParameter("theData", theSnippetId);
        return theQuery.getResultList();
    }

    // Snippet silinmeden önce ona bağlı analizler silinir (yabancı anahtar)
    @Override
    public int deleteBySnippetId(int theSnippetId) {
        return entityManager.createQuery("DELETE FROM AnalysisResult WHERE snippet.id = :theData")
                .setParameter("theData", theSnippetId)
                .executeUpdate();
    }
}
