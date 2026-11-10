package com.refactorlab.api.common;

import com.refactorlab.api.entity.AnalysisResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// SQL / JPQL sorgularındaki performans kokularını tespit eder.
// @Qualifier("sqlQueryEngine") ile açıkça istenince enjekte edilir.
@Component
@Qualifier("sqlQueryEngine")
public class SqlQuerySmellEngine implements AnalysisEngine {

    // Sadece WHERE koşulunun içine bakar (ORDER BY / GROUP BY / HAVING / LIMIT öncesinde durur)
    private static final Pattern FUNCTION_ON_COLUMN = Pattern.compile(
            "\\bWHERE\\b(?:(?!\\b(?:ORDER BY|GROUP BY|HAVING|LIMIT)\\b).)*?\\b(UPPER|LOWER|YEAR|MONTH|DATE|SUBSTRING|CAST)\\s*\\(");
    private static final Pattern MUTATION_WITHOUT_WHERE = Pattern.compile("^\\s*(UPDATE|DELETE)\\b(?!.*\\bWHERE\\b).*", Pattern.DOTALL);

    @Override
    public String getEngineName() {
        return "SqlQuerySmellEngine";
    }

    @Override
    public AnalysisResult analyze(String theSourceCode) {
        String theSql = theSourceCode.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
        List<String> theSmells = new ArrayList<>();

        if (theSql.contains("SELECT *")) {
            theSmells.add("Select Star");
        }
        if (theSql.contains("LIKE '%")) {
            theSmells.add("Leading Wildcard Like");
        }
        if (FUNCTION_ON_COLUMN.matcher(theSql).find()) {
            theSmells.add("Function On Indexed Column");
        }
        if (theSql.matches(".*\\bIN\\s*\\(\\s*SELECT\\b.*")) {
            theSmells.add("Nested In Subquery");
        }
        if (MUTATION_WITHOUT_WHERE.matcher(theSql).matches()) {
            theSmells.add("Unbounded Update Or Delete");
        }
        if (AnalysisEngine.countOccurrences(theSql, " JOIN ") > 3) {
            theSmells.add("Too Many Joins");
        }
        if (AnalysisEngine.countOccurrences(theSql, " OR ") > 3) {
            theSmells.add("Or Chain");
        }
        if (theSql.contains("SELECT") && !theSql.contains("WHERE") && !theSql.contains("LIMIT")) {
            theSmells.add("Missing Filter");
        }

        boolean theHasSubquery = theSmells.contains("Nested In Subquery");
        boolean theFullScan = theSmells.contains("Leading Wildcard Like") || theSmells.contains("Function On Indexed Column")
                || theSmells.contains("Missing Filter");

        AnalysisResult theResult = new AnalysisResult();
        theResult.setEngineName(getEngineName());
        theResult.setDetectedSmells(theSmells.isEmpty() ? "No obvious smell" : String.join(", ", theSmells));
        theResult.setComplexityBefore(theHasSubquery ? "O(n*m)" : (theFullScan ? "O(n) full scan" : "O(log n)"));
        theResult.setComplexityAfter(theHasSubquery || theFullScan ? "O(log n) index" : theResult.getComplexityBefore());
        return theResult;
    }
}
