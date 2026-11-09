package com.refactorlab.api.common;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

// Martin Fowler refactoring kataloğu + SQL optimizasyon kılavuzunun bellek içi bilgi bankası.
// @Component DEĞİL: AppConfig içinde @Bean + @Lazy ile tanımlanır.
public class RefactoringCatalog {

    private static final Logger logger = LoggerFactory.getLogger(RefactoringCatalog.class);

    // koku adı -> {teknik, modernizasyon ipucu}
    private final Map<String, String[]> entries = new LinkedHashMap<>();

    public RefactoringCatalog() {
        logger.info(">>> RefactoringCatalog constructor calisti (@Lazy: ilk kullanimda olusur)");
    }

    // Bean oluşturulduktan sonra bir kez çalışır: kataloğu yükler
    @PostConstruct
    public void loadCatalog() {
        entries.put("Long Method", new String[]{"Extract Method", "Split the method into small, well-named methods that each do one thing."});
        entries.put("Deeply Nested Conditionals", new String[]{"Replace Nested Conditional with Guard Clauses", "Return early for edge cases so the main path is not nested."});
        entries.put("Complex Conditional", new String[]{"Decompose Conditional / Replace Conditional with Polymorphism", "Move each branch into its own method or strategy class."});
        entries.put("Long Parameter List", new String[]{"Introduce Parameter Object", "Group related parameters into a small value object."});
        entries.put("Magic Number", new String[]{"Replace Magic Number with Symbolic Constant", "Declare a named static final constant."});
        entries.put("Swallowed Exception", new String[]{"Replace Error Code with Exception", "Log or rethrow; never leave a catch block empty."});
        entries.put("Duplicated Code", new String[]{"Extract Method / Pull Up Method", "Move the repeated lines into one shared method."});
        entries.put("N+1 Query", new String[]{"JOIN FETCH / Batch Fetching", "Load the related data with one JOIN FETCH query instead of one query per item."});
        entries.put("Select Star", new String[]{"Select Only Needed Columns", "List the needed columns explicitly to reduce I/O."});
        entries.put("Leading Wildcard Like", new String[]{"Use Full-Text Index / Prefix Search", "Avoid LIKE '%x'; use a FULLTEXT index or prefix search 'x%'."});
        entries.put("Function On Indexed Column", new String[]{"Make Predicate SARGable", "Do not wrap the indexed column in a function; compare with a range instead."});
        entries.put("Nested In Subquery", new String[]{"Replace Subquery with JOIN / EXISTS", "Rewrite IN (SELECT ...) as a JOIN or EXISTS."});
        entries.put("Unbounded Update Or Delete", new String[]{"Add WHERE Clause", "Always restrict UPDATE/DELETE with a WHERE condition."});
        entries.put("Too Many Joins", new String[]{"Denormalize / Split Query", "Split the query or add a read model for the report."});
        entries.put("Or Chain", new String[]{"Replace OR Chain with IN", "Use column IN (...) so the index can be used."});
        entries.put("Missing Filter", new String[]{"Add Pagination / WHERE", "Add a WHERE filter or LIMIT to avoid reading the whole table."});
        logger.info(">>> @PostConstruct: RefactoringCatalog {} kayit ile yuklendi", entries.size());
    }

    // Uygulama kapanırken bir kez çalışır: kaynakları temizler
    @PreDestroy
    public void clearCatalog() {
        entries.clear();
        logger.info(">>> @PreDestroy: RefactoringCatalog temizlendi");
    }

    public String getTechnique(String theSmellName) {
        String[] theEntry = entries.get(theSmellName);
        return theEntry == null ? null : theEntry[0];
    }

    public String getHint(String theSmellName) {
        String[] theEntry = entries.get(theSmellName);
        return theEntry == null ? null : theEntry[1];
    }

    public Map<String, String[]> getEntries() {
        return entries;
    }
}
