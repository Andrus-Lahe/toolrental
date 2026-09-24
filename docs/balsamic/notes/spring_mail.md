# E-kirjade saatmine Spring Bootiga (`spring-boot-starter-mail`)

See juhend kirjeldab, kuidas toolrental projektis saata e-kiri rentijale, kui tööriista omanik broneeringu **kinnitab** või **tagasi lükkab**. Kirjas on ka omaniku sõnum (`booking.owner_message`).

## Üldpilt

1. Rentija teeb broneeringu, mille staatus on `P` (ootel).
2. Omanik avab broneeringu, kirjutab soovi korral sõnumi ja vajutab „Kinnita“ või „Lükka tagasi“.
3. Backend kontrollib, et päringu teeb tööriista omanik ja et broneering on veel ootel.
4. Backend salvestab uue staatuse (`C` või `R`) ja omaniku sõnumi välja `booking.owner_message`.
5. Backend leiab rentija e-posti aadressi tabelist `profile` ja saadab talle kirja.

## Andmebaasi struktuur

| Tabel / väli | Kasutus kirjas |
|---|---|
| `booking.status` | `P` = ootel, `C` = kinnitatud, `R` = tagasi lükatud |
| `booking.owner_message` | omaniku sõnum rentijale (kuni 500 märki, võib olla tühi) |
| `booking.start_date`, `booking.end_date` | rendiperiood |
| `booking.renter_id` → `profile.user_id` | **saaja**: `profile.email` |
| `booking.tool_id` → `tool.owner_id` → `profile.user_id` | **omanik**: tema e-post läheb väljale *Reply-To* |
| `tool.name` | tööriista nimi kirja teemas ja sisus |
| `app_user.first_name` | pöördumine („Tere, Liis!“) |

E-posti aadress on `profile` tabelis, mitte `app_user` tabelis. Kasutajal ei pruugi profiili veel olla (vt `googlega_login.md`). Seepärast tuleb profiili otsida `Optional` tüübiga ja arvestada, et see võib puududa.

Skeemi muutma ei pea.

## 1. Sõltuvus (`backend/build.gradle`)

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-mail'
}
```

## 2. Seadistus (`backend/src/main/resources/application.properties`)

Õppeprojektis on kõige lihtsam kasutada Gmaili SMTP serverit:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

toolrental.frontend-url=http://localhost:5173
```

### Gmaili rakenduse parool

Gmail ei luba SMTP kaudu kasutada tavalist Google'i parooli. Vaja on **rakenduse parooli** (*App password*):

1. Lülita Google'i kontol sisse kaheastmeline kinnitamine (*2-Step Verification*).
2. Ava aadress https://myaccount.google.com/apppasswords ja loo uus rakenduse parool.
3. Määra IntelliJ-s keskkonnamuutujad (*Run Configuration → Environment variables*):
   ```
   MAIL_USERNAME=sinu.konto@gmail.com
   MAIL_PASSWORD=abcdefghijklmnop
   ```

**Parooli ei tohi kunagi Git'i panna.** Seepärast on `application.properties` failis ainult `${MAIL_PASSWORD}`.

Gmail kirjutab saatja (*From*) alati üle sinu konto aadressiga, seega kõik kirjad lähevad välja ühelt aadressilt. Omaniku e-post pannakse väljale *Reply-To*, et rentija saaks vastata otse omanikule.

## 3. Kirja saatmise teenus (`MailService`)

`MailService` teab ainult seda, kuidas kiri välja saata. Kirja sisu koostab `BookingService`.

```java
package ee.toolrental.infrastructure.mail;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendMail(String to, String replyTo, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setReplyTo(replyTo);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("E-kirja saatmine aadressile {} ebaõnnestus", to, e);
        }
    }
}
```

**Miks viga ainult logitakse?** Broneeringu kinnitamine on tähtsam kui e-kiri. Kui Gmail pole parasjagu kättesaadav, peab broneering ikkagi kinnitatuks jääma. Kui erindit edasi visata, rullib `@Transactional` ka staatuse muudatuse tagasi.

`JavaMailSender` klassi loob Spring ise, kui `spring.mail.host` on seadistatud.

## 4. Broneeringu kinnitamine ja tagasilükkamine

### Päringu DTO

