package com.refactorlab.api.dao;

import com.refactorlab.api.entity.RefactoringRule;

import java.util.List;

public interface RefactoringRuleDAO {

    void save(RefactoringRule theRule);

    RefactoringRule findById(int theId);

    List<RefactoringRule> findAll();

    List<RefactoringRule> findByCategory(String theCategory);

    RefactoringRule findBySmellName(String theSmellName);

    RefactoringRule update(RefactoringRule theRule);

    void deleteById(int theId);

    long count();
}
