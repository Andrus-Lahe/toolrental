# Sisselogitud kasutaja päring — implementatsiooni plaan

**Seotud task:** [Sisselogitud-kasutaja-paring.md](./Sisselogitud-kasutaja-paring.md)

## Hetkeseis (mis on juba olemas)

- `backend/build.gradle`: Java 21 / Spring Boot 4.0.6, JPA, valideerimine, MapStruct ja testisõltuvused; OAuth2 client sõltuvus puudub.
- `backend/src/main/java/ee/toolrental/ToolRentalApplication.java`: rakenduse põhiklass.
- `backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`: olemas 400/403/404 käsitlejad; taski 500 leping puudub.
- `backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java`: veavastuse `message` ja string-tüüpi `errorCode`.
- `docs/database/2_create.sql` ja `3_import.sql`: `app_user`, `role`, `profile` tabelid ja Liisi/Marko näidisandmed olemas.
- `AppUserPrincipal`, `SecurityConfig`, domeeni entiteedid/repositooriumid, mapper, CurrentUserDto, AppUserService ja kontroller puuduvad; `backend/src/test` puudub.
- [Google sisselogimise task](../GoogleLoginView/Google-kontoga-sisselogimine.md) ja selle [plaan](../GoogleLoginView/Google-kontoga-sisselogimine-IMPLEMENTATSIOON.md) kirjeldavad juba sama `/api/me` teenust. Headeri plaan täpsustab selle ühist teostust, mitte teist endpoint'i.

## Puuduv/muudetav

Vaja on sessioonist tuvastatud kasutaja lugemist, valikulise profiili arvestamist, kuue väljaga DTO-d ja 401/500 lepingut. OAuth sisselogimine, principal'i loomine ning logout on GoogleLoginView taski sõltuvused. Rakenda sama teenust kõigile FE-vaadetele; eraldi HeaderController'it ega Header-kasutajatabelit ei looda.

## Sammud

1. **Entiteedid** — `backend/src/main/java/ee/toolrental/persistence/appuser/AppUser.java`, `backend/src/main/java/ee/toolrental/persistence/role/Role.java`, `backend/src/main/java/ee/toolrental/persistence/profile/Profile.java`.
   - Kooskõlasta loomine OAuth/MyProfile taskidega; olemasolul taaskasuta. Kaardista `app_user.role_id → role.id` ja `profile.user_id → app_user.id` täpselt SQL-i järgi. Kasutajal võib profiil puududa.
   - Päring ei vaja asukoha, telefonide ega teiste domeenide laadimist. Skeemi ei muudeta; ära loo profiili GET kõrvalmõjuna.
2. **Repositooriumid** — `backend/src/main/java/ee/toolrental/persistence/appuser/AppUserRepository.java`, `backend/src/main/java/ee/toolrental/persistence/profile/ProfileRepository.java`.
   - Kasutaja otsitakse principal'i ID järgi; profiilile kirjeldava nimega `Optional<Profile> findProfileByUser(AppUser appUser)`. Lae roll transaktsiooni sees, et vältida lazy-laadimist controlleris. `getValidAppUserBy(Integer userId)` koondab ID-otsingu service'is; puuduva principal-kasutaja leping vajab allpool kirjeldatud täpsustust.
3. **DTO ja mapper** — `backend/src/main/java/ee/toolrental/controller/auth/dto/CurrentUserDto.java`, `backend/src/main/java/ee/toolrental/persistence/appuser/AppUserMapper.java`.
   - DTO väljad: `Integer userId`, `String firstName`, `String lastName`, `String roleName`, `String email`, `boolean hasProfile`. Kaardista `id → userId`, rolli nimi ja nimed MapStructiga. Profiili/fallback'i valik kuulub service'isse; mapper saab valitud e-posti ja hasProfile väärtuse parameetritena.
   - `google_sub`, status, telefon ja aadress ei lähe DTO-sse. Tühi perekonnanimi säilib tühistringina.
   - Praegu pole DTO kasutuskohti. GoogleLogin plaan paigutab DTO auth paketti: sama endpoint'i kasutamine mitmes FE-vaates ei tee sellest jagatud backend DTO-d. Kui teostamise ajal kasutab seda mitu backend ressursipaketti, paiguta see `controller/common/dto/` alla ja uuenda kõik import'id.
4. **Service** — `backend/src/main/java/ee/toolrental/service/AppUserService.java`.
   - `CurrentUserDto getCurrentUser(Integer userId, String googleEmail)`, lugemine read-only transaktsioonis. Leia app_user, roll ja valikuline profiil.
   - Profiili korral `email = profile.email`, `hasProfile = true`; profiilita `email = googleEmail`, `hasProfile = false`. Profiili e-post võidab ka siis, kui Google e-post erineb. Ärireeglid ei kuulu controllerisse.
