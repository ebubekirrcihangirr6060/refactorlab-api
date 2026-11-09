# PROJE DURUMU — Yapay zekâ / ekip devir dosyası

> **Bu projede çalışacak her yapay zekâ (ChatGPT, Gemini, Antigravity, Copilot…) ve ekip üyesi önce bu dosyayı okumalıdır.**
> Ders: Sunucu Uygulama. Teslim: 10 Kasım 17:00 (her geç saat −5 puan). Sunum: 11 Kasım, hocanın odasında canlı gösterim.

## 1. DEĞİŞMEZ KURALLAR (ihlal = puan kaybı)
1. **Lombok YASAK.** Getter/setter/constructor elle yazılır.
2. **Spring Data JPA arayüzleri (`JpaRepository`, `CrudRepository`) ve Spring Data REST YASAK.** Veri erişimi `dao/` paketinde `EntityManager` ile elle yazılır.
3. **Sadece constructor injection.** Alanda `@Autowired` veya setter injection YOK. `@Value` da constructor parametresinde.
4. Akış daima **Controller → Service → DAO → DB**. Controller DB'ye dokunmaz, `@Transactional` sadece Service'te.
5. İsimlendirme ders kataloğundaki gibi: `XxxDAO` + `XxxDAOJpaImpl`, `XxxService` + `XxxServiceImpl`, `XxxRestController`, parametrelerde `theXxx`. Paket adlarını (`entity, dao, service, rest, common, config, security`) değiştirme.
6. Kimlik doğrulama **HTTP Basic** (JWT değil), parolalar **Argon2id**.
7. Gereklilik listesinde olmayan büyük şey ekleme (Docker, JWT, Spring AI vb.) — hoca listeyi kontrol ediyor.

## 2. Paket yapısı
```
src/main/java/com/refactorlab/api/
  RefactorlabApiApplication.java
  entity/   CodeSnippet, RefactoringRule, AnalysisResult, User, Role
  dao/      *DAO (arayüz) + *DAOJpaImpl (@Repository, EntityManager)
  service/  *Service (arayüz) + *ServiceImpl (@Service, @Transactional)
  rest/     *RestController, *NotFoundException, InvalidRequestException,
            RefactorLabErrorResponse, RefactorLabRestExceptionHandler (@RestControllerAdvice)
  common/   AnalysisEngine + JavaCodeSmellEngine(@Primary) + SqlQuerySmellEngine(@Qualifier), RefactoringCatalog
  config/   AppConfig (@Bean, @Lazy), DataInitializer (örnek veri), OpenApiConfig (Swagger)
  security/ SecurityConfig (SecurityFilterChain), RefactorLabSecurityErrorHandler (401/403 JSON)
```

## 3. Gereklilik → Kod eşleştirmesi (sunumda "nerede?" sorusu için)
Yollar `src/main/java/com/refactorlab/api/` altındadır.

