# Laenutuse taotluse loomine — implementatsiooni plaan

**Seotud task:** [Laenutuse-taotluse-loomine.md](./Laenutuse-taotluse-loomine.md)

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

BookingRepository.save; existsOverlappingBookingsByToolIdAndPeriod JPQL: status IN (P,C), startDate<=newEnd ja endDate>=newStart; ToolRepository lukustatud lugemine. Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/booking/dto/BookingCreateRequestDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/booking/dto/BookingResponseDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/BookingMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/BookingService.java`

BookingService.createBooking(actorId,request): rentija ainult sessioonist; valideeri kohustuslikud väljad ja endDate>=startDate, getValidToolBy, oma tööriista keeld; salvesta status P ja google_event_id null, tagasta BookingResponseDto. Europe/Tallinn Clock ja @FutureOrPresent sama kella järgi; seejärel TOOL_UNAVAILABLE, TOOL_ALREADY_BOOKED P/C kaasaarvatud piirpäevade kattumisel. Lukusta tool rida enne kattumiskontrolli ja insert’i samas transaktsioonis, et ka esimest samaaegset broneeringut serialiseerida. Omanikule HTML kiri booking-request.html mallist; puuduv omaniku profiil/SMTP viga ainult logitakse. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/booking/BookingController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `POST /api/bookings` ning lähteülesande 200 keha. Actor/userId tuleb sessiooni principal’ist, mitte request body’st; säilita taski osapoole kontrollid. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/LaenutusetaotluseloomineServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/booking/LaenutusetaotluseloomineControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| `endDate` on varasem kui `startDate`. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"endDate: peab olema startDate'iga samal päeval või hiljem"}` |
| `startDate` on minevikus. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"startDate: ei tohi olla minevikus"}` |
| `toolId`, `startDate` või `endDate` puudub; `ownerMessage` on üle 500 märgi; kuupäeva vorming on vale. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"<väli>: <valideerimise teade>"}` |
| Tööriista `toolId = 123` pole. Teates kasutada tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'toolId' väärtusega: 123"}` |
| Kasutaja on tööriista omanik (ka admin). | 403 Forbidden | `{"errorCode":"OWN_TOOL_BOOKING_FORBIDDEN","message":"Enda tööriista ei saa laenata"}` |
| Tööriista `status = 'U'`. | 403 Forbidden | `{"errorCode":"TOOL_UNAVAILABLE","message":"Tööriist pole hetkel saadaval"}` |
| Periood kattub sama tööriista `P` või `C` broneeringuga. | 403 Forbidden | `{"errorCode":"TOOL_ALREADY_BOOKED","message":"Tööriist on valitud perioodil juba broneeritud"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Laenutuse taotluse saatmine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist.
- `OWN_TOOL_BOOKING_FORBIDDEN`, `TOOL_UNAVAILABLE` ja `TOOL_ALREADY_BOOKED` on uued koodid (kinnitatud sildil). Neid visatakse olemasoleva `ForbiddenException` klassiga.
- 400 tuleb `@Valid` kaudu olemasolevast `handleMethodArgumentNotValid` handlerist. See loeb ainult väljavigu (`getFieldErrors().getFirst()`), seega peab `endDate` kontroll tekitama väljavea nimega `endDate`, mitte klassitaseme (global) vea. Muidu handler viskab erindi.
- Kuupäeva vale vorming (`HttpMessageNotReadableException`) ja ühtne 500 kuju tuleb teostamisel eraldi käsitleda; praegune handler neid ei kata.
- Kontrollide järjekord: 400, 404, `OWN_TOOL_BOOKING_FORBIDDEN`, `TOOL_UNAVAILABLE`, siis `TOOL_ALREADY_BOOKED`. Vea korral broneeringut ei looda ja e-kirja ei saadeta.
- E-kirja saatmise viga ei ole veaolukord: see logitakse ja vastus on 200.

## Testid

Ühe päeva periood, 500/501 sõnum, oma tööriist, sessiooni rentija, 404 ja rollback; eile/täna/homme fikseeritud kellaga, P/C/R piirid, kaks samaaegset kattuvat taotlust ning e-kirja sisu/tõrge.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] `POST /api/bookings` on olemas ja nõuab sisselogimist.
- [ ] Näites toodud päring (Liis, `toolId = 5`) annab 200 ja `BookingResponseDto` väljadega `bookingId`, `toolId`, `renterId`, `startDate`, `endDate`, `status`, `ownerMessage`.
- [ ] Loodud rea `status = 'P'`, `renter_id` on sessiooni kasutaja ID (body's antud `renterId` ignoreeritakse) ja `google_event_id = NULL`.
- [ ] `ownerMessage: null` või puuduv väli on lubatud.
- [ ] Omanikule saadetakse e-kiri teemaga `Uus laenutuse taotlus: Muruniiduk`, saajaks `email@Gmail.com`, Reply-To `liis.kask@example.com` ja lingiga `/bookings/4`.
- [ ] Omaniku profiili puudumisel või `MailException` korral on vastus ikka 200 ja broneering salvestatud; viga logitakse.
- [ ] Kattuv `P` või `C` broneering annab 403 `TOOL_ALREADY_BOOKED`; `R` broneering ja piiriga mittekattuv periood ei takista.
- [ ] Enda tööriist annab 403 `OWN_TOOL_BOOKING_FORBIDDEN`; `status = 'U'` tööriist 403 `TOOL_UNAVAILABLE`.
- [ ] Olematu `toolId` annab 404; `endDate < startDate` ja muud valideerimisvead annavad 400.
- [ ] Minevikus algav `startDate` annab 400 teatega `startDate: ei tohi olla minevikus`; täna algav periood on lubatud.
- [ ] Vea korral broneeringut ei looda ja e-kirja ei saadeta.
- [ ] Automaattestid (e-kirja saatmine `JavaMailSender` mockiga) katavad eduka loomise, kõik testiandmete näited, kattumise piirjuhud (sama algus- või lõpupäev), alguskuupäeva piirjuhud (eile, täna, homme), kirja sisu ja saajad, profiilita omaniku, kirja saatmise vea ning 400/401/403/404/500 juhtumid.

## Avatud küsimused

Sama POST /api/bookings on kahes erinevas taskis: Laenutaotluse-loomine.md ja Laenutuse-taotluse-loomine.md. Need erinevad kuupäeva-, saadavuse-, kattumise-, e-kirja- ja veasõnumi lepingus. Enne endpoint’i teostamist tuleb valida autoriteetne leping ja kooskõlastada FE; kahte konkureerivat controllerit ei looda. See plaan kirjeldab ainult viidatud faili.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