5. **Controller ja turvaseos** — `backend/src/main/java/ee/toolrental/controller/auth/AuthController.java`, `backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`, `backend/src/main/java/ee/toolrental/infrastructure/security/AppUserPrincipal.java`.
   - Üks `GET /api/me`, parameetriks `@AuthenticationPrincipal AppUserPrincipal principal`; kutsu service'it ainult `principal.getUserId()` ja `principal.getEmail()` väärtustega. URL/body/query kaudu kasutajat valida ei saa.
   - OAuth task loob principal'i ja seadistab sessiooni. API päringule ilma autentimiseta tagasta 401 tühja body'ga `HttpStatusEntryPoint` kaudu, mitte OAuth 302. Controller ei käsitle puuduvat sessiooni ärilise 404-na.
   - Ära dubleeri sisselogimise ega logout controllerit. GET ei vaja CSRF tokenit; OAuth taski muutvate päringute CSRF seadistust ei nõrgendata selle plaaniga.
6. **Tehnilised vead** — `backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`.
   - Lisa andmelaadimise tõrkele taski 500 `ApiError`: `INTERNAL_SERVER_ERROR` / „Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti.“ Endpoint'i-spetsiifilise teate eristamiseks kasuta vajadusel `backend/src/main/java/ee/toolrental/infrastructure/exception/CurrentUserLoadException.java` koos põhjusega ning selle handlerit.
   - Ära neela viga 200/profiilita vastuseks ega muuda olemasolevaid teiste endpoint'ide veateateid. Logi põhjus serveris; response ei sisalda SQL-i, stack trace'i ega sessiooni saladusi.
7. **Testid** — `backend/src/test/java/ee/toolrental/service/AppUserServiceTest.java`, `backend/src/test/java/ee/toolrental/controller/auth/AuthControllerTest.java`; `backend/build.gradle`.
   - Lisa OAuth/Security testide sõltuvus vastavalt GoogleLogin taskile. MockMvc autentimine peab sisaldama päris projekti `AppUserPrincipal`-i `OAuth2AuthenticationToken` sees; tavaline `oidcLogin()` loob teist tüüpi principal'i.
   - Käivita pärast teostust `./gradlew test`. Testid ei vaja päris Google kontot ega demokasutajate võlts-google_sub väärtustega sisselogimist.

## Veakäsitlus

| Olukord | Tulemus |
|---|---|
| Sessioon puudub/aegunud | Security 401; tühi body, puudub Location ümbersuunamine. |
| Profiil puudub | 200, Google e-post, `hasProfile: false`. |
| Andmebaasipäring ebaõnnestub | 500, täpne taski ApiError. |

Siin ei lisata sisendivalideerimise 400 ega profiili puudumise 404 viga. Blokeeritud konto sisselogimise keeld kuulub OAuth taskile; olemasoleva sessiooni hilisem blokeerimine/kustutamine pole selle GET taskiga määratud.

## Testid

- Liis (3): customer, profiili e-post, `hasProfile: true`; Marko (1): admin ja vastavad nimed.
- Profiilita fixture: 200, principal'i Google e-post ja false; tühi perekonnanimi säilib.
- Google ja profiili erinev e-post: profiili väärtus võidab.
- JSON sisaldab täpselt kuut taski välja; hasProfile on boolean. `google_sub`, status, telefon/aadress puuduvad.
- Sessioonita/aegunud sessiooniga 401, tühi body, puudub redirect; teise kasutaja query/body ID ei muuda principal'i põhist valikut.
- DB tõrge annab täpse 500 keha. Lugemine ei tekita/muuda profiili ega kasutajat.

## Avatud küsimused

- Task ei määra käitumist siis, kui sessioonis viidatud app_user on pärast sisselogimist kustutatud või blokeeritud. Enne selle servajuhu teostust täpsustada sessiooni tühistamise/401 või muu vastuse poliitika; ära lisa vaikimisi avalikku 404 lepingut.
- `backend/CLAUDE.md` nimetab ErrorResponse enumit ja vananenud skeemi; tegelik kood kasutab ApiError string-koode ja `ee.toolrental` paketti. Uut enumit ei eeldata olemasolevaks; selle vajadus kooskõlastatakse ühise veakäsitlusega.
- GoogleLogin plaan ja see plaan kirjeldavad sama endpoint'i. Teostamise eel ühenda töö, et ei tekiks kahte `/api/me` mapping'ut ega eri DTO-sid.
