package com.refactorlab.api.service;

import com.refactorlab.api.entity.CodeSnippet;

import java.util.List;
import java.util.Map;

public interface CodeSnippetService {

    List<CodeSnippet> findAll();

    List<CodeSnippet> findByLanguage(String theLanguage);

    CodeSnippet findById(int theId);

    CodeSnippet save(CodeSnippet theSnippet, String theOwnerUsername);

    CodeSnippet update(int theId, CodeSnippet theSnippet);

    CodeSnippet patch(int theId, Map<String, Object> thePatchPayload);

    void deleteById(int theId);
}
