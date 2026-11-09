package com.refactorlab.api.dao;

import com.refactorlab.api.entity.AnalysisResult;

import java.util.List;

public interface AnalysisResultDAO {

    void save(AnalysisResult theResult);

    List<AnalysisResult> findBySnippetId(int theSnippetId);

    int deleteBySnippetId(int theSnippetId);
}
