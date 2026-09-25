# Kasutajate nimekirja päring — implementatsiooni plaan

**Seotud task:** [Kasutajate-nimekirja-paring.md](./Kasutajate-nimekirja-paring.md)

**Teenus:** `GET /api/admin/users`

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/PrimaryKeyNotFoundException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/DataNotFoundException.java`

Rakenduse pakett on ee.toolrental. Handler katab @Valid väljavead ning kohandatud 403/404; ApiError errorCode ja message on String. ForbiddenException konstruktor võtab (message, errorCode). Puuduvad controller/service/persistence klassid ja backend testid; valmis analoogset endpoint’i ei leitud. `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` sisaldab JPA, validation, MapStructi ja testisõltuvusi. `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` ning `3_import.sql` määravad skeemi ja näidisandmed. Teiste taskide olemasolu ei tähenda nende koodi olemasolu.

## Puuduv/muudetav

Vajalikud on allpool loetletud entiteedid/repositooriumid, DTO/mapper, AdminUserService, AdminUserController, täpne vealeping ja testid. Ühised klassid luuakse üks kord; enne teostamist kontrolli uuesti paralleelsete taskide tehtud muudatusi. SQL skeemi automaatset muutmist see plaan ei nõua.

## Sammud

1. **Persistence entiteedid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUser.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/role/Role.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/Profile.java`

Kaardista lähteülesande tabelid ja FK-d JPA-s; ära kasuta cascade REMOVE viiteandmete või jagatud seoste suhtes. FK mudelid, mida teised taskid juba loovad, taaskasuta.

2. **Repositooriumid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUserRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/role/RoleRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/ProfileRepository.java`

AppUserRepository.findAllUsersWithProfilesBy(): LEFT JOIN profile, roll, id ASC. Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/admin/dto/AdminUserDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUserMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/AdminUserService.java`

AdminUserService.getUsers(): registeredAt=profile.created_at kuupäevaosa; profiilita email/registeredAt null. Blokeeritud kirjed säilivad, google_sub vastusest välja. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/admin/AdminUserController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `GET /api/admin/users` ning lähteülesande 200 keha. Admin endpoint nõuab admin rolli; klient ei saa sessiooni actorId-d asendada. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/KasutajatenimekirjaparingServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/admin/KasutajatenimekirjaparingControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`). 401 ja 403 annab Spring Security enne controllerit, seega nende body on tühi.

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi |
| Sisse logitud kasutaja roll pole `admin`. | 403 Forbidden | tühi |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kasutajate laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

Ühtne 500 kuju tuleb teostamisel tagada, sest praegune `RestExceptionHandler` seda ei käsitle. SQL-i ega stack trace'i ei tagastata.

## Testid

Profiilita/blokeeritud kasutaja, roll, yyyy-MM-dd, ainult seitse vastusevälja.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] `GET /api/admin/users` on olemas ja kättesaadav ainult `admin` rollile.
- [ ] HTTP 200 vastus on massiiv täpselt väljadega `userId`, `firstName`, `lastName`, `email`, `roleName`, `registeredAt`, `status`.
- [ ] Impordiandmetega tagastatakse 2 kasutajat (id 1 ja 3) järjestuses `app_user.id ASC` ja näites toodud väärtustega.
- [ ] Blokeeritud (`status = 'B'`) kasutaja on samuti loendis.
- [ ] Profiilita kasutaja on loendis ning tema `email` ja `registeredAt` on `null`.
- [ ] `registeredAt` on kuupäev kujul `yyyy-MM-dd`; `google_sub` pole vastuses.
- [ ] Sisse logimata kasutaja saab 401 ja mitte-admin 403.
- [ ] Andmebaasi tõrge annab kirjeldatud 500 vastuse.
- [ ] Automaattestid katavad DTO kuju, järjestuse, profiilita ja blokeeritud kasutaja, rollipõhise ligipääsu ning 500 juhtumi.

## Avatud küsimused

Lähteülesande põhileping on plaani jaoks piisav; uut ärikäitumist ei lisata.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
