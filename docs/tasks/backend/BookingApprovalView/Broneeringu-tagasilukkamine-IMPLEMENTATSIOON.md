# Broneeringu tagasilükkamine — implementatsiooni plaan

**Seotud task:** [Broneeringu-tagasilukkamine.md](./Broneeringu-tagasilukkamine.md)

**Teenus:** `PATCH /api/bookings/{bookingId}/reject`

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

BookingRepository lukustatud kirje otsing või tingimuslik P→otsus uuendus; ProfileRepository optional kontaktid. Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/booking/dto/BookingDecisionRequest.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/BookingMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/BookingService.java`

BookingService otsusemeetod kasutab ühist decideBooking(actorId,id,message,targetStatus=R): 400 → 404 → BOOKING_NOT_OWNER → BOOKING_NOT_PENDING. Kirjuta status, owner_message (ka null) ja updated_at; teised väljad säilivad. Samaaegset confirm/reject ei tohi mõlemat õnnestunuks lugeda. Lisa MailService JavaMailSender-iga, rentijale kiri, omaniku Reply-To. Puuduva rentijaprofiili või MailException puhul logi, säilita 200. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/booking/BookingController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `PATCH /api/bookings/{bookingId}/reject` ning lähteülesande 200 keha. Actor/userId tuleb sessiooni principal’ist, mitte request body’st; säilita taski osapoole kontrollid. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

Kirja infrastruktuur: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/mail/MailService.java`, `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` mail starter ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/resources/application.properties` MAIL_USERNAME/MAIL_PASSWORD ning frontend-url. JavaMailSender testis mock; tegelikke kirju dokumentide koostamisel ei saadeta. Kontrolli taski saajat, Reply-To fallback'i ja teemat/sisu; kõik kasutajatekstid HTML malli puhul kodeerida. Loomise HTML mall: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/resources/templates/email/booking-request.html`; otsusekirjade plain text sisu koostab service. SMTP erind logitakse taski järgi, mitte ei pöörata edukat otsust tagasi.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/BroneeringutagasilukkamineServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/booking/BroneeringutagasilukkamineControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| `ownerMessage` on pikem kui 500 märki. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"ownerMessage: Sõnum võib olla kuni 500 märki"}` |
| Broneeringut `bookingId = 123` pole. Teates kasutada tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'bookingId' väärtusega: 123"}` |
| Päringu tegija pole tööriista omanik (ka rentija või admin). | 403 Forbidden | `{"errorCode":"BOOKING_NOT_OWNER","message":"Ainult tööriista omanik saab taotlust kinnitada või tagasi lükata"}` |
| Broneeringu staatus pole `P`. | 403 Forbidden | `{"errorCode":"BOOKING_NOT_PENDING","message":"Taotlus on juba kinnitatud või tagasi lükatud"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Taotluse tagasilükkamine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist.
- `BOOKING_NOT_OWNER` ja `BOOKING_NOT_PENDING` on uued koodid (kinnitatud sildil), samad mis kinnitamisel. Neid visatakse olemasoleva `ForbiddenException` klassiga `spring_mail.md` meetodis `decideBooking`.
- 400 tuleb `@Valid` + `@Size` kaudu olemasolevast `handleMethodArgumentNotValid` handlerist.
- Kontrollide järjekord: 400, 404, `BOOKING_NOT_OWNER`, siis `BOOKING_NOT_PENDING`. Vea korral andmeid ei muudeta ja e-kirja ei saadeta.
- E-kirja saatmise viga ei ole veaolukord: see logitakse ja vastus on 200.

## Testid

P→otsus, C/R keeld, sõnum500/501/null, osapoolte õigused, konkurents, SMTP viga ja saaja/Reply-To/link.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] `PATCH /api/bookings/{bookingId}/reject` on olemas ja nõuab sisselogimist.
- [ ] Omanik `userId = 1` lükkab tagasi `bookingId = 1`: vastus 200 tühja body'ga; `status = 'R'`, `owner_message` = päringu väärtus ja `updated_at` on uuendatud.
- [ ] `ownerMessage: null` või puuduv väli kirjutab `owner_message` väärtuseks `NULL`.
- [ ] Rentijale saadetakse e-kiri teemaga `Broneering tagasi lükatud: Akutrell`, saajaks `liis.kask@example.com` ja Reply-To `email@Gmail.com`.
- [ ] Rentija profiili puudumisel või `MailException` korral on vastus ikka 200 ja broneering tagasi lükatud; viga logitakse.
- [ ] Mitte-omanik saab 403 `BOOKING_NOT_OWNER`; juba otsustatud broneering (`bookingId = 2` või `3`) annab 403 `BOOKING_NOT_PENDING`.
- [ ] Olematu `bookingId` annab 404; üle 500 märgi pikk sõnum annab 400 eestikeelse teatega.
- [ ] Vea korral broneering ei muutu ja e-kirja ei saadeta.
- [ ] Automaattestid (e-kirja saatmine `JavaMailSender` mockiga) katavad eduka tagasilükkamise, `null` sõnumi, kirja sisu ja saajad, profiilita rentija, kirja saatmise vea ning 400/401/403/404/500 juhtumid.

## Avatud küsimused

Kattumise garantii eeldab, et [Laenutaotluse-loomine.md](../BookingFormView/Laenutaotluse-loomine.md) kattumise kontroll (TOOL_ALREADY_BOOKED) on teostatud. SMTP saatmine transaktsiooni sees on lähteülesandes: commit võib hiljem ebaõnnestuda. AFTER_COMMIT parandaks seda, kuid ajastuse muutus tuleb kooskõlastada; plaan ei väida vaikimisi, et lähteülesanne seda juba nõuab.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
