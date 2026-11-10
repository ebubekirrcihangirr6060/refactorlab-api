package com.refactorlab.api.common;

import com.refactorlab.api.entity.AnalysisResult;

// Aynı arayüzün birden fazla implementasyonu var:
//   JavaCodeSmellEngine  -> @Primary (varsayılan)
//   SqlQuerySmellEngine  -> @Qualifier("sqlQueryEngine") ile seçilir
public interface AnalysisEngine {

    String getEngineName();

    // Kodu inceler; engineName, detectedSmells, complexityBefore/After alanları dolu bir sonuç döner
    AnalysisResult analyze(String theSourceCode);

    // İki motorun ortak yardımcı metodu: bir metnin kaç kez geçtiğini sayar
    static int countOccurrences(String theText, String theToken) {
        int theCount = 0;
        int theIndex = theText.indexOf(theToken);
        while (theIndex != -1) {
            theCount++;
            theIndex = theText.indexOf(theToken, theIndex + theToken.length());
        }
        return theCount;
    }
}
