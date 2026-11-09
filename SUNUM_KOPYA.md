# SUNUM KOPYA KÂĞIDI — RefactorLab API

> Sunumdan önce bir kez baştan sona oku. Dosya yolları `src/main/java/com/refactorlab/api/` altındadır.
> IntelliJ'de bir dosyayı açmak için **Ctrl+Shift+N**'ye bas ve dosya adını yaz. Belirli bir satıra gitmek için **Ctrl+G**'ye bas.

---

## 1. Proje ne yapıyor? (ezberle, 3 cümle)

> "Eski, kötü yazılmış Java kodlarını ve SQL sorgularını alıp **içindeki hataları/kötü alışkanlıkları buluyoruz**.
> Her sorun için **nasıl düzeltileceğini öneriyoruz** ve kodun ne kadar yavaş çalıştığını (Big-O) tahmin ediyoruz.
> Kodlar, analiz sonuçları ve kullanıcılar **MySQL'de saklanıyor**; kim neyi yapabilir, rollerle sınırlı."

**Basit benzetme:** Kodu bir doktora götürüyorsun. Doktor (bizim API) muayene ediyor, hastalığı ("kod kokusu") söylüyor ve tedaviyi ("refactoring tekniği") yazıyor.

**Örnek:** Veritabanında hazır duran "Legacy order total" kodu, döngünün içinde her seferinde veritabanına sorgu atıyor.
→ Sistem şunu söylüyor: *Sorun: N+1 Query · Çözüm: JOIN FETCH · Hız: O(n²) → O(n)*

---

## 2. Proje nasıl kurulu? (katmanlar)

Bir istek API'ye geldiğinde şu sırayla ilerler:

```
Postman  →  Controller (rest/)  →  Service (service/)  →  DAO (dao/)  →  MySQL
           isteği karşılar         iş kurallarını uygular   veritabanıyla konuşur
```

- **Controller:** Garson gibi düşün. Siparişi (isteği) alır, mutfağa iletir. Veritabanına kendisi dokunmaz.
- **Service:** Mutfaktaki aşçı. Kuralları kontrol eder (kod çok uzun mu, id doğru mu…).
- **DAO:** Depocu. Veritabanından veriyi getirir ve veritabanına kaydeder (`EntityManager` ile).

---

## 3. Hoca "… nerede?" diye sorarsa

| Hoca sorar | Ne söylersin (basitçe) | Açacağın dosya : satır |
|---|---|---|
| **@Lazy nerede?** | "Katalog uygulama açılırken değil, ilk analiz isteği gelince oluşturuluyor. Konsolda o anda log çıkıyor." | `config/AppConfig.java:23` ve `service/AnalysisServiceImpl.java:36` |
| **@Primary / @Qualifier?** | "İki analiz motorumuz var: Java ve SQL. Varsayılan Java (`@Primary`). SQL motorunu ismiyle istiyoruz (`@Qualifier`)." | `common/JavaCodeSmellEngine.java:17`, `common/SqlQuerySmellEngine.java:15`, kullanım: `service/AnalysisServiceImpl.java:34-35` |
| **@PostConstruct / @PreDestroy?** | "Katalog oluşunca 16 kaydı yüklüyor, uygulama kapanırken temizliyor." | `common/RefactoringCatalog.java:25` / `:47` |
| **@Configuration + @Bean?** | "Şifreleyiciyi ve kataloğu burada kendimiz tanımladık." | `config/AppConfig.java:11, 15, 22` |
| **@Value?** | "Maksimum kod uzunluğu (10000) ayar dosyasından okunuyor, constructor parametresi olarak geliyor." | `service/CodeSnippetServiceImpl.java:41` + `application.properties` |
| **Constructor injection?** | "Hiçbir yerde `@Autowired` alan yok, her şey constructor ile geliyor." | Her sınıfın constructor'ı, örnek: `service/AnalysisServiceImpl.java:31-44` |
| **@RestController?** | "İstekleri karşılayan sınıf." | `rest/CodeSnippetRestController.java:28` |
| **@Service + @Transactional?** | "İş kuralları burada. Kaydetme/silme işlemleri transaction içinde." | `service/CodeSnippetServiceImpl.java:22` |
| **@Repository + EntityManager?** | "Spring Data JPA kullanmadık, sorguları kendimiz yazdık." | `dao/CodeSnippetDAOJpaImpl.java:11` |
| **persist / merge / remove?** | "Ekleme persist, güncelleme merge, silme remove." | `dao/CodeSnippetDAOJpaImpl.java:24, 52, 60` |
| **Parametreli JPQL?** | "Dile göre filtreleme: `?language=SQL`" | `dao/CodeSnippetDAOJpaImpl.java:45` (`setParameter`) |
| **POST'ta gelen id ne oluyor?** | "Yok sayıyoruz, `setId(0)` yapıyoruz. Böylece her POST'ta yeni kayıt oluşuyor." | `rest/CodeSnippetRestController.java:60` |
| **PATCH nasıl çalışıyor?** | "Sadece gönderilen alanlar değişiyor. Gövdede `id` gönderilirse 400 dönüyoruz." | `service/CodeSnippetServiceImpl.java:95-102` (`updateValue`) |
| **Hatalar nerede yakalanıyor?** | "Tek bir merkez sınıf bütün hataları aynı JSON formatına çeviriyor." | `rest/RefactorLabRestExceptionHandler.java:16` |
| **Güvenlik?** | "HTTP Basic. Kullanıcılar ve roller MySQL'de duruyor." | `security/SecurityConfig.java:31` |
| **Şifreler nasıl saklanıyor?** | "Argon2id ile şifrelenmiş halde. Düz metin şifre veritabanında yok." | `config/AppConfig.java:17` |
| **401 / 403 ne demek?** | "401: kim olduğunu bilmiyorum (giriş yok/yanlış). 403: seni tanıyorum ama bu işe yetkin yok." | `security/RefactorLabSecurityErrorHandler.java` |
| **Actuator?** | "Uygulamanın sağlık ve bilgi sayfası." | Tarayıcıda: `localhost:8080/actuator/health` |
| **Swagger?** | "API'nin otomatik dokümantasyonu." | Tarayıcıda: `localhost:8080/swagger-ui.html` |
| **Lombok var mı?** | "Yok. Getter/setter'ları elle yazdık." | `entity/CodeSnippet.java` |