```java
public record BookingDecisionRequest(
        @Size(max = 500) String ownerMessage
) {}
```

`@Size(max = 500)` vastab andmebaasi väljale `owner_message varchar(500)`.

### Controller

```java
@PatchMapping("/api/bookings/{bookingId}/confirm")
public void confirmBooking(@AuthenticationPrincipal AppUserPrincipal principal,
                           @PathVariable Integer bookingId,
                           @RequestBody @Valid BookingDecisionRequest request) {
    bookingService.confirmBooking(principal.getUserId(), bookingId, request.ownerMessage());
}

@PatchMapping("/api/bookings/{bookingId}/reject")
public void rejectBooking(@AuthenticationPrincipal AppUserPrincipal principal,
                          @PathVariable Integer bookingId,
                          @RequestBody @Valid BookingDecisionRequest request) {
    bookingService.rejectBooking(principal.getUserId(), bookingId, request.ownerMessage());
}
```

Omaniku ID tuleb sessioonist (`AppUserPrincipal`), mitte frontendist.

### Service

```java
package ee.toolrental.service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final String STATUS_PENDING = "P";
    private static final String STATUS_CONFIRMED = "C";
    private static final String STATUS_REJECTED = "R";

    private final BookingRepository bookingRepository;
    private final ProfileRepository profileRepository;
    private final MailService mailService;

    @Value("${toolrental.frontend-url}")
    private String frontendUrl;

    @Transactional
    public void confirmBooking(Integer ownerId, Integer bookingId, String ownerMessage) {
        Booking booking = decideBooking(ownerId, bookingId, STATUS_CONFIRMED, ownerMessage);
        sendDecisionMail(booking);
    }

    @Transactional
    public void rejectBooking(Integer ownerId, Integer bookingId, String ownerMessage) {
        Booking booking = decideBooking(ownerId, bookingId, STATUS_REJECTED, ownerMessage);
        sendDecisionMail(booking);
    }

    private Booking decideBooking(Integer ownerId, Integer bookingId, String newStatus, String ownerMessage) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("bookingId", bookingId));

        if (!booking.getTool().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException(...);   // ainult tööriista omanik tohib otsustada
        }
        if (!STATUS_PENDING.equals(booking.getStatus())) {
            throw new ForbiddenException(...);   // otsustada saab ainult ootel broneeringut
        }

        booking.setStatus(newStatus);
        booking.setOwnerMessage(ownerMessage);
        booking.setUpdatedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    private void sendDecisionMail(Booking booking) {
        AppUser renter = booking.getRenter();
        AppUser owner = booking.getTool().getOwner();

        Optional<Profile> renterProfile = profileRepository.findByUser(renter);
        if (renterProfile.isEmpty()) {
            log.warn("Rentijal {} puudub profiil, e-kirja ei saadetud", renter.getId());
            return;
        }
        String ownerEmail = profileRepository.findByUser(owner)
                .map(Profile::getEmail)
                .orElse(null);

        mailService.sendMail(
                renterProfile.get().getEmail(),
                ownerEmail,
                createSubject(booking),
                createText(booking));
    }

    private String createSubject(Booking booking) {
        String decision = STATUS_CONFIRMED.equals(booking.getStatus()) ? "kinnitatud" : "tagasi lükatud";
        return "Broneering " + decision + ": " + booking.getTool().getName();
    }

    private String createText(Booking booking) {
        String decision = STATUS_CONFIRMED.equals(booking.getStatus())
                ? "Omanik kinnitas sinu broneeringu."
                : "Kahjuks lükkas omanik sinu broneeringu tagasi.";

        String ownerMessage = booking.getOwnerMessage() == null || booking.getOwnerMessage().isBlank()
                ? ""
                : "\nOmaniku sõnum:\n" + booking.getOwnerMessage() + "\n";

        return """
                Tere, %s!

                %s

                Tööriist: %s
                Periood: %s kuni %s
                %s
                Broneeringut saad vaadata siit:
                %s/bookings/%d

                Omanikule saad vastata otse sellele kirjale vastates.

                ToolRental
                """.formatted(
                booking.getRenter().getFirstName(),
                decision,
                booking.getTool().getName(),
                booking.getStartDate(),
                booking.getEndDate(),
                ownerMessage,
                frontendUrl,
                booking.getId());
    }
}
```

