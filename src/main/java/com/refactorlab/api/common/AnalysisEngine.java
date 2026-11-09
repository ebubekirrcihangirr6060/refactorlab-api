package com.refactorlab.api.common;

import com.refactorlab.api.entity.AnalysisResult;

// Aynı arayüzün birden fazla implementasyonu var:
//   JavaCodeSmellEngine  -> @Primary (varsayılan)
//   SqlQuerySmellEngine  -> @Qualifier("sqlQueryEngine") ile seçilir
public interface AnalysisEngine {

    String getEngineName();

    // Kodu inceler; engineName, detectedSmells, complexityBefore/After alanları dolu bir sonuç döner
    AnalysisResult analyze(String theSourceCode);
}
