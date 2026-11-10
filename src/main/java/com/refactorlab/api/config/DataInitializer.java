package com.refactorlab.api.config;

import com.refactorlab.api.dao.RefactoringRuleDAO;
import com.refactorlab.api.entity.CodeSnippet;
import com.refactorlab.api.entity.RefactoringRule;
import com.refactorlab.api.service.CodeSnippetService;
import com.refactorlab.api.service.RefactoringRuleService;
import com.refactorlab.api.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Uygulama ilk açıldığında örnek kullanıcıları ve verileri oluşturur (varsa dokunmaz).
// Böylece farklı bilgisayarda ek SQL çalıştırmadan proje denenebilir.
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedData(UserService theUserService,
                                      RefactoringRuleDAO theRuleDAO,
                                      RefactoringRuleService theRuleService,
                                      CodeSnippetService theSnippetService) {
        return runner -> {
            // Örnek kullanıcılar (parola: test123, Argon2id ile hash'lenir)
            theUserService.createUserIfMissing("viewer", "test123", "ROLE_VIEWER");
            theUserService.createUserIfMissing("developer", "test123", "ROLE_VIEWER", "ROLE_DEVELOPER");
            theUserService.createUserIfMissing("reviewer", "test123", "ROLE_VIEWER", "ROLE_DEVELOPER", "ROLE_REVIEWER");

            // Örnek kurallar ve kod parçaları sadece kendi tabloları boşsa eklenir (tekrar eklenmez)
            if (theRuleDAO.count() == 0) {
                theRuleService.save(new RefactoringRule("Long Method", "Bloaters", "Extract Method",
                        "Methods longer than ~30 lines should be split."));
                theRuleService.save(new RefactoringRule("Long Parameter List", "Bloaters", "Introduce Parameter Object",
                        "More than 4 parameters should be grouped."));
                theRuleService.save(new RefactoringRule("N+1 Query", "Performance", "JOIN FETCH",
                        "One query per loop item must be replaced with a single fetch join."));
                theRuleService.save(new RefactoringRule("Select Star", "SQL", "Select Only Needed Columns",
                        "SELECT * reads unnecessary columns."));
            }

            if (theSnippetService.findAll().isEmpty()) {
                theSnippetService.save(new CodeSnippet("Legacy order total",
                        "JAVA",
                        "public double total(List<Order> orders) {\n"
                                + "    double t = 0;\n"
                                + "    for (Order o : orders) {\n"
                                + "        for (Item i : itemDao.findByOrder(o.getId())) {\n"
                                + "            if (i.getPrice() > 100) { t += i.getPrice() * 0.9; } else { t += i.getPrice(); }\n"
                                + "        }\n"
                                + "    }\n"
                                + "    return t;\n"
                                + "}",
                        "HIGH", "N+1 Query"), "developer");
                theSnippetService.save(new CodeSnippet("Customer report query",
                        "SQL",
                        "SELECT * FROM customer WHERE UPPER(name) LIKE '%ALI%' AND id IN (SELECT customer_id FROM orders)",
                        "MEDIUM", "Full Table Scan"), "developer");
            }
        };
    }
}
