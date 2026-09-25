# Kasutaja blokeerimine — implementatsiooni plaan

**Seotud task:** [Kasutaja-blokeerimine.md](./Kasutaja-blokeerimine.md)

**Teenus:** `PATCH /api/admin/users/{userId}/status`

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

Kaardista lähteülesande tabelid ja FK-d JPA-s; ära kasuta cascade REMOVE viiteandmete või jagatud seoste suhtes. FK mudelid, mida teised taskid juba loovad, taaskasuta.

2. **Repositooriumid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUserRepository.java`

AppUserRepository tavapärane ID lugemine; muuda ainult status. Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/admin/dto/UserStatusRequestDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUserMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/AdminUserService.java`

AdminUserService.changeUserStatus(actorId,userId,request): @NotNull/@Pattern [AB], getValidAppUserBy, seejärel SELF_BLOCK_NOT_ALLOWED ainult iseenda B puhul. Sama status on idempotentne. A taastamine on API-s lubatud. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/admin/AdminUserController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `PATCH /api/admin/users/{userId}/status` ning lähteülesande 200 keha. Admin endpoint nõuab admin rolli; klient ei saa sessiooni actorId-d asendada. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/KasutajablokeerimineServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/admin/KasutajablokeerimineControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

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
| Admin üritab blokeerida iseennast (`userId` = sisse logitud kasutaja ID) ja `status = "B"`. | 403 Forbidden | `{"errorCode":"SELF_BLOCK_NOT_ALLOWED","message":"Iseennast ei saa blokeerida"}` |
| `status` puudub või pole `A`/`B`. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"status: <valideerimise teade>"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kasutaja oleku muutmine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist.
- `SELF_BLOCK_NOT_ALLOWED` on uus kood. Seda visatakse olemasoleva `ForbiddenException` klassiga.
- 400 tuleb `@Valid` + `@NotNull`/`@Pattern(regexp = "[AB]")` kaudu olemasolevast `handleMethodArgumentNotValid` handlerist.
- Kontrollide järjekord: 400 (valideerimine), 404, siis 403.

## Testid

Iseenda B keeld, muu B/A, sama olek, puuduv kasutaja, muutumatud ülejäänud väljad.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] `PATCH /api/admin/users/{userId}/status` on olemas ja kättesaadav ainult `admin` rollile.
- [ ] `{"status":"B"}` kasutajale `userId = 3` annab 200 tühja body'ga ja `app_user.status` on pärast seda `B`.
- [ ] `{"status":"A"}` taastab oleku `A`; sama oleku uuesti saatmine annab samuti 200.
- [ ] Muud `app_user` veerud ega teised tabelid ei muutu.
- [ ] Olematu `userId` annab 404 ja `PRIMARY_KEY_NOT_FOUND` teate päringu ID-ga.
- [ ] Admin ei saa iseennast blokeerida: vastus 403 `SELF_BLOCK_NOT_ALLOWED` ja olek ei muutu.
- [ ] Puuduv või vigane `status` annab 400 `INCORRECT_INPUT`.
- [ ] Sisse logimata kasutaja saab 401 ja mitte-admin 403.
- [ ] Automaattestid katavad eduka blokeerimise ja aktiveerimise, idempotentsuse, 400/403/404/500 juhtumid ning rollipõhise ligipääsu.

## Avatud küsimused

Task nimetab CustomOAuth2UserService, Google task AppUserOidcService: kasuta ühte OAuth teostust. Blokeerimine keelab järgmise sisselogimise; olemasoleva sessiooni lõpetamise uut reeglit task ei määra.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
