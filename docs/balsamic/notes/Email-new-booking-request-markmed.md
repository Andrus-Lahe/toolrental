# Email template new-booking-request märkmed

Allikas: `Laenukas.pdf`, lehekülg 6 (Email template new-booking-request).

Staatus: kinnitatud (nupu sihtrada, kirja teema ja perioodi rida kinnitati 2026-09-25). Backendis pole veel e-kirja saatmise koodi. Lahendus järgib faili [spring_mail.md](spring_mail.md) mustrit (`MailService`, `toolrental.frontend-url`) ja mockupi kollase märkme kirjeldust (Thymeleaf mall `booking-request.html`).

## Erinevus tavalisest vaate sildist

- See leht on **e-kiri**, mitte Vue vaade. Seetõttu on `Failinimi` väljal backendi HTML-mall (`.vue` asemel) ja `Frontend rada` väljal kirjas, kuhu viib kirja nupp.
- Kirjal **pole oma API märget**. Kiri saadetakse backendis automaatselt pärast edukat `POST /api/bookings` päringut (vt [BookingFormView-markmed.md](BookingFormView-markmed.md)).
- Mockupi väärtused „Peeter“, „Aurupesur 1“ ja „13232323?“ on kohatäitjad. Näide allpool kasutab faili `3_import.sql` broneeringut `bookingId = 1`.
- Nupu „Vaata taotlust“ sihtrada `/bookings/{bookingId}` on kinnitatud: see järgib `spring_mail.md` lingi mustrit ja on BookingApprovalView.vue rada.

## Vaate märkmed

```text
Roll: Tööriista omanik (e-kirja saaja)
Failinimi: backend/src/main/resources/templates/email/booking-request.html
Frontend rada: — (e-kiri; nupp "Vaata taotlust" avab /bookings/{bookingId})

Vaatega seotud lisainfo:
Kiri saadetakse automaatselt pärast edukat POST /api/bookings päringut tööriista omaniku profiili e-posti aadressile (profile.email). Teema: "Uus laenutuse taotlus: {toolName}". Reply-To on taotleja e-post.

Java kood annab mallile ainult andmed: toolName, bookingId, renterName, startDate, endDate, bookingUrl. bookingUrl = {toolrental.frontend-url}/bookings/{bookingId}; nupp avab BookingApprovalView.vue vaate, kus omanik kinnitab või lükkab taotluse tagasi.

Kui omanikul pole profiili (e-posti pole), kirja ei saadeta ja see logitakse. Kirja saatmise viga ainult logitakse: broneering jääb salvestatuks ja POST /api/bookings vastab ikka 200.
```

## Malli andmed (näide)

Näide kasutab faili `3_import.sql` broneeringut `bookingId = 1`: Liis Kask (`userId = 3`) taotleb Marko Tamme (`userId = 1`) Akutrelli (`toolId = 1`).

| Malli muutuja | Allikas | Näide |
|---|---|---|
| `toolName` | `tool.name` | Akutrell |
| `bookingId` | `booking.id` | 1 |
| `renterName` | taotleja `app_user.first_name` | Liis |
| `startDate` | `booking.start_date` (`dd.MM.yyyy`) | 02.10.2026 |
| `endDate` | `booking.end_date` (`dd.MM.yyyy`) | 04.10.2026 |
| `bookingUrl` | `toolrental.frontend-url` + `/bookings/{bookingId}` | http://localhost:5173/bookings/1 |

| Kirja väli | Allikas | Näide |
|---|---|---|
| Saaja (To) | omaniku `profile.email` (`tool.owner_id`) | email@Gmail.com |
| Reply-To | taotleja `profile.email` (`booking.renter_id`) | liis.kask@example.com |
| Teema | `"Uus laenutuse taotlus: " + toolName` | Uus laenutuse taotlus: Akutrell |

Näidiskiri (mockupi tekstide järgi):

```text
Sulle on saabunud uus laenutuse taotlus

Laenutuse taotlus

Taotleja: Liis
Tööriist: Akutrell
Broneeringu number: 1
Periood: 02.10.2026 kuni 04.10.2026

Palun kinnita või tühista taotlus

[Vaata taotlust] → http://localhost:5173/bookings/1
```

Rida „Periood“ pole mockupi kirjapildil, kuid kollane märge nimetab malli andmetena `startDate` ja `endDate` ning näitab neid malli näites. Seetõttu on see näidiskirjas olemas (kinnitatud).
