package com.refactorlab.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig {

    // Kullanıcılar, aktiflik ve roller veritabanından okunur (users + roles tabloları)
    @Bean
    public UserDetailsManager userDetailsManager(DataSource theDataSource) {
        JdbcUserDetailsManager theManager = new JdbcUserDetailsManager(theDataSource);
        theManager.setUsersByUsernameQuery(
                "SELECT username, password, enabled FROM users WHERE username = ?");
        theManager.setAuthoritiesByUsernameQuery(
                "SELECT u.username, r.role FROM roles r JOIN users u ON r.user_id = u.id WHERE u.username = ?");
        return theManager;
    }

    // Yetki düzeyleri: okuma=VIEWER, ekleme/güncelleme(PATCH dahil)=DEVELOPER, silme=REVIEWER
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           RefactorLabSecurityErrorHandler theErrorHandler) throws Exception {
        http.authorizeHttpRequests(configurer -> configurer
                // Herkese açık: sağlık, bilgi, Swagger, hata sayfası
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll()
                // Diğer actuator yönetim uçları sadece REVIEWER
                .requestMatchers("/actuator/**").hasRole("REVIEWER")

                // Kurumsal refactoring kurallarını yalnızca REVIEWER yönetir
                .requestMatchers(HttpMethod.GET, "/api/rules", "/api/rules/**").hasRole("VIEWER")
                .requestMatchers("/api/rules", "/api/rules/**").hasRole("REVIEWER")

                // Kod parçaları
                .requestMatchers(HttpMethod.GET, "/api/**").hasRole("VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/**").hasRole("DEVELOPER")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("DEVELOPER")
                .requestMatchers(HttpMethod.PATCH, "/api/**").hasRole("DEVELOPER")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("REVIEWER")
                .anyRequest().authenticated()
        );

        // HTTP Basic kimlik doğrulama; 401 cevabı JSON gövdeli
        http.httpBasic(basic -> basic.authenticationEntryPoint(theErrorHandler));

        // 401 (kimlik yok/yanlış) ve 403 (yetki yok) JSON gövdeli
        http.exceptionHandling(handling -> handling
                .authenticationEntryPoint(theErrorHandler)
                .accessDeniedHandler(theErrorHandler));

        // REST API: durumsuz, CSRF gerekmez
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());

        return http.build();
    }
}
