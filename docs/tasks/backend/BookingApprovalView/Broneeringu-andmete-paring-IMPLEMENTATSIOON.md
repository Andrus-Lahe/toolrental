# Broneeringu andmete päring — implementatsiooni plaan

**Seotud task:** [Broneeringu-andmete-paring.md](./Broneeringu-andmete-paring.md)

**Teenus:** `GET /api/bookings/{bookingId}`

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

BookingRepository.findBookingWithPartiesById; ProfileRepository.findProfileByUserId teise poole jaoks. Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/booking/dto/BookingApprovalDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/BookingMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/BookingService.java`

BookingService.getBooking(actorId,bookingId): getValidBookingBy → osapoole õiguse kontroll (admin pole erand). Mapper tool/booking väljad; handleContactDetails täidab omaniku jaoks rentija, rentija jaoks omaniku kontaktid. Profiilita kontakt null; ownerMessage tagastatakse muutmata. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/booking/BookingController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `GET /api/bookings/{bookingId}` ning lähteülesande 200 keha. Actor/userId tuleb sessiooni principal’ist, mitte request body’st; säilita taski osapoole kontrollid. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/BroneeringuandmeteparingServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/booking/BroneeringuandmeteparingControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Broneeringut `bookingId = 123` pole. Teates kasutada tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'bookingId' väärtusega: 123"}` |
| Sisse logitud kasutaja pole tööriista omanik ega broneeringu rentija. | 403 Forbidden | `{"errorCode":"BOOKING_ACCESS_DENIED","message":"Sul pole õigust seda broneeringut vaadata"}` |
| `bookingId` pole täisarv. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"bookingId: peab olema Integer-tüüpi täisarv"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Broneeringu laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist.
- `BOOKING_ACCESS_DENIED` on uus kood (kinnitatud sildil). Seda visatakse olemasoleva `ForbiddenException` klassiga.
- Kontrollide järjekord: 404, siis 403.
- Path variable'i 400 ja ühtne 500 kuju tuleb teostamisel tagada, sest praegune `RestExceptionHandler` neid automaatselt ei käsitle. SQL-i ega stack trace'i ei tagastata.

## Testid

Mõlemad osapooled, kõrvaline admin, profiilita teine pool, null sõnum ja kõik vead.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] `GET /api/bookings/{bookingId}` on olemas ja nõuab sisselogimist.
- [ ] HTTP 200 vastus sisaldab täpselt välju `bookingId`, `toolId`, `toolName`, `startDate`, `endDate`, `status`, `ownerMessage`, `isOwner`, `contactName`, `contactEmail`, `contactPhone`.
- [ ] Omanik (`userId = 1`) saab `bookingId = 1` puhul `isOwner = true` ja laenaja Liis Kase kontaktid.
- [ ] Rentija (`userId = 3`) saab `bookingId = 1` puhul `isOwner = false` ja omaniku Marko Tamme kontaktid.
- [ ] `ownerMessage` tagastatakse andmebaasi väärtusega, sh `null`.
- [ ] Kui teisel poolel pole profiili, on `contactEmail` ja `contactPhone` `null`, kuid vastus on 200.
- [ ] Kõrvaline kasutaja (ka admin) saab 403 `BOOKING_ACCESS_DENIED`.
- [ ] Olematu `bookingId` annab 404 ja `PRIMARY_KEY_NOT_FOUND` teate päringu ID-ga.
- [ ] Sisse logimata kasutaja saab 401; vigane `bookingId` annab 400 ja andmebaasi tõrge 500.
- [ ] Päring ei muuda andmeid.
- [ ] Automaattestid katavad omaniku ja rentija vaate, profiilita teise poole, `null` sõnumi, 400/401/403/404/500 juhtumid.

## Avatud küsimused

Lähteülesande põhileping on plaani jaoks piisav; uut ärikäitumist ei lisata.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
