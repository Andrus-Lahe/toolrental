# Kasutaja kustutamine — implementatsiooni plaan

**Seotud task:** [Kasutaja-kustutamine.md](./Kasutaja-kustutamine.md)

**Teenus:** `DELETE /api/admin/users/{userId}`

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/PrimaryKeyNotFoundException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/DataNotFoundException.java`

Rakenduse pakett on ee.toolrental. Handler katab @Valid väljavead ning kohandatud 403/404; ApiError errorCode ja message on String. ForbiddenException konstruktor võtab (message, errorCode). Puuduvad controller/service/persistence klassid ja backend testid; valmis analoogset endpoint’i ei leitud. `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` sisaldab JPA, validation, MapStructi ja testisõltuvusi. `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` ning `3_import.sql` määravad skeemi ja näidisandmed. Teiste taskide olemasolu ei tähenda nende koodi olemasolu.

## Puuduv/muudetav

Vajalikud on allpool loetletud entiteedid/repositooriumid, AdminUserService, AdminUserController, täpne vealeping ja testid. Ühised klassid luuakse üks kord; enne teostamist kontrolli uuesti paralleelsete taskide tehtud muudatusi. SQL skeemi automaatset muutmist see plaan ei nõua.

## Sammud

1. **Persistence entiteedid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUser.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/Profile.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/Tool.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/Booking.java`

Kaardista lähteülesande tabelid ja FK-d JPA-s; ära kasuta cascade REMOVE viiteandmete või jagatud seoste suhtes. FK mudelid, mida teised taskid juba loovad, taaskasuta.

2. **Repositooriumid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUserRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/ProfileRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/ToolRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/BookingRepository.java`

ToolRepository.existsToolsByOwnerId ja BookingRepository.existsBookingsByRenterId; ProfileRepository.findProfileByUserId. Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

Uut request/response DTO-d pole vaja: body puudub ja edu on tühi 200.

Kontrolleri parameetrid jäävad path/sessiooni tasemele.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/AdminUserService.java`

AdminUserService.deleteUser(actorId,userId): tehing, 404 → SELF_DELETE_NOT_ALLOWED → USER_HAS_DATA. Kustuta profile enne app_user, location säilib. Kontrolli FK võistlusolukordi: ära muuda kõiki integrity vigu USER_HAS_DATA-ks. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/admin/AdminUserController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `DELETE /api/admin/users/{userId}` ning lähteülesande 200 keha. Admin endpoint nõuab admin rolli; klient ei saa sessiooni actorId-d asendada. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/KasutajakustutamineServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/admin/KasutajakustutamineControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Sisse logitud kasutaja roll pole `admin`. | 403 Forbidden | tühi (Spring Security) |
| Kasutajat `userId = 123` pole. Teates kasutada tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'userId' väärtusega: 123"}` |
| Admin üritab kustutada iseennast. | 403 Forbidden | `{"errorCode":"SELF_DELETE_NOT_ALLOWED","message":"Iseennast ei saa kustutada"}` |
| Kasutajal on vähemalt üks tööriist või broneering. | 403 Forbidden | `{"errorCode":"USER_HAS_DATA","message":"Kasutajat ei saa kustutada, sest tal on tööriistu või broneeringuid"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kasutaja kustutamine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist.
- `SELF_DELETE_NOT_ALLOWED` ja `USER_HAS_DATA` on uued koodid. Neid visatakse olemasoleva `ForbiddenException` klassiga.
- Kontrollide järjekord: 404, `SELF_DELETE_NOT_ALLOWED`, siis `USER_HAS_DATA`. Vea korral ei kustutata midagi.

## Testid

Tööriist/broneering eraldi takistused, iseenda keeld, profiiliga/ilma edukas testkasutaja, rollback, võõrad read säilivad.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] `DELETE /api/admin/users/{userId}` on olemas ja kättesaadav ainult `admin` rollile.
- [ ] Tööriistade ja broneeringuteta kasutaja kustutamine annab 200 tühja body'ga ning kasutaja `app_user` ja `profile` read on kustutatud.
- [ ] Profiilita kasutaja kustutamine õnnestub samuti.
- [ ] Impordikasutaja `userId = 3` annab 403 `USER_HAS_DATA` ja andmed jäävad alles.
- [ ] Admin ei saa iseennast kustutada: vastus 403 `SELF_DELETE_NOT_ALLOWED`.
- [ ] Olematu `userId` annab 404 ja `PRIMARY_KEY_NOT_FOUND` teate päringu ID-ga.
- [ ] Profiili ja kasutaja kustutamine toimub ühes transaktsioonis; vea korral ei jää osaliselt kustutatud andmeid.
- [ ] Sisse logimata kasutaja saab 401 ja mitte-admin 403.
- [ ] Automaattestid katavad eduka kustutamise (profiiliga ja ilma), `USER_HAS_DATA` (tööriista ja broneeringu korral eraldi), `SELF_DELETE_NOT_ALLOWED`, 404, 500 ja rollipõhise ligipääsu.

## Avatud küsimused

Lähteülesande põhileping on plaani jaoks piisav; uut ärikäitumist ei lisata.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
