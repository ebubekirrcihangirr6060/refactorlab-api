package com.refactorlab.api.rest;

// Kural bulunamadığında fırlatılır -> 404
public class RefactoringRuleNotFoundException extends RuntimeException {

    public RefactoringRuleNotFoundException(String message) {
        super(message);
    }
}
