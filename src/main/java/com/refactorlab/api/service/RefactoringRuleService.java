package com.refactorlab.api.service;

import com.refactorlab.api.entity.RefactoringRule;

import java.util.List;
import java.util.Map;

public interface RefactoringRuleService {

    List<RefactoringRule> findAll();

    List<RefactoringRule> findByCategory(String theCategory);

    RefactoringRule findById(int theId);

    RefactoringRule save(RefactoringRule theRule);

    RefactoringRule update(int theId, RefactoringRule theRule);

    RefactoringRule patch(int theId, Map<String, Object> thePatchPayload);

    void deleteById(int theId);
}
