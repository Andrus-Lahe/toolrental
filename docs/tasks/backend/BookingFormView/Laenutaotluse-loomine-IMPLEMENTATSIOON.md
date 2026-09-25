# Laenutaotluse loomine — implementatsiooni plaan

**Seotud task:** [Laenutaotluse-loomine.md](./Laenutaotluse-loomine.md)

**Teenus:** `POST /api/bookings`

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/PrimaryKeyNotFoundException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/DataNotFoundException.java`

Rakenduse pakett on ee.toolrental. Handler katab @Valid väljavead ning kohandatud 403/404; ApiError errorCode ja message on String. ForbiddenException konstruktor võtab (message, errorCode). Puuduvad controller/service/persistence klassid ja backend testid; valmis analoogset endpoint’i ei leitud. `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` sisaldab JPA, validation, MapStructi ja testisõltuvusi. `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` ning `3_import.sql` määravad skeemi ja näidisandmed. Teiste taskide olemasolu ei tähenda nende koodi olemasolu.

## Puuduv/muudetav

Vajalikud on allpool loetletud entiteedid/repositooriumid, DTO/mapper, BookingService, BookingController, täpne vealeping ja testid. Ühised klassid luuakse üks kord; enne teostamist kontrolli uuesti paralleelsete taskide tehtud muudatusi. SQL skeemi automaatset muutmist see plaan ei nõua.

## Sammud

1. **Persistence entiteedid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/Booking.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/Tool.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUser.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/Profile.java`

Kaardista lähteülesande tabelid ja FK-d JPA-s; ära kasuta cascade REMOVE viiteandmete või jagatud seoste suhtes. FK mudelid, mida teised taskid juba loovad, taaskasuta.

2. **Repositooriumid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/BookingRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/ToolRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUserRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/ProfileRepository.java`

BookingRepository.save; seotud Tool/AppUser ID lugemine. Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/booking/dto/BookingCreateRequestDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/booking/dto/BookingResponseDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/BookingMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/BookingService.java`

BookingService.createBooking(actorId,request): rentija ainult sessioonist; valideeri kohustuslikud väljad ja endDate>=startDate, getValidToolBy, oma tööriista keeld; salvesta status P ja google_event_id null, tagasta BookingResponseDto. Ära lisa sellesse vanasse kontrakti omal algatusel minevikukeeldu, kattumiskeeldu, serveri saadavuse viga ega e-kirja; need on siin lahtised või skoobist väljas. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/booking/BookingController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `POST /api/bookings` ning lähteülesande 200 keha. Actor/userId tuleb sessiooni principal’ist, mitte request body’st; säilita taski osapoole kontrollid. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/LaenutaotluseloomineServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/booking/LaenutaotluseloomineControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

| Olukord | Status code | Response body |
|---|---|---|
| Sessioon puudub või on aegunud. | 401 Unauthorized | Tühi body, kooskõlas autentimise taskiga. |
| Sessiooni kasutaja ID võrdub `tool.owner_id` väärtusega. | 403 Forbidden | `{"errorCode":"OWN_TOOL_BOOKING_FORBIDDEN","message":"Enda tööriista broneerimine ei ole lubatud."}` |
| Tööriista ID ei eksisteeri, näites 123. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'toolId' väärtusega: 123"}` |
| `endDate < startDate`. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"endDate: peab olema startDate'iga samal päeval või hiljem"}` |
| Kohustuslik väärtus puudub, ID pole positiivne, kuupäev on vigane või teade ületab 500 märki. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"<väli>: <valideerimisvea kirjeldus>"}`; täpne tekst sõltub valideerimisreeglist. |
| Ootamatu salvestamise tõrge. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Laenutaotluse saatmine ebaõnnestus. Palun proovi hiljem uuesti."}` |

403 reegel ja FE teate sõnastus on kasutaja kinnitatud; sama sõnum backendis on selle taski lepingu täpsustus. 400 kuupäevavahemiku ja 404 vead pärinevad märkmetest; PDF-i 99 ja märkmete 123 on üksnes puuduva ID näited. Ülejäänud sisendivalideerimine ning 500 leping on tehnilised täpsustused. Kasuta olemasolevat `ApiError` kuju (`String errorCode`, `String message`), `ForbiddenException` ja `PrimaryKeyNotFoundException` käsitlemist. Praegune veahaldur ei taga veel kõiki kirjeldatud valideerimise ja 500 vastuseid; need tuleb teostamisel katta.

CSRF ebaõnnestumine võib autentimiskihis anda samuti 403, kuid seda ei tohi märgistada `OWN_TOOL_BOOKING_FORBIDDEN` koodiga. FE eristab oma tööriista keeldu nii staatuse kui veakoodi järgi.

## Testid

Ühe päeva periood, 500/501 sõnum, oma tööriist, sessiooni rentija, 404 ja rollback; ilma lisatud kattumise või mineviku nõudeta.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] Autenditud Customer ja Admin saavad saata kirjeldatud request'i `POST /api/bookings` aadressile; autentimata kasutaja saab 401.
- [ ] Edukal loomisel tagastatakse 200 ja kirjeldatud DTO; kirje ID genereeritakse, staatus on P ja rentija tuleb sessioonist.
- [ ] Oma tööriista korral saab nii Customer kui Admin 403 `OWN_TOOL_BOOKING_FORBIDDEN`; booking kirjete arv ei muutu.
- [ ] Kliendilt saadetud renterId ega status ei saa serveri määratud väärtusi muuta.
- [ ] Puuduv tööriist annab 404; vigased/puuduvad kuupäevad, pööratud vahemik, vigane ID ja liiga pikk teade annavad 400.
- [ ] Sama päeva algus ja lõpp on lubatud; ownerMessage puudumine/null ning 500 märki on lubatud, 501 märki mitte.
- [ ] Salvestamise tõrkel ei jää osalist kirjet ning tagastatakse kirjeldatud üldine viga.
- [ ] Teenus ei kinnita taotlust, muuda tööriista staatust ega loo Google sündmust.
- [ ] Automaattestid katavad loomise, sessiooni rentija, oma tööriista keelu koos salvestamise puudumisega, 400/401/404/500, piirväärtused ja tehingu tagasipööramise.
- [ ] Saadavuse serverikontrolli ja kattuvuse lahtised küsimused on nähtavalt eristatud kinnitatud nõuetest.

## Avatud küsimused

Sama POST /api/bookings on kahes erinevas taskis: Laenutaotluse-loomine.md ja Laenutuse-taotluse-loomine.md. Need erinevad kuupäeva-, saadavuse-, kattumise-, e-kirja- ja veasõnumi lepingus. Enne endpoint’i teostamist tuleb valida autoriteetne leping ja kooskõlastada FE; kahte konkureerivat controllerit ei looda. See plaan kirjeldab ainult viidatud faili.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
