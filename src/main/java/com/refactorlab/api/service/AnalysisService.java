package com.refactorlab.api.service;

import com.refactorlab.api.entity.AnalysisResult;

import java.util.List;

public interface AnalysisService {

    AnalysisResult analyzeSnippet(int theSnippetId);

    List<AnalysisResult> findBySnippetId(int theSnippetId);
}
