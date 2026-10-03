# Spring Boot Learning

Bu proje, `spring-boot-learning` YouTube serisi projesinin eğitim amaçlı
kopyasıdır. Orijinal Lombok'suz akış korunur; aynı katmanların Lombok kullanan
karşılıkları dosya adlarının sonuna `Lombok` eklenerek yan yana tutulur.

Spring Boot ile kullanıcı üyelik işlemlerinin (kayıt, giriş, listeleme,
güncelleme, şifre değiştirme ve silme) klasik katmanlı mimariyle
(**Controller → Service → Repository → Database**) geliştirildiği öğrenme
projesidir.

Projede her katmanın tek bir sorumluluğu vardır. Veriler H2 veritabanında
JPA/Hibernate ile saklanır; parolalar BCrypt ile hash'lenir ve API cevaplarında
Entity yerine güvenli DTO'lar kullanılır.

## Bu README Nasıl Çalışılmalı?

1. Önce aşağıdaki mimari akışı incele.
2. **Kodun Yazılma Sırası** bölümündeki sekiz adımı sırayla takip et.
3. Her adımda önce Lombok'suz dosyayı, sonra yanındaki `*Lombok` dosyasını aç.
4. Son olarak iki REST akışını çalıştırıp aynı davranışı verdiklerini karşılaştır.

---

## Mimari: Katmanlar ve Sorumlulukları

```
İstek (HTTP)
    │
    ▼
┌─────────────┐   HTTP'yi karşılar, DTO <-> Entity dönüşümü yapar.
│  Controller │   İş kuralı İÇERMEZ.
└──────┬──────┘
       │ DTO
       ▼
┌─────────────┐   Tüm iş mantığı (business logic) burada.
│   Service   │   Doğrulama, kural kontrolü, şifre hash'leme,
└──────┬──────┘   hata (exception) fırlatma.
       │ Entity
       ▼
┌─────────────┐   Veritabanı ile konuşan tek katman.
│ Repository  │   Spring Data JPA otomatik SQL üretir.
└──────┬──────┘
       │ SQL
       ▼
┌─────────────┐
│  Database   │   H2 (in-memory), users tablosu.
└─────────────┘
```

**Altın kural:** Bir katman, sadece kendinden bir alt katmanı bilir.
Controller, Repository'yi hiç tanımaz (doğrudan çağırmaz); her zaman
Service üzerinden gider. Bu sayede katmanlar birbirinden bağımsız
test edilebilir ve değiştirilebilir.

---

## Kodun Yazılma Sırası (ve Neden Bu Sıra?)

Bir Spring Boot CRUD özelliği yazarken önerilen sıra **"veriden dışarıya
doğru"**dur — yani en içteki, en az bağımlılığı olan katmandan başlanır,
en dışa (HTTP'ye) doğru ilerlenir:

```
Entity  →  Repository  →  DTO  →  Exception  →  Config  →  Service  →  Controller  →  Test
```

Bunun tersini yapmaya çalışırsanız (örneğin önce Controller yazarsanız),
henüz var olmayan Service ve DTO sınıflarına referans vermeniz gerekir
ve kod derlenmez / IDE kırmızı gösterir. Aşağıda özellikle
**Controller, Service, DTO, Repository** için sırayı ve HER BİRİNİN
İÇİNDE hangi adımların önce geldiğini detaylıca anlatıyorum.

### 1) Entity (`entity/User.java`)
Veritabanı tablosunun Java karşılığı. Her şey buradan başlar çünkü
Repository, bu sınıfa (`JpaRepository<User, Long>`) ihtiyaç duyar.
Henüz hiçbir katman yokken Entity'nin alanları (`id`, `name`, `surname`,
`passwordHash`, `email`) belirlenir; çünkü diğer tüm katmanlar (DTO'ların hangi
alanları taşıyacağı, Service'in hangi veriyi işleyeceği) bu alanlara
göre şekillenir.

---

### 2) Repository (`repository/UserRepository.java`) — DETAYLI SIRA
Repository, Entity'den hemen sonra yazılır çünkü **Service henüz yokken
bile** Repository tek başına derlenip test edilebilir (üstünde iş
mantığı yoktur, sadece veri erişimidir).

Repository yazarken izlenecek adımlar:
1. **Arayüzü aç ve `JpaRepository<Entity, IdTipi>`'ı extend et.**
   Örn: `interface UserRepository extends JpaRepository<User, Long>`.
   Bu tek satır size `save`, `findById`, `findAll`, `deleteById`,
   `count` gibi temel metotları BEDAVA verir — bunları siz yazmazsınız.
2. **Hazır metotlar yetmiyorsa, "türetilmiş sorgu" (derived query)
   metotları ekle.** Örn: `Optional<User> findByEmail(String email)`.
   Burada SADECE metot imzasını yazarsınız; Spring Data JPA, metot
   isminden (`findBy` + alan adı) SQL'i kendisi üretir. Gövde (body)
   YAZILMAZ — arayüz olduğu için zaten yazılamaz.
3. Karmaşık bir sorgu gerekiyorsa (birden fazla tabloyu birleştirme,
   özel filtreleme) bu noktada `@Query` anotasyonu ile JPQL/SQL
   yazılabilir; ama bu projede buna gerek kalmadı.

Repository'nin Service'ten ÖNCE bitmiş olması gerekir çünkü Service,
constructor'ında `UserRepository` tipini parametre olarak ister —
tip henüz tanımlı değilse Service sınıfı yazılamaz.

---

### 3) DTO'lar (`dto/*.java`) — DETAYLI SIRA
DTO'lar Entity'den sonra, Service'ten önce yazılır. Sıra kendi
içinde de önemlidir; önce "girdi" (request) DTO'ları, sonra "çıktı"
(response) DTO'ları yazılır — çünkü Service metotlarını tasarlarken
önce "bu metot ne alıyor" sorusuna, sonra "ne döndürüyor" sorusuna
cevap verilir.

**a) Önce İstek (Request) DTO'ları:**
- `CreateUserRequest` (kayıt için gerekli alanlar: name, surname,
  password, email)
