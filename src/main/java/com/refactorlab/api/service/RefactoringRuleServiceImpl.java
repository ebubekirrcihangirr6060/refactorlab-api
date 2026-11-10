package com.refactorlab.api.service;

import com.refactorlab.api.dao.RefactoringRuleDAO;
import com.refactorlab.api.entity.RefactoringRule;
import com.refactorlab.api.rest.InvalidRequestException;
import com.refactorlab.api.rest.RefactoringRuleNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RefactoringRuleServiceImpl implements RefactoringRuleService {

    private final RefactoringRuleDAO refactoringRuleDAO;
    private final JsonMapper jsonMapper;
    private final Validator validator;

    public RefactoringRuleServiceImpl(RefactoringRuleDAO theRefactoringRuleDAO, JsonMapper theJsonMapper, Validator theValidator) {
        refactoringRuleDAO = theRefactoringRuleDAO;
        jsonMapper = theJsonMapper;
        validator = theValidator;
    }

    @Override
    public List<RefactoringRule> findAll() {
        return refactoringRuleDAO.findAll();
    }

    @Override
    public List<RefactoringRule> findByCategory(String theCategory) {
        return refactoringRuleDAO.findByCategory(theCategory);
    }

    @Override
    public RefactoringRule findById(int theId) {
        RefactoringRule theRule = refactoringRuleDAO.findById(theId);
        if (theRule == null) {
            throw new RefactoringRuleNotFoundException("Refactoring rule id not found - " + theId);
        }
        return theRule;
    }

    @Override
    @Transactional
    public RefactoringRule save(RefactoringRule theRule) {
        checkSmellNameIsUnique(theRule.getSmellName(), 0);
        refactoringRuleDAO.save(theRule);
        return theRule;
    }

    @Override
    @Transactional
    public RefactoringRule update(int theId, RefactoringRule theRule) {
        findById(theId);
        checkSmellNameIsUnique(theRule.getSmellName(), theId);
        theRule.setId(theId);
        return refactoringRuleDAO.update(theRule);
    }

    @Override
    @Transactional
    public RefactoringRule patch(int theId, Map<String, Object> thePatchPayload) {
        if (thePatchPayload.containsKey("id")) {
            throw new InvalidRequestException("Refactoring rule id not allowed in request body - " + theId);
        }
        RefactoringRule thePatchedRule = jsonMapper.updateValue(findById(theId), thePatchPayload);
        Set<ConstraintViolation<RefactoringRule>> theViolations = validator.validate(thePatchedRule);
        if (!theViolations.isEmpty()) {
            throw new InvalidRequestException("Validation failed - " + theViolations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; ")));
        }
        checkSmellNameIsUnique(thePatchedRule.getSmellName(), theId);
        return refactoringRuleDAO.update(thePatchedRule);
    }

    @Override
    @Transactional
    public void deleteById(int theId) {
        findById(theId);
        refactoringRuleDAO.deleteById(theId);
    }

    // İş kuralı: aynı koku adı için iki kural olamaz
    private void checkSmellNameIsUnique(String theSmellName, int theOwnId) {
        RefactoringRule theExisting = refactoringRuleDAO.findBySmellName(theSmellName);
        if (theExisting != null && theExisting.getId() != theOwnId) {
            throw new InvalidRequestException("A rule for smell '" + theSmellName + "' already exists (id " + theExisting.getId() + ")");
        }
    }
}
