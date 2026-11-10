package com.refactorlab.api.common;

import com.refactorlab.api.entity.AnalysisResult;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Uygulama kodundaki (Java vb.) kod kokularını kural tabanlı tespit eder.
// @Primary: AnalysisEngine istenip qualifier verilmezse bu enjekte edilir.
@Component
@Primary
public class JavaCodeSmellEngine implements AnalysisEngine {

    private static final Pattern PARAMETER_LIST = Pattern.compile("\\w+\\s+\\w+\\s*\\(([^)]*)\\)\\s*\\{");
    private static final Pattern MAGIC_NUMBER = Pattern.compile("[^\\w.](\\d{2,})[^\\w.]");
    private static final Pattern EMPTY_CATCH = Pattern.compile("catch\\s*\\([^)]*\\)\\s*\\{\\s*}");
    private static final Pattern LOOP = Pattern.compile("\\b(for|while)\\s*\\(");
    // Veritabanı çağrısı işaretleri (stream'in findFirst/findAny metotları hariç)
    private static final Pattern DB_CALL_IN_CODE = Pattern.compile("(?i)(\\bfind(?!First|Any)\\w*\\(|createQuery|executeQuery|repository\\.|dao\\.)");

    @Override
    public String getEngineName() {
        return "JavaCodeSmellEngine";
    }

    @Override
    public AnalysisResult analyze(String theSourceCode) {
        List<String> theSmells = new ArrayList<>();
        String[] theLines = theSourceCode.split("\\R");

        if (theLines.length > 30) {
            theSmells.add("Long Method");
        }
        if (maxNestingDepth(theSourceCode) > 3) {
            theSmells.add("Deeply Nested Conditionals");
        }
        if (AnalysisEngine.countOccurrences(theSourceCode, "if (") + AnalysisEngine.countOccurrences(theSourceCode, "if(") > 5
                || theSourceCode.contains("switch")) {
            theSmells.add("Complex Conditional");
        }
        Matcher theParamMatcher = PARAMETER_LIST.matcher(theSourceCode);
        while (theParamMatcher.find()) {
            if (theParamMatcher.group(1).split(",").length > 4) {
                theSmells.add("Long Parameter List");
                break;
            }
        }
        if (MAGIC_NUMBER.matcher(theSourceCode).find()) {
            theSmells.add("Magic Number");
        }
        if (EMPTY_CATCH.matcher(theSourceCode).find()) {
            theSmells.add("Swallowed Exception");
        }
        if (hasDuplicatedLines(theLines)) {
            theSmells.add("Duplicated Code");
        }

        Set<Integer> theLoopBraces = findLoopBodyBraces(theSourceCode);
        int theLoopDepth = maxLoopDepth(theSourceCode, theLoopBraces);
        boolean theDbCallInLoop = hasDbCallInsideLoop(theSourceCode, theLoopBraces);
        if (theDbCallInLoop) {
            theSmells.add("N+1 Query");
        }

        AnalysisResult theResult = new AnalysisResult();
        theResult.setEngineName(getEngineName());
        theResult.setDetectedSmells(theSmells.isEmpty() ? "No obvious smell" : String.join(", ", theSmells));
        theResult.setComplexityBefore(theLoopDepth >= 2 ? "O(n^" + theLoopDepth + ")" : (theLoopDepth == 1 ? "O(n)" : "O(1)"));
        theResult.setComplexityAfter(theDbCallInLoop || theLoopDepth >= 2 ? "O(n)" : theResult.getComplexityBefore());
        return theResult;
    }

    // Süslü parantez derinliğinin en fazla kaç olduğunu bulur (metot gövdesi 1 sayılır)
    private int maxNestingDepth(String theSourceCode) {
        int theDepth = 0;
        int theMax = 0;
        for (char c : theSourceCode.toCharArray()) {
            if (c == '{') {
                theDepth++;
                theMax = Math.max(theMax, theDepth);
            } else if (c == '}') {
                theDepth--;
            }
        }
        return theMax - 1;
    }

    // Döngü gövdelerinin açılış '{' konumlarını bulur.
    // Sadece başlığın ')' karakterinden hemen sonra '{' gelen döngüler sayılır (süslü parantezsiz döngü başka bloğu kapmaz).
    private Set<Integer> findLoopBodyBraces(String theSourceCode) {
        Set<Integer> theLoopBraces = new HashSet<>();
        Matcher theLoopMatcher = LOOP.matcher(theSourceCode);
        while (theLoopMatcher.find()) {
            int theClose = findMatching(theSourceCode, theLoopMatcher.end() - 1, '(', ')');
            if (theClose == -1) {
                continue;
            }
            int theNext = theClose + 1;
            while (theNext < theSourceCode.length() && Character.isWhitespace(theSourceCode.charAt(theNext))) {
                theNext++;
            }
            if (theNext < theSourceCode.length() && theSourceCode.charAt(theNext) == '{') {
                theLoopBraces.add(theNext);
            }
        }
        return theLoopBraces;
    }

    // N+1: veritabanı çağrısı bir döngü gövdesinin İÇİNDE mi?
    private boolean hasDbCallInsideLoop(String theSourceCode, Set<Integer> theLoopBraces) {
        for (int theOpen : theLoopBraces) {
            int theClose = findMatching(theSourceCode, theOpen, '{', '}');
            String theBody = theSourceCode.substring(theOpen, theClose == -1 ? theSourceCode.length() : theClose);
            if (DB_CALL_IN_CODE.matcher(theBody).find()) {
                return true;
            }
        }
        return false;
    }

    // theOpenIndex'teki açılış karakterinin kapanışını bulur; yoksa -1
    private int findMatching(String theText, int theOpenIndex, char theOpen, char theClose) {
        int theDepth = 0;
        for (int i = theOpenIndex; i < theText.length(); i++) {
            char c = theText.charAt(i);
            if (c == theOpen) {
                theDepth++;
            } else if (c == theClose && --theDepth == 0) {
                return i;
            }
        }
        return -1;
    }

    // İç içe döngü sayısını kabaca tahmin eder (Big-O tahmini için)
    private int maxLoopDepth(String theSourceCode, Set<Integer> theLoopBraces) {
        int theDepth = 0;
        int theMax = 0;
        List<Boolean> theStack = new ArrayList<>();
        for (int i = 0; i < theSourceCode.length(); i++) {
            char c = theSourceCode.charAt(i);
            if (c == '{') {
                boolean theIsLoop = theLoopBraces.contains(i);
                theStack.add(theIsLoop);
                if (theIsLoop) {
                    theDepth++;
                    theMax = Math.max(theMax, theDepth);
                }
            } else if (c == '}' && !theStack.isEmpty()) {
                if (theStack.remove(theStack.size() - 1)) {
                    theDepth--;
                }
            }
        }
        return theMax;
    }

    private boolean hasDuplicatedLines(String[] theLines) {
        Set<String> theSeen = new HashSet<>();
        for (String theLine : theLines) {
            String theTrimmed = theLine.trim();
            if (theTrimmed.length() > 25 && !theSeen.add(theTrimmed)) {
                return true;
            }
        }
        return false;
    }
}