| # | Gereklilik | Dosya : satır |
|---|---|---|
| 1 | Spring Boot 4.1.2 | `pom.xml` (parent version) |
| 2 | Spring Data JPA/REST yok | `dao/*DAOJpaImpl.java` — hepsi `EntityManager` |
| 3 | Controller→Service→DAO | `rest/CodeSnippetRestController` → `service/CodeSnippetServiceImpl` → `dao/CodeSnippetDAOJpaImpl` |
| 4 | @RestController | `rest/CodeSnippetRestController.java:28`, `rest/RefactoringRuleRestController.java` |
| 5 | Service arayüz + @Service | `service/CodeSnippetService.java` + `CodeSnippetServiceImpl.java:22` |
| 6 | Repository + @Repository + EntityManager | `dao/CodeSnippetDAOJpaImpl.java:11` |
| 7 | Constructor injection | Her sınıfın constructor'ı (ör. `service/AnalysisServiceImpl.java:31-44`) |
| 8 | @Configuration + @Bean | `config/AppConfig.java:11,15,22` (PasswordEncoder, RefactoringCatalog) |
| 9 | @Lazy | `config/AppConfig.java:23` (bean) + `service/AnalysisServiceImpl.java:36` (enjeksiyon noktası). Konsolda ilk analizde "RefactoringCatalog constructor calisti" logu çıkar |
| 10 | @PostConstruct / @PreDestroy | `common/RefactoringCatalog.java:25` / `:47` |
| 11 | Çoklu impl + @Primary/@Qualifier | `common/JavaCodeSmellEngine.java:17` (@Primary), `common/SqlQuerySmellEngine.java:15` (@Qualifier), kullanımı `service/AnalysisServiceImpl.java:34-35` |
| 12 | @Value → constructor parametresi | `service/CodeSnippetServiceImpl.java:41` (`refactorlab.analysis.max-code-length`, `application.properties`) |
| 13 | Actuator health + info | `application.properties` (`info.app.*`) → `/actuator/health`, `/actuator/info` |
| 14 | Actuator koruması | `security/SecurityConfig.java` (`/actuator/**` → REVIEWER) |
| 15 | MySQL kalıcı | `application.properties` (`ddl-auto=update`) |
| 16 | @Entity @Table @Column, no-arg ctor, @Id @GeneratedValue(IDENTITY) | `entity/*.java` |
| 17 | persist / find / JPQL liste / merge / remove | `dao/CodeSnippetDAOJpaImpl.java:24,30,36,52,58-60` |
| 18 | Parametreli JPQL + setParameter | `dao/CodeSnippetDAOJpaImpl.java:42-45` (`findByLanguage`) |
| 19 | @Transactional (Spring) Service'te | `service/CodeSnippetServiceImpl.java:69,81,93,110` |
| 20 | GET liste + tek (@PathVariable) | `rest/CodeSnippetRestController.java:42,51` |
| 21 | POST, id yok sayılır | `rest/CodeSnippetRestController.java:57-60` + `service/CodeSnippetServiceImpl.java:72` |
| 22 | PUT tam güncelleme | `rest/CodeSnippetRestController.java:65` |
| 23 | PATCH kısmi, id engelli | `rest/CodeSnippetRestController.java:71` → `service/CodeSnippetServiceImpl.java:95-102` (JsonMapper.updateValue) |
| 24 | DELETE | `rest/CodeSnippetRestController.java:77` |
| 25 | @RequestBody + Jackson | Controller metotları |
| 26 | Global exception handler | `rest/RefactorLabRestExceptionHandler.java:16` |
| 27 | Custom exception + ortak JSON hata modeli | `rest/CodeSnippetNotFoundException`, `InvalidRequestException`, `RefactorLabErrorResponse` (status, message, timeStamp) |
| 28 | 404 / 400 | `RefactorLabRestExceptionHandler` |
| 29 | SecurityFilterChain + HTTP Basic | `security/SecurityConfig.java:31` |
| 30 | Kullanıcı/rol DB'de | `entity/User`, `entity/Role`, `security/SecurityConfig.java:21` (JdbcUserDetailsManager) |
| 31 | Argon2id | `config/AppConfig.java:17`, `users.password` VARCHAR(255) |
| 32 | Okuma/yazma/silme farklı yetki, PATCH=güncelleme | `security/SecurityConfig.java` (GET=VIEWER, POST/PUT/PATCH=DEVELOPER, DELETE=REVIEWER) |
| 33 | 401 / 403 | `security/RefactorLabSecurityErrorHandler.java` |
| 34 | OpenAPI + Swagger UI | `config/OpenApiConfig.java`, `/swagger-ui.html`, `/v3/api-docs` |
| 35 | Çalıştırma rehberi | `README.md` |
| 36 | Postman koleksiyonu | `postman/RefactorLab.postman_collection.json` |

> Satır numaraları kod değişirse kayabilir; değişiklik yapan bu tabloyu güncellesin.

## 4. Durum
- [x] Proje iskeleti, entity, DAO, service, controller, hata yönetimi
- [x] Security (HTTP Basic, Argon2id, roller, 401/403 JSON), Actuator, Swagger
- [x] README, SQL scriptleri, Postman koleksiyonu
- [ ] Uçtan uca test (MySQL'de `springstudent` kullanıcısı oluşturulduktan sonra)
- [ ] GitHub'a push + collaborator ekleme

## 5. Yeni geliştirici için hızlı başlangıç
1. `README.md` adımlarını uygula (JDK 25, MySQL, `sql/01-create-user.sql`, `mvnw spring-boot:run`).
2. Postman koleksiyonunu import et, sırayla çalıştır.
3. Değişiklik yaparken 1. bölümdeki kurallara uy; bitince bu dosyadaki durum listesini güncelle ve commit et.