---

## 4. Kullanıcılar (şifre hepsinde `test123`)

| Kullanıcı | Ne yapabilir |
|---|---|
| `viewer` | Sadece bakar (GET) |
| `developer` | Bakar + ekler + günceller + analiz eder |
| `reviewer` | Hepsini yapar + **siler** + kuralları yönetir |

---

## 5. Canlı gösterim (Postman)

**Önce uygulamayı başlat:** IntelliJ'de `RefactorlabApiApplication` → yeşil ▶. Ya da proje klasöründe şu komutu çalıştır: `mvnw.cmd spring-boot:run`.
Tarayıcıda `localhost:8080/actuator/health` → `UP` görünüyorsa hazır.

Postman'de soldan **RefactorLab API → 1 - Code Snippets CRUD** klasörünü aç. Bir isteği seçip **Send**'e bas.

| Hoca der ki | Sen ne yaparsın |
|---|---|
| "Listele" | **Listele - GET** → Send |
| "Yeni kayıt ekle" | **Ekle - POST** → Send → cevap **201** |
| "PATCH at" | **Kısmi güncelle - PATCH** → Send → sadece başlık değişti |
| "Analiz et" | **Analiz et - POST** → Send → bulunan sorunlar + öneri + Big-O |
| "Sil" | **Sil - DELETE** → Send → cevap **200** → sonra MySQL'de göster (aşağıda) |
| "Yetkisiz biri silmeye çalışsın" | **2 - Hata Durumları → 403 - DEVELOPER ile silme** → cevap **403** |
| "Yanlış id gönder" | **2 - Hata Durumları → 404 - Olmayan kayıt** → cevap **404** |
| "Giriş yapmadan dene" | **2 - Hata Durumları → 401 - Kimlik bilgisi yok** → cevap **401** |
| "PATCH'te id göndersem?" | **2 - Hata Durumları → 400 - PATCH gövdesinde id** → cevap **400** |

Hepsini birden çalıştırmak için **RefactorLab API → ▷ Run → Run RefactorLab API**. 31 testin hepsi yeşil olmalı.

---

## 6. MySQL'de gösterme (DELETE'ten sonra)

**MySQL Workbench** ile: bağlantıyı aç ve şunu çalıştır:
```sql
SELECT id, title, language FROM refactorlab.code_snippet;
```
Silinen id'nin listede olmadığını göster.

Workbench yoksa PowerShell'de:
```
& "C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe" -u springstudent -pspringstudent -e "SELECT id, title FROM refactorlab.code_snippet;"
```

---

## 7. Bir şey ters giderse

| Sorun | Çözüm |
|---|---|
| Postman "Could not send request" diyor | Uygulama kapalı. Başlat (5. bölüm). |
| Uygulama açılmıyor, "Access denied" hatası | MySQL servisi kapalı. Görev Yöneticisi → Hizmetler → `MySQL267` → Başlat |
| "Port 8080 already in use" hatası | Uygulama zaten açık. Eski pencereyi kapat ya da onu kullan. |
| Postman'de istek 401 dönüyor | İsteğin **Authorization** sekmesinde kullanıcı adı ve şifre doğru mu, kontrol et. |
