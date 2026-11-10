package com.refactorlab.api.service;

import com.refactorlab.api.common.AnalysisEngine;
import com.refactorlab.api.common.RefactoringCatalog;
import com.refactorlab.api.dao.AnalysisResultDAO;
import com.refactorlab.api.dao.CodeSnippetDAO;
import com.refactorlab.api.dao.RefactoringRuleDAO;
import com.refactorlab.api.entity.AnalysisResult;
import com.refactorlab.api.entity.CodeSnippet;
import com.refactorlab.api.entity.RefactoringRule;
import com.refactorlab.api.rest.CodeSnippetNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final CodeSnippetDAO codeSnippetDAO;
    private final AnalysisResultDAO analysisResultDAO;
    private final RefactoringRuleDAO refactoringRuleDAO;
    private final AnalysisEngine defaultEngine;
    private final AnalysisEngine sqlEngine;
    private final RefactoringCatalog refactoringCatalog;

    public AnalysisServiceImpl(CodeSnippetDAO theCodeSnippetDAO,
                               AnalysisResultDAO theAnalysisResultDAO,
                               RefactoringRuleDAO theRefactoringRuleDAO,
                               AnalysisEngine theDefaultEngine,                                  // @Primary -> JavaCodeSmellEngine
                               @Qualifier("sqlQueryEngine") AnalysisEngine theSqlEngine,          // @Qualifier -> SqlQuerySmellEngine
                               @Lazy RefactoringCatalog theRefactoringCatalog) {                  // @Lazy -> ilk kullanımda oluşur
        codeSnippetDAO = theCodeSnippetDAO;
        analysisResultDAO = theAnalysisResultDAO;
        refactoringRuleDAO = theRefactoringRuleDAO;
        defaultEngine = theDefaultEngine;
        sqlEngine = theSqlEngine;
        refactoringCatalog = theRefactoringCatalog;
    }

    // Analiz yapılır ve sonucu (revizyon) kaydedilir: tek transaction
    @Override
    @Transactional
    public AnalysisResult analyzeSnippet(int theSnippetId) {
        CodeSnippet theSnippet = findSnippet(theSnippetId);

        // Dile göre motor seçimi
        boolean theIsQuery = "SQL".equals(theSnippet.getLanguage()) || "JPQL".equals(theSnippet.getLanguage());
        AnalysisEngine theEngine = theIsQuery ? sqlEngine : defaultEngine;
        AnalysisResult theResult = theEngine.analyze(theSnippet.getSourceCode());

        // Kurallar tek sorguyla okunur (koku başına ayrı sorgu = N+1 olurdu)
        Map<String, RefactoringRule> theRulesBySmell = new HashMap<>();
        for (RefactoringRule theRule : refactoringRuleDAO.findAll()) {
            theRulesBySmell.put(theRule.getSmellName(), theRule);
        }

        // Her koku için: önce DB'deki kurumsal kural, yoksa Fowler kataloğu
        List<String> theTechniques = new ArrayList<>();
        List<String> theHints = new ArrayList<>();
        for (String theSmell : theResult.getDetectedSmells().split(",\\s*")) {
            RefactoringRule theRule = theRulesBySmell.get(theSmell);
            String theTechnique = theRule != null ? theRule.getTechnique() : refactoringCatalog.getTechnique(theSmell);
            String theHint = refactoringCatalog.getHint(theSmell);
            if (theTechnique != null) {
                theTechniques.add(theSmell + " -> " + theTechnique);
            }
            if (theHint != null) {
                theHints.add(theHint);
            }
        }

        theResult.setSnippet(theSnippet);
        theResult.setSuggestedTechniques(theTechniques.isEmpty() ? "No refactoring needed" : String.join("; ", theTechniques));
        theResult.setModernizationHint(theHints.isEmpty() ? "Code looks clean." : String.join(" ", theHints));
        theResult.setCreatedAt(LocalDateTime.now());
        analysisResultDAO.save(theResult);
        return theResult;
    }

    @Override
    public List<AnalysisResult> findBySnippetId(int theSnippetId) {
        findSnippet(theSnippetId);
        return analysisResultDAO.findBySnippetId(theSnippetId);
    }

    private CodeSnippet findSnippet(int theSnippetId) {
        CodeSnippet theSnippet = codeSnippetDAO.findById(theSnippetId);
        if (theSnippet == null) {
            throw new CodeSnippetNotFoundException("Code snippet id not found - " + theSnippetId);
        }
        return theSnippet;
    }
}
