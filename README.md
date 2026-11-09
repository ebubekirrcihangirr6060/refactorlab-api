# RefactorLab API — Çalıştırma Rehberi

Legacy kod parçalarını ve SQL sorgularını alıp **kod kokularını (code smells)** tespit eden, Martin Fowler refactoring kataloğuyla eşleştiren ve **refactoring önerisi + Big-O tahmini** üreten Spring Boot REST API'si.

Mimari: `Controller → Service → DAO (EntityManager) → MySQL`

## 1. Gerekli yazılımlar
| Yazılım | Sürüm | Not |
|---|---|---|
| JDK | 25 (Eclipse Temurin önerilir) | `java -version` ile kontrol |
| MySQL Server | 8.x veya üstü | 3306 portunda çalışmalı |
| Maven | **Gerekmez** | Proje içindeki `mvnw` / `mvnw.cmd` kullanılır |
| Postman | güncel | `postman/` klasöründeki koleksiyon içe aktarılır |
| IDE (opsiyonel) | IntelliJ IDEA | `pom.xml` açılarak proje olarak yüklenir |

Spring Boot **4.1.2**, Hibernate 7, Jackson 3, Spring Security 7.

## 2. Veritabanı ayarları
1. MySQL'de uygulama kullanıcısını oluştur (bir kez, root ile):
   ```bash
   mysql -u root -p < sql/01-create-user.sql
   ```
   (MySQL Workbench kullanıyorsan dosyayı açıp çalıştırman yeterli.)
2. Veritabanı (`refactorlab`) ve tablolar uygulama ilk açıldığında **otomatik oluşur** (`createDatabaseIfNotExist=true`, `ddl-auto=update`). İstersen elle oluşturmak için: `sql/02-refactorlab-schema.sql`.
3. Bağlantı ayarları `src/main/resources/application.properties` dosyasında:
   | Ayar | Varsayılan | Ortam değişkeni ile değiştirme |
   |---|---|---|
   | URL | `jdbc:mysql://localhost:3306/refactorlab` | `DB_URL` |
   | Kullanıcı | `springstudent` | `DB_USERNAME` |
   | Şifre | `springstudent` | `DB_PASSWORD` |

Veriler MySQL'de kalıcıdır; uygulama yeniden başlatılınca korunur.

## 3. Uygulamayı başlatma
Windows:
```bash
mvnw.cmd spring-boot:run
```
macOS / Linux / Git Bash:
```bash
./mvnw spring-boot:run
```
IntelliJ: `RefactorlabApiApplication` sınıfını çalıştır.

Uygulama `http://localhost:8080` adresinde açılır. İlk açılışta örnek kullanıcılar, kurallar ve iki örnek kod parçası otomatik eklenir.

## 4. Adresler
| Ne | Adres | Kimlik |
|---|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html | Gerekmez (denemek için "Authorize") |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | Gerekmez |
| Sağlık | http://localhost:8080/actuator/health | Gerekmez |
| Uygulama bilgisi | http://localhost:8080/actuator/info | Gerekmez |
| Diğer actuator uçları (`/actuator/beans`, `/mappings`…) | | `reviewer` |
| Kod parçaları | http://localhost:8080/api/snippets | aşağıya bak |
| Refactoring kuralları | http://localhost:8080/api/rules | aşağıya bak |

## 5. Örnek kullanıcılar ve roller (HTTP Basic)
Parolalar veritabanında **Argon2id** hash'i olarak saklanır.

| Kullanıcı | Parola | Roller | Yapabildikleri |
|---|---|---|---|
| `viewer` | `test123` | VIEWER | Sadece okuma (GET) |
| `developer` | `test123` | VIEWER, DEVELOPER | Okuma + snippet ekleme/güncelleme (POST, PUT, PATCH) + analiz |
| `reviewer` | `test123` | VIEWER, DEVELOPER, REVIEWER | Hepsi + silme (DELETE) + kural yönetimi + actuator |

## 6. API uç noktaları
| Metot | URL | Rol | Açıklama |
|---|---|---|---|
| GET | `/api/snippets` | VIEWER | Liste (`?language=SQL` ile filtre) |
| GET | `/api/snippets/{id}` | VIEWER | Tek kayıt |
| POST | `/api/snippets` | DEVELOPER | Ekle (201, gövdedeki id yok sayılır) |
| PUT | `/api/snippets/{id}` | DEVELOPER | Tam güncelleme |
| PATCH | `/api/snippets/{id}` | DEVELOPER | Kısmi güncelleme (gövdede `id` → 400) |
| DELETE | `/api/snippets/{id}` | REVIEWER | Sil |
| POST | `/api/snippets/{id}/analyses` | DEVELOPER | Analiz et (koku, teknik, Big-O) |
| GET | `/api/snippets/{id}/analyses` | VIEWER | Analiz geçmişi |
| GET | `/api/rules`, `/api/rules/{id}` | VIEWER | Kurallar (`?category=Bloaters`) |
| POST/PUT/PATCH/DELETE | `/api/rules...` | REVIEWER | Kural yönetimi |

Hata gövdesi her zaman aynı formattadır:
```json
{ "status": 404, "message": "Code snippet id not found - 999", "timeStamp": 1762700000000 }
```
`400` geçersiz istek · `401` kimlik yok/yanlış · `403` yetki yok · `404` bulunamadı.

## 7. Postman
`postman/RefactorLab.postman_collection.json` dosyasını Postman'e **Import** et. `baseUrl` ve kullanıcı bilgileri koleksiyon değişkenlerindedir. İstekleri yukarıdan aşağıya sırayla çalıştırabilirsin.
