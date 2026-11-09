package com.refactorlab.api.config;

import com.refactorlab.api.common.RefactoringCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Java tabanlı bean tanımları (@Configuration + @Bean)
@Configuration
public class AppConfig {

    // Parolalar Argon2id ile hash'lenir (Bouncy Castle gerektirir)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    // @Lazy: uygulama açılırken DEĞİL, ilk analiz isteğinde oluşturulur.
    // @PostConstruct / @PreDestroy metotları RefactoringCatalog sınıfında.
    @Bean
    @Lazy
    public RefactoringCatalog refactoringCatalog() {
        return new RefactoringCatalog();
    }
}
