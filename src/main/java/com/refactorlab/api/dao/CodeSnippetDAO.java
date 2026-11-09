package com.refactorlab.api.dao;

import com.refactorlab.api.entity.CodeSnippet;

import java.util.List;

public interface CodeSnippetDAO {

    void save(CodeSnippet theSnippet);

    CodeSnippet findById(int theId);

    List<CodeSnippet> findAll();

    List<CodeSnippet> findByLanguage(String theLanguage);

    CodeSnippet update(CodeSnippet theSnippet);

    void deleteById(int theId);
}
