# Google kontoga sisselogimine — implementatsiooni plaan

**Seotud task:** [Google-kontoga-sisselogimine.md](./Google-kontoga-sisselogimine.md)

**Teenus:** `GET /api/me`; seotud OAuth sisselogimisvoog ja `POST /logout`.

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/PrimaryKeyNotFoundException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/DataNotFoundException.java`

Rakenduse pakett on ee.toolrental. Handler katab @Valid väljavead ning kohandatud 403/404; ApiError errorCode ja message on String. ForbiddenException konstruktor võtab (message, errorCode). Puuduvad controller/service/persistence klassid ja backend testid; valmis analoogset endpoint’i ei leitud. `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` sisaldab JPA, validation, MapStructi ja testisõltuvusi. `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` ning `3_import.sql` määravad skeemi ja näidisandmed. Teiste taskide olemasolu ei tähenda nende koodi olemasolu.

## Puuduv/muudetav

Vajalikud on allpool loetletud entiteedid/repositooriumid, DTO/mapper, CurrentUserService, AuthController, täpne vealeping ja testid. Ühised klassid luuakse üks kord; enne teostamist kontrolli uuesti paralleelsete taskide tehtud muudatusi. SQL skeemi automaatset muutmist see plaan ei nõua.

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

AppUserRepository.findUserByGoogleSub, RoleRepository rolli otsing, ProfileRepository.findProfileByUserId. google_sub unikaalsus peab kaitsma samaaegset esmaloginit. Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/auth/dto/CurrentUserDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUserMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/CurrentUserService.java`

AppUserOidcService delegeerib standardsele OidcUserService-le, seob kontrollitud sub järgi konto, loob customer/A kasutaja ilma profiilita, säilitab olemasoleva rolli, blokeeritud kasutaja ei saa sessiooni. AppUserPrincipal hoiab sisemist userId-d. CurrentUserService loeb DB nimed/rolli ning profiili email või principal’i fallback; getCurrentUser ei loo profiili. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/auth/AuthController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `GET /api/me`; seotud OAuth sisselogimisvoog ja `POST /logout`. ning lähteülesande 200 keha. Actor/userId tuleb sessiooni principal’ist, mitte request body’st; säilita taski osapoole kontrollid. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

OAuth erisammud: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/AppUserOidcService.java`, `AppUserPrincipal.java` ja `SecurityConfig.java`; `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` OAuth client sõltuvus; `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/resources/application.properties` keskkonnamuutujad GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET. Registreeri /oauth2/authorization/google ja /login/oauth2/code/google Springi voo kaudu, success /, failure /?loginError ning POST /logout sessiooni lõpetamisega. `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/vite.config.js` vajab /oauth2, /login/oauth2 ja /logout proxy't algset Host päist säilitades. Secret ei lähe frontendisse ega faili väärtusena. CurrentUserDto kuus välja peavad vastama taskile; OAuth protokolliteid ei dubleerita custom POST endpoint'iga.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/GooglekontogasisselogimineServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/auth/GooglekontogasisselogimineControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

| Olukord | Status code | Response body |
|---|---|---|
| `GET /api/me`: sessioon puudub või on aegunud. | 401 Unauthorized | Tühi body; Google'i lehele ei suunata. |
| `GET /api/me`: ootamatu andmebaasitõrge. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |
| OAuth autentimine ebaõnnestub või kasutaja on blokeeritud. | 302 Found | Body puudub; `Location: /?loginError` frontendi originil. |
| `POST /logout`: CSRF token puudub või on vigane. | 403 Forbidden | Body pole selle taskiga määratud; klient käsitleb staatust. |

401 ja loginError käitumine tulevad juhendist. 500 JSON on taski tehniline täpsustus olemasoleva `ApiError` stringväljadega; praegune `RestExceptionHandler` seda veel ei taga. Kliendile ei tagastata SQL-i, stack trace'i ega Google tokenit. CSRF tokeni aegumisel peab uus katse kasutama värsket tokenit.

## Testid

Uus/olemasolev/korduv/samaaegne login, blokeeritud konto, puuduva family_name tühistring, vigane token, profile true/false, sessioon ja logout/CSRF.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] Google nupp saab alustada OAuth voogu `/oauth2/authorization/google` kaudu; callback kontrollitakse Spring Security poolt.
- [ ] Uus konto seotakse `sub` järgi, luuakse aktiivse customer'ina ilma profiilita; korduv/samaaegne login ei dubleeri kontot.
- [ ] Olemasolev roll säilib ja blokeeritud kontole sessiooni ei looda.
- [ ] `/api/me` kasutab sessiooni principal'i ning tagastab täpselt kuus kirjeldatud välja, õige rolli ja e-posti allika.
- [ ] Profiilita kasutaja saab 200 ning `hasProfile: false`; autentimata päring saab 401 ilma Google redirect'ita.
- [ ] OAuth õnnestumine, ebaõnnestumine ja logout järgivad kirjeldatud suunamisi.
- [ ] Logout lõpetab sessiooni; pärast seda annab `/api/me` 401. CSRF leping on frontendiga kooskõlas ja kehtetu tokeniga POST lükatakse tagasi.
- [ ] Automaattestid katavad uue ja olemasoleva kasutaja, blokeeritud konto, vigase autentimistulemuse, korduva loomise, profiiliga/profiilita vastuse, 401, 500 ning logout/CSRF käitumise. Testid ei vaja päris Google kontot.
- [ ] Eraldi integratsioonikontroll päris seadistatud testkontoga kinnitab OAuth redirect'i, callback'i ja sessiooniküpsise töö; seda ei asenda mock-testide läbimine.

## Avatud küsimused

CSRF tokeni väljastus/edastuse täpne leping jääb taskis avatuks. HomeView vana POST /auth/google ei ole selle OAuth voo endpoint. Päris Google redirect test vajab eraldi testkontot; mock-test seda ei tõenda.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