- `UpdateUserRequest` (sadece güncellenebilir alanlar: name, surname)
- `ChangePasswordRequest` (id, newPassword)
- `UserLoginRequest` (email, password)

Her birinde hangi alanların zorunlu olduğu `@NotBlank` / `@NotNull`
ile işaretlenir. Bu DTO'lar Entity'nin BİREBİR kopyası DEĞİLDİR;
örneğin `CreateUserRequest`'te `id` yoktur (henüz üretilmedi),
`passwordHash` yoktur (henüz hash'lenmedi, ham `password` vardır).

**b) Sonra Cevap (Response) DTO'ları:**
- `UserResponse` (id, name, surname, email — **password YOK**, kasıtlı olarak
  çıkarıldı)
- `LoginResponse` (login durumu + UserResponse)

`UserResponse` içine konulan `static fromEntity(User user)` metodu,
Entity → DTO dönüşümünü tek bir yerde toplar; bu metodu Entity zaten
tanımlı olduğu için (adım 1) buraya yazabiliriz.

**Neden Service'ten önce?** Çünkü Service katmanındaki her metodun
imzası (`public User createUser(CreateUserRequest request)` gibi)
bu DTO tiplerini parametre veya dönüş tipi olarak kullanır. DTO'lar
yoksa Service metotlarının imzası bile yazılamaz.

---

### 4) Exception sınıfları (`exception/*.java`)
`UserNotFoundException`, `InvalidCredentialsException` gibi özel hata
tipleri + bunları HTTP durum koduna çeviren `GlobalExceptionHandler`.
Önce münferit exception sınıfları (`extends RuntimeException`) yazılır,
`GlobalExceptionHandler` EN SON yazılır çünkü o, tüm exception
sınıflarını `@ExceptionHandler` ile referans alır. Service katmanı bu
sınıfları fırlatacağı için Service'ten hemen önce hazır olmalıdır.

### 5) Config (`config/SecurityConfig.java`)
`PasswordEncoder` bean'i gibi, birden fazla yerde paylaşılacak
nesnelerin tanımlandığı yer. Service, bu bean'i constructor'ında
enjekte edeceği için Service'ten önce hazır olmalıdır.

---

### 6) Service (`service/UserService.java`) — DETAYLI SIRA
Service, Repository + DTO + Exception + Config'in hepsini bir araya
getirdiği için hepsinden SONRA yazılır. Service'in kendi içinde de
önerilen yazım sırası şöyledir:

1. **Constructor / bağımlılıklar önce.** `UserRepository` ve
   `PasswordEncoder` alanlarını `final` yapıp constructor ile enjekte
   edin. Bu, metotları yazmadan önce "elimde hangi araçlar var"
   sorusuna cevap verir.
2. **"Yardımcı" / tekrar kullanılan metodu erken yazın.**
   Bu projede `getUserById(Long id)` böyle bir metottur — hem
   `updateUser`, hem `changePassword`, hem `deleteUser` onu kullanır.
   Önce bunu yazmak, sonraki metotları kısaltır (kod tekrarını önler).
