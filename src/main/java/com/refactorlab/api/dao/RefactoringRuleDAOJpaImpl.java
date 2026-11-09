package com.refactorlab.api.dao;

import com.refactorlab.api.entity.RefactoringRule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RefactoringRuleDAOJpaImpl implements RefactoringRuleDAO {

    private final EntityManager entityManager;

    public RefactoringRuleDAOJpaImpl(EntityManager theEntityManager) {
        entityManager = theEntityManager;
    }

    @Override
    public void save(RefactoringRule theRule) {
        entityManager.persist(theRule);
    }

    @Override
    public RefactoringRule findById(int theId) {
        return entityManager.find(RefactoringRule.class, theId);
    }

    @Override
    public List<RefactoringRule> findAll() {
        TypedQuery<RefactoringRule> theQuery = entityManager.createQuery("FROM RefactoringRule ORDER BY id", RefactoringRule.class);
        return theQuery.getResultList();
    }

    // Kategoriye göre filtre (parametreli JPQL)
    @Override
    public List<RefactoringRule> findByCategory(String theCategory) {
        TypedQuery<RefactoringRule> theQuery = entityManager.createQuery(
                "FROM RefactoringRule WHERE category = :theData ORDER BY id", RefactoringRule.class);
        theQuery.setParameter("theData", theCategory);
        return theQuery.getResultList();
    }

    // Analiz motorunun bulduğu koku adına göre kural arar; yoksa null
    @Override
    public RefactoringRule findBySmellName(String theSmellName) {
        TypedQuery<RefactoringRule> theQuery = entityManager.createQuery(
                "FROM RefactoringRule WHERE smellName = :theData", RefactoringRule.class);
        theQuery.setParameter("theData", theSmellName);
        List<RefactoringRule> theResults = theQuery.getResultList();
        return theResults.isEmpty() ? null : theResults.get(0);
    }

    @Override
    public RefactoringRule update(RefactoringRule theRule) {
        return entityManager.merge(theRule);
    }

    @Override
    public void deleteById(int theId) {
        RefactoringRule theRule = entityManager.find(RefactoringRule.class, theId);
        if (theRule != null) {
            entityManager.remove(theRule);
        }
    }

    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(r) FROM RefactoringRule r", Long.class).getSingleResult();
    }
}
