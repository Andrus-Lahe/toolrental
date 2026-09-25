# Minu profiili haldamine — implementatsiooni plaan

**Seotud task:** [Minu-profiili-haldamine.md](./Minu-profiili-haldamine.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java` — Spring Boot rakenduse algusklass; tegelik baaspakett ee.toolrental.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` — Olemas 400 väljade valideerimine ja kohandatud 403/404 erindite käsitlus, mitte kogu uue taski 500 leping.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java` — String message ja String errorCode.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java` — 403 ärivea erind.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/PrimaryKeyNotFoundException.java` — Puuduva ID 404.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` — Java 21, Spring Boot 4, JPA, validation, MapStruct ja testisõltuvused.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` — Profiili ja aadressi skeem.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/backend/GoogleLoginView/Google-kontoga-sisselogimine.md` — OAuth/principal ja /api/me plaanitud leping; ei ole teostus.

Controller/service/persistence profiilikomponendid ja profiilitestid puuduvad; sarnast valmis teenuseahelat ei leitud. Frontend on samas repos, kuigi backend/CLAUDE.md ütleb teisiti.

## Puuduv/muudetav

Vajalikud on profiili lugemise ja PUT salvestamise DTO-d, JPA seosed, repository/mapper/service/controller, OAuth taski sessiooni ühendus, atomaarne salvestamine, täpne veakäsitlus ja testid. Uut SQL skeemi, app_user registreerimist ega e-kirja saatmist ei lisata.

## Sammud

1. **Persistence entiteedid**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUser.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/Profile.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/location/Location.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/district/District.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/city/City.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/role/Role.java`.

Loo skeemile vastavad JPA entiteedid ainult juhul, kui OAuth või viiteandmete taskid pole neid vahepeal loonud. AppUser → Role, Profile → AppUser/Location, Location → District, District → City. Säilita FK ja unikaalsuse piirangud; ära lisa jagatud Location seosele remove cascade’i. Profiili seos kasutajaga on valikuline kasutaja poolelt, kohustuslik profiili poolelt.

2. **Repositooriumid**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUserRepository.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/ProfileRepository.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/location/LocationRepository.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/district/DistrictRepository.java`.

Lisa `findProfileByUserId(Integer userId)` ja `existsProfileByEmailAndUserIdNot(String email, Integer userId)` (või vastav nimega JPQL). Lae profiili aadressi vajalikud seosed kontrollitud päringuga. Samaaegse esmase PUT-i serialiseerimiseks lisa kasutajarea pessimistic write lukuga otsing; lukustus kestab sama salvestustehingu lõpuni. GET seda kirjutuslukku ei kasuta. Kasuta JPQL-i ja tagastatavat subjekti nimetavaid meetodeid, mitte geneerilist findFilteredBy nime.

3. **DTO ja MapStruct mapper**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/profile/dto/MyProfileRequestDto.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/profile/dto/MyProfileResponseDto.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/ProfileMapper.java`.

Loo taski väljade ja tüüpidena DTO-d. Request kasutab NotBlank/Size/Email/NotNull/Positive vastavalt BE tabelile. LastName lubab tühja/null sisendit, mille service normaliseerib. Mapper kaardistab profiili vastuse koos cityId-ga; kasutaja ID/roll/Google seos ei tule request mapperist. Profiilita vastuse koostab service kontrollitud kasutajaandmetest. DTO-d on praegu ainult profile ressursi jaoks; kui tegelik taaskasutus tekib teises ressursis, vii ühine DTO controller/common/dto paketti. Ära loo mapperi genereeritud klasse käsitsi.

4. **Teenused ja tehingupiir**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/ProfileService.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/AppUserService.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/DistrictService.java`.

Lisa `getMyProfile(Integer userId, String googleEmail)` ja `saveMyProfile(Integer userId, MyProfileRequestDto myProfileRequestDto)`. ID järgi findById/orElseThrow paiguta vastava service’i `public getValidAppUserBy(Integer userId)` / `getValidDistrictBy(Integer districtId)` meetodisse. GET kasutab readOnly tehingut ning puuduva profiili korral ei kirjuta midagi. PUT lukustab kasutajarea, valideerib district'i ja teise profiili e-posti, salvestab nimed/kontaktid/asukoha ühes Transactional tehingus ning tagastab värske DTO. Muutunud aadressile loo uus Location, muutumatule säilita vana; nii ei muudeta teise profiili jagatud aadressi. Pärast flush/commit unikaalsuskonflikti peab kogu tehing tagasi pöörduma; käsitle ainult tuvastatud email unikaalsuskonflikti EMAIL_ALREADY_EXISTS-na, mitte kõiki integrity vigu. Hoia muutujanimed täistüüpi kirjeldavad; tingimuslikule DTO täitmise abimeetodile sobib handle-prefiks.

5. **Controller ja autentimise ühendus**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/profile/ProfileController.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`.

Controller näeb ainult DTO-sid ja saab userId/Google email'i autentimise taski principal'ist. GET /api/me/profile ja PUT /api/me/profile; PUT request @Valid. Mõlemad vajavad autentimist, kuid mitte olemasolevat profile kirjet. Säilita CSRF-kaitse PUT jaoks. SecurityConfig ja principal on OAuth taski sõltuvused, mitte praegu olemasolevad failid. Ühenda olemasoleva /api/me service’i lugemine värskete DB nimede ja profiili e-postiga, säilitades selle DTO lepingu. Ära loo POST /api/users ega uut Google autentimisvoogu.

6. **Veahaldus**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`.

Säilita olemasolev ApiError stringiline errorCode. Koonda uued sõnumid/konstandid ErrorResponse enumi ilma kogu projekti veakoodide tüübi muutmiseta. Lisa puuduva valideerimise/teisenduse ning profiili laadimise/salvestamise tõrgete taskis kirjeldatud kuju; ära püüa neid controlleri üldise catch-blokiga. Kontrolli tegelikku DB constraint'i e-posti konflikti eristamiseks. Kasuta district'i jaoks PrimaryKeyNotFoundException, e-posti konflikti jaoks ForbiddenException.

7. **Testid** — failid: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/ProfileServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/profile/ProfileControllerTest.java` ning `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/persistence/profile/ProfilePersistenceTest.java`. Lisa allpool loetletud kontrollid; PostgreSQL integratsioon kontrollib tehinguid/constraint'e päriselt, mock-test ei tõenda neid. Käivita esmalt profiilitestid, seejärel vajalik Gradle test/build kontroll olemasoleva projekti seadistuse järgi. Selle plaani koostamisel teste ei käivitata, sest tootmiskoodi ei muudeta.

## Veakäsitlus

GET/PUT autentimata → 401 tühi body. Vigane request → 400 INCORRECT_INPUT, olematu district → 404 PRIMARY_KEY_NOT_FOUND. Teise profiili e-post → 403 EMAIL_ALREADY_EXISTS, oma muutmata e-post on lubatud. Profiili puudumine GET puhul → 200 vormi algolek. CSRF 403 ei ole EMAIL_ALREADY_EXISTS. Ootamatu viga → taski GET/PUT 500 sõnum ja tehingu rollback, ilma SQL-i või stack trace'i kliendile saatmata.

## Testid

- GET olemasoleva ja profiilita kasutajaga, Google email'i puudumine, õige cityId/districtId, lugemise kõrvaltoimete puudumine.
- Esimene PUT ja korduv/samaaegne PUT: üks profiil sama kasutaja kohta, ei teki uut app_user kirjet ega muutumatul aadressil uut location kirjet.
- Kliendi võõras ID/roll/googleSub ei mõjuta isikut ega õigusi; test peab tõendama ka teise kasutaja andmete säilimist.
- Oma e-post lubatud; teise e-post keelatud, ka konkureeriva unikaalsuskonflikti korral.
- Kõigi väljade pikkuspiirid, tühi perekonnanimi, valikuline korter, vigane email/puuduv districtId/olematu district.
- Jagatud Location puhul ainult sihtprofiili aadress muutub, vana profiili aadress jääb alles; täielik rollback tõrke korral.
- HTTP 200/400/401/403/404/500, CSRF ja GET /api/me värske hasProfile/nime/e-posti nähtavus.

## Avatud küsimused

- Kasutaja lahendas põhivastuolu: tegemist on Google-järgse profiili täitmise/muutmisega, mitte PDF-i uue kasutaja registreerimisega. Endpoint'id ja route on seotud taski tehnilised ettepanekud, mida olemasolevas koodis pole.
- backend/CLAUDE.md mainib numbrilisi ErrorResponse koode, tegelik ApiError kasutab stringe; plaan säilitab olemasoleva stringlepingu ja ei tee kõrvalist migratsiooni.
- Struktuuridokumendi ee.minuprojekt on näide; kasutada tegelikku ee.toolrental paketti. Seadistuse/skeemi näidisnimesid ei kopeerita pimesi.
- OAuth ja CSRF teostus puudub ning tuleb ühendada autentimise taskiga; täpsed CSRF tokeni edastamise detailid on seal veel lahtised.
- Uus /my-profile route tuleb siduda GoogleLoginView seni määramata profiilivormi suunamisega. Varasemaid dokumente ei muudeta selles ülesandes automaatselt.