3. **Sonra CRUD metotları sırayla:** `createUser` → `login` →
   `getAllUsers` → `getUserById` → `updateUser` → `changePassword` →
   `deleteUser`. Genelde Controller'da hangi sırayla uç nokta
   tanımlanacaksa Service'te de aynı sırayla ilerlemek okunabilirliği
   artırır.
4. Her metot içinde mantık şu üçlü sırayla kurulur:
   a) Repository'den veri çek / DTO'dan veri oku,
   b) İş kuralını uygula (doğrulama, hash'leme, karşılaştırma) —
      kural ihlalinde `throw new XyzException(...)`,
   c) Repository'ye yaz (`save`/`delete`) ve sonucu (Entity) döndür.

**Önemli:** Service metotları Entity döner (DTO değil!). DTO'ya
çevirme işi bilerek Controller'a bırakılır; böylece Service, HTTP
katmanından tamamen habersiz, saf bir iş mantığı katmanı olarak kalır.

---

### 7) Controller (`controller/*.java`) — DETAYLI SIRA
En son yazılır çünkü Service'e (iş mantığı için) ve DTO/Entity'ye
(dönüşüm için) bağımlıdır. Controller'ı yazarken önerilen adımlar:

1. **Önce `@RestController` sınıfını aç, `UserService`'i constructor
   ile enjekte edin.** Henüz hiçbir `@GetMapping`/`@PostMapping` yazmadan
   önce bu bağımlılık hazır olmalı.
2. **Uç noktaları HTTP metoduna göre mantıksal sırayla ekleyin:**
   önce kaynağı YARATAN (`POST /users/add`), sonra kaynağı DOĞRULAYAN
   (`POST /users/login`), sonra OKUYAN (`GET /users/get`,
   `GET /users/{id}`), sonra GÜNCELLEYEN (`PUT`, `PATCH`), en son
   SİLEN (`DELETE`) uç nokta yazılır. Bu sıra, CRUD'un doğal akışını
   (Create → Read → Update → Delete) izler ve dosyayı okuyan birinin
   akışı takip etmesini kolaylaştırır.
3. **Her metodun içi 2 satırdan fazla OLMAMALI:**
   a) `@RequestBody`/`@PathVariable` ile gelen veriyi doğrudan
      Service metoduna geçir,
   b) Service'ten dönen Entity'yi `UserResponse.fromEntity(...)` ile
      DTO'ya çevirip döndür.
   Eğer bir Controller metodunda `if`, `for`, veya bir hata mesajı
   oluşturma mantığı görüyorsanız, bu kod yanlış katmandadır —
   Service'e taşınmalıdır.