`ProfileRepository` vajab meetodit:

```java
Optional<Profile> findByUser(AppUser user);
```

Märkused:

- Veateated ja veakoodid lisa `ErrorResponse` enumisse ning kasuta neid `ForbiddenException` erindis (vt `backend/CLAUDE.md`).
- `owner_message` on kasutaja sisestatud tekst. Kuna `SimpleMailMessage` saadab lihtteksti, ei saa sinna HTML-i ega skripte sokutada.
- Kui omanikul pole profiili, jääb `replyTo` tühjaks ja rentija vastus läheb rakenduse Gmaili kontole.
- Kuupäevad kuvatakse kujul `2026-10-02`. Eestipärase kuju jaoks kasuta `DateTimeFormatter.ofPattern("dd.MM.yyyy")`.

### Näidiskiri

```
Teema: Broneering kinnitatud: Redel

Tere, Liis!

Omanik kinnitas sinu broneeringu.

Tööriist: Redel
Periood: 2026-09-18 kuni 2026-09-21

Omaniku sõnum:
Palun tagasta redel 21. septembril enne kella 18.

Broneeringut saad vaadata siit:
http://localhost:5173/bookings/2

Omanikule saad vastata otse sellele kirjale vastates.

ToolRental
```

## 5. Testimine

- **Demokasutajate aadressid on väljamõeldud.** Enne testimist muuda rentija (Liis, `user_id = 3`) e-post enda omaks:
  ```sql
  UPDATE profile SET email = 'sinu.konto@gmail.com' WHERE user_id = 3;
  ```
  Tagasi saad selle `3_import.sql` uuesti käivitades.
- **Testitav broneering:** `3_import.sql` failis on broneering `id = 1` staatusega `P`: tööriist Akutrell, omanik Marko (`user_id = 1`), rentija Liis (`user_id = 3`). Kinnitamiseks pead olema sisse logitud Markona (vt `googlega_login.md`, jaotis „Testimine demokasutajatega“).
- **Kui kiri ei tule kohale**, vaata backendi logist `MailService` veateadet. Levinumad põhjused:
  - `535 Authentication failed`: kasutasid tavalist parooli, mitte rakenduse parooli;
  - keskkonnamuutujad pole IntelliJ run configuration'is määratud;
  - kiri läks rämpsposti kausta.
- **Unit-testides** asenda `JavaMailSender` mockiga (`@MockitoBean`), et testid ei saadaks päris kirju.

## Edasijõudnutele: kiri alles pärast commit'i

Ülaltoodud lahenduses saadetakse kiri transaktsiooni sees. Kui andmebaasi commit peaks pärast kirja saatmist ebaõnnestuma, on rentija saanud kirja broneeringu kohta, mille staatus tegelikult ei muutunud. Õppeprojektis on see risk väike.

Täpsem lahendus on avaldada sündmus ja saata kiri alles pärast edukat commit'i:

```java
// BookingService
private final ApplicationEventPublisher eventPublisher;
...
eventPublisher.publishEvent(new BookingDecidedEvent(booking.getId()));
```

```java
@Component
@RequiredArgsConstructor
public class BookingMailListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingDecided(BookingDecidedEvent event) {
        // loe broneering uuesti ja saada kiri
    }
}
```

Kui soovid, et kasutaja ei peaks kirja saatmist ootama (Gmail võib vastata 1–2 sekundit), lisa meetodile `@Async` ja rakenduse klassile `@EnableAsync`.

## Muud võimalused

- **Omaniku teavitamine uuest broneeringust.** Sama `MailService` abil saab broneeringu loomisel saata omanikule kirja „Sinu tööriistale on uus broneeringusoov“. Saaja on sel juhul omaniku `profile.email` ja *Reply-To* rentija e-post.
- **HTML-kiri.** `SimpleMailMessage` asemel kasuta `MimeMessageHelper` klassi ja `setText(html, true)`. HTML-kirjas tuleb `owner_message` enne sisestamist HTML-kodeerida.
- **Gmaili akna avamine kasutaja enda kontolt.** Kui kiri peab minema kasutaja enda nimel, vt `Mail_compose.md`.
