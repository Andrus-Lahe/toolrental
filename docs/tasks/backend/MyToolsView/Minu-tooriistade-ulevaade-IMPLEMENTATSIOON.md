# Minu tööriistade ülevaade — implementatsiooni plaan

**Seotud task:** [Minu-tooriistade-ulevaade.md](./Minu-tooriistade-ulevaade.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java` — Spring Boot, tegelik pakett ee.toolrental.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` — 400 ja kohandatud 403/404 vead.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java` — String errorCode ja message.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java` — 403 erind.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/DataNotFoundException.java` — 404 erind.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/util/StringBytesConverter.java` — UTF-8 teisendus, mitte Base64 kodeerimine.

`/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` sisaldab JPA, MapStructi ja valideerimise sõltuvusi; `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` vajalikku skeemi. MyTools controller, service, DTO-d, persistence ahel ja testid puuduvad. OAuth/MyProfile/Tools taskid on sõltuvuste kirjeldused, mitte teostus.

## Puuduv/muudetav

Viie loendiga DTO, sessioonipõhine lugemisteenus, repository päringud, mapperid, endpoint, selle vealeping ning fikseeritava kellaga testid. Andmebaasi skeemi ega booking olekut ei muudeta.

## Sammud

1. **Entiteedid**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUser.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/Profile.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/Tool.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/toolimage/ToolImage.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/Booking.java`.

Kasuta skeemile vastavaid seoseid ja varasemate taskide loodud entiteete, kui need on teostatud. Kontrolli enne loomist ka Role/Category/Location FK entiteetide olemasolu; ära loo paralleelseid mudeleid. Puuduv peapilt ei tohi välistada tööriista.

2. **Repository päringud**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/BookingRepository.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/ToolRepository.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/ProfileRepository.java`.

Lisa subjekti nimetavad meetodid findCurrentRenterBookingsBy, findPendingOwnerBookingsBy, findPendingRenterBookingsBy, findCurrentOwnerBookingsBy ning findAvailableOwnerToolsBy. Kasuta JPQL-i ja userId/today parameetreid, mitte kliendi saadetud kasutaja ID-d. Vaba tööriista päring kasutab NOT EXISTS käimasoleva C broneeringu suhtes. Pildiseosed ei tohi korrutada tulemusi; lae ainult põhipildid ning väldi N+1 päringuid.

3. **DTO ja mapperid**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/mytools/dto/MyToolsResponseDto.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/common/dto/MyToolCardDto.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/common/dto/MyToolBookingDto.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/ToolMapper.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/BookingMapper.java`.

Ümbris kuulub mytools ressursile. Kaarditüübid on jagatud ToolMapper/BookingMapper vahel ja kuuluvad common/dto alla vastavalt projekti jagatud DTO reeglile. Kaardista taski väljad MapStructiga. Pildid Base64.getEncoder abil, mitte UTF-8 StringBytesConverter abil. Puuduva pildi korral null, tühjad loendid alati [].

4. **Service ja kell**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/MyToolsService.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/AppUserService.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/config/TimeConfig.java`.

Lisa getMyTools(Integer userId). ID findById/orElseThrow koonda AppUserService public getValidAppUserBy(Integer userId) meetodisse. Kontrolli blokeeritud olekut ja profiili, loe Clock abil Europe/Tallinn tänane kuupäev üks kord ning kasuta seda kõigis viies päringus. Read-only tehing, võimalusel ühtse snapshoti jaoks sobiv isolatsioon; sortimine vastavalt taskile. Muutujanimed peegeldavad täistüüpi, tingimuslik DTO täitmine handle-prefiksiga.

5. **Controller ja turve**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/mytools/MyToolsController.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`.

GET /api/users/me/tools saab userId principal’ist, ei aktsepteeri omaniku/rentija ID-d. Customer/Admin näevad ainult enda loendeid. SecurityConfig/principal on OAuth taski sõltuvused. Selle endpoint’i 401 AUTHENTICATION_REQUIRED JSON seadista sobiva entry point’iga, jättes /api/me tühja 401 lepingu muutmata.

6. **Veahaldus**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`.

USER_BLOCKED → ForbiddenException, PROFILE_NOT_FOUND → DataNotFoundException. Koonda uued sõnumid/koodid ErrorResponse enumisse, säilitades tegeliku String errorCode tüübi. Lisa taski 500 kuju tehnilise info avaldamiseta.

7. **Testid** — failid `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/MyToolsServiceTest.java`, `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/mytools/MyToolsControllerTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/persistence/MyToolsQueryTest.java`. Käivita esmalt sihttestid, seejärel vajalik Gradle test/build kontroll; dokumentide koostamisel rakendust ei ehitata.

## Veakäsitlus

Taski 401/403/404/500 leping tuleb tagada ka siis, kui controllerini ei jõuta. Puuduv profiil on 404 ja FE suunab profiili täitma; tühi tööriistaloend on 200. Varem autenditud blokeeritud kasutaja ei tohi saada loendeid. Keeldu ei asendata tühja eduka vastusega.

## Testid

- Fikseeritud Clock 25.09.2026 ja taski Liisi näide, lisaks kõigi viie rühma eraldi testandmed.
- Omaniku/rentija suund, Customer/Admin endaandmete piirang ning teise kasutaja andmete lekkimise puudumine.
- P/C/R, startDate=täna, endDate=täna, tulevane C, lõppenud taotlus ja U tööriist.
- P ei võta tööriista vabade hulgast ära; kehtiv C võtab. Sama tööriista mitu taotlust jäävad eri bookingId-ga.
- Peapildi puudumine, lisapildi mittevalimine, Base64 baitide täpsus, duplikaadivabad loendid ja sortimine.
- 401/403/404/500 kuju, profiili telefon ja endpoint’i lugemisoperatsiooni kõrvaltoimete puudumine.
- Repository integratsioonitestid peavad kasutama tegelikke seoseid/JPQL-i; ainult mock-test ei kinnita filtreerimist ega N+1 puudumist.

## Avatud küsimused

- Viis rühma on kasutaja suunise järgi võetud kujundusest. Kuupäevareeglid ning tulevaste C/U kirjete väljajäämine on nähtavad tehnilised täpsustused, mitte väide, et kasutaja need eraldi kinnitas.
- Broneeringu detaili ja BookingApprovalView täpsed route'id on määramata. FE peab eristama bookingId-d ja toolId-d.
- AddToolView on Customer-only, MyToolsView ka Adminile; admini tööriista lisamise õiguse laiendamine ei kuulu automaatselt sellesse taski.
- MIME/pildiformaatide leping on AddToolView-ga ühine lahtine detail.
- backend/CLAUDE.md kirjeldab numbrilisi koode ja näidispaketti; tegelik kood kasutab String errorCode ning ee.toolrental. Plaan ei muuda olemasolevat veavormingut numbriliseks.
- Vana MyToolsView märkme kolme loendi kirjeldust ei kasutata uue FE lepinguna; fail ise jääb puutumata.