4. **HTTP durum kodlarını (`@ResponseStatus`) doğru seçin:**
   oluşturma → 201 CREATED, silme → 204 NO_CONTENT, diğerleri
   varsayılan 200 OK (hata durumları zaten `GlobalExceptionHandler`
   tarafından yönetilir, Controller'da bunun için kod yazılmaz).

### 8) Test (`src/test/java/...`)
Test EN SON yazılır çünkü test edeceğimiz katmanların önce hazır olması gerekir.
Bu projedeki `SpringBootLearningApplicationTests`, Spring context'inin bütün
bean'lerle açıldığını; `UserLombokTests` ise Lombok'un builder, getter ve setter
metotlarını gerçekten ürettiğini doğrular. Daha sonra eklenecek bir
`UserFlowTests` ile kayıt → giriş → şifre değiştir → güncelle → sil → 404 akışı
`MockMvc` üzerinden uçtan uca sınanabilir.

> **Özet sıralama:** Entity → Repository → DTO (önce request, sonra
> response) → Exception → Config → Service (önce bağımlılıklar, sonra
> yardımcı metot, sonra CRUD metotları) → Controller (önce bağımlılık,
> sonra Create → Read → Update → Delete sırasıyla uç noktalar) → Test

---

## Uç Noktalar (Endpoints)

| Metot  | Yol                        | Açıklama                          | Başarı Kodu |
|--------|-----------------------------|------------------------------------|-------------|
| GET    | `/`                          | Sistem ayakta mı kontrolü          | 200         |
| POST   | `/users/add`                 | Yeni kullanıcı kaydı               | 201         |
| POST   | `/users/login`                | Giriş                              | 200         |
| GET    | `/users/get`                  | Tüm kullanıcıları listele          | 200         |
| GET    | `/users/{id}`                 | id ile kullanıcı getir             | 200 / 404   |
| PUT    | `/users/{id}`                 | İsim/soyisim güncelle              | 200 / 404   |
| PATCH  | `/users/change-password`      | Şifre değiştir                     | 200 / 404   |
| DELETE | `/users/{id}`                 | Kullanıcı sil                      | 204 / 404   |

Lombok kullanan ikinci akışta aynı uç noktaların başına `/lombok` gelir.
Örneğin kullanıcı ekleme adresi `POST /lombok/users/add` olur. Bu akış
`users_lombok` tablosunu kullanır; böylece iki örnek birbirinin verisini
etkilemez.

---

## Lombok'lu ve Lombok'suz Karşılaştırma

| Lombok'suz sınıf | Lombok'lu karşılığı | Öğrenilecek konu |
|---|---|---|
| `entity/User.java` | `entity/UserLombok.java` | `@Getter`, `@Setter`, `@NoArgsConstructor`, `@Builder` |
| `repository/UserRepository.java` | `repository/UserLombokRepository.java` | Entity tipinin repository'ye bağlanması |
| `service/UserService.java` | `service/UserLombokService.java` | `@RequiredArgsConstructor` ile constructor injection |
| `controller/UserController.java` | `controller/UserLombokController.java` | `@RequiredArgsConstructor` ile controller injection |
| `dto/*` record'ları | `dto/*Lombok.java` sınıfları | Getter, setter ve constructor üretimi |

Java sınıf adlarında tire (`-`) kullanılamadığı için `User-lombok` yerine
Java standardına uygun `UserLombok` adı kullanılmıştır.

### Neden Entity Üzerinde `@Data` Yok?

`@Data`; getter/setter yanında `toString`, `equals` ve `hashCode` da üretir.
JPA entity'lerinde bu metotlar tembel ilişkileri istemeden yükleyebilir,
döngü oluşturabilir veya kimliği henüz atanmamış nesnelerde beklenmedik
sonuçlar doğurabilir. Bu nedenle `UserLombok` üzerinde yalnızca gerçekten
gereken Lombok anotasyonları seçilmiştir.

### Record mu, Lombok mu?

Orijinal request/response DTO'ları Java `record` olarak zaten çok kısadır ve
immutable'dır. Gerçek bir projede sırf Lombok kullanmak için record'ları normal
sınıfa çevirmek çoğu zaman gerekli değildir. Buradaki `*Lombok` DTO'ları iki
yaklaşımı yan yana inceleyebilmek için özellikle eklenmiştir.

### Lombok Akışını İnceleme Sırası

Lombok'lu taraf da aynı bağımlılık sırasıyla okunmalıdır:

```
UserLombok
    → UserLombokRepository
    → *RequestLombok / *ResponseLombok
    → UserLombokService
    → UserLombokController
    → UserLombokTests
```

Önce `User.java` ile `UserLombok.java` dosyalarını yan yana aç. Ardından
`UserService.java` içindeki elle yazılmış constructor ile
`UserLombokService.java` üzerindeki `@RequiredArgsConstructor` anotasyonunu
karşılaştır. Lombok'un iş mantığını değiştirmediğini, sadece tekrarlanan Java
kodunu derleme sırasında ürettiğini göreceksin.

---

## Güvenlik Notu

Bu proje katmanlı mimari ve temel üyelik işlemlerini öğretmek için hazırlanmıştır.
Mevcut `/users/login` ve `/lombok/users/login` uç noktaları e-posta ile parolayı
doğrular ancak JWT, session veya cookie üretmez. Uç noktalar henüz Spring
Security ile korunmamaktadır. Gerçek bir uygulamada kullanıcı kimliği istek
gövdesindeki ID'den değil, doğrulanmış token veya session bilgisinden
alınmalıdır.

---

## `auth` Projesinden Farklar

- **Bellek (`List<User>`) yerine gerçek veritabanı** (H2, in-memory,
  JPA/Hibernate ile).
- **Şifreler artık düz metin değil, `BCryptPasswordEncoder` ile
  hash'lenerek** saklanıyor.
- **Entity asla API cevabı olarak dönmüyor**; her zaman `UserResponse`
  gibi bir DTO'ya çevriliyor, böylece hash bile olsa şifre dışarı
  sızmıyor.
- **Hatalar merkezi olarak (`GlobalExceptionHandler`) doğru HTTP durum
  koduna (404 / 401 / 409 / 400) çevriliyor**, tek tip `RuntimeException`
  yerine.

---

## Çalıştırma

```bash
./mvnw spring-boot:run
```

Uygulama `http://localhost:8082` üzerinde ayağa kalkar (auth projesi
8081'de olduğu için port çakışması yaşanmaz).

H2 konsolu: `http://localhost:8082/h2-console`
(JDBC URL: `jdbc:h2:mem:springbootlearningdb`, kullanıcı: `sa`, şifre: boş)

## Test

```bash
./mvnw test
```
