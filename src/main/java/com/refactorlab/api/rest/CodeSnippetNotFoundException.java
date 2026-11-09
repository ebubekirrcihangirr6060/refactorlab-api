package com.refactorlab.api.rest;

// Kayıt bulunamadığında fırlatılır -> 404
public class CodeSnippetNotFoundException extends RuntimeException {

    public CodeSnippetNotFoundException(String message) {
        super(message);
    }
}
