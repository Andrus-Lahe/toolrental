# Email template booking-rejected märkmed

Allikas: `Laenukas.pdf`, lehekülg 9 (Email template booking-rejected).

Staatus: ettepanek. Järgib [Email-booking-accepted-markmed.md](Email-booking-accepted-markmed.md) kinnitatud otsuseid: saaja on laenaja, nupp „Vaata taotlust“, rida „Periood“ ja HTML-mall. Backendis pole veel e-kirja saatmise koodi. Mall on kaustas `templates/email/` (lehekülje 6 kollase märkme failipuus nimega `booking-rejected.html`). Saatmise loogika (millal, kellele, Reply-To, vigade logimine) tuleb failist [spring_mail.md](spring_mail.md).

## Erinevus tavalisest vaate sildist ja mockupist

- See leht on **e-kiri**, mitte Vue vaade. `Failinimi` väljal on backendi HTML-mall ja `Frontend rada` väljal kirjas, kuhu viib kirja nupp.
- Kirjal **pole oma API märget**. Kiri saadetakse backendis automaatselt pärast edukat `PATCH /api/bookings/{bookingId}/reject` päringut (vt [BookingApprovalView-markmed.md](BookingApprovalView-markmed.md)).
- Kollane märge nimetab nuppu „Vaata taotlust“, mida mockupi kirjapildil pole. Silt lisab selle nupu, nagu booking-accepted kirjal. Nupp viib BookingApprovalView lehele (`/bookings/{bookingId}`).
- Rida „Periood“ pole mockupil. Silt lisab selle, nagu booking-accepted kirjal.
- Erinevalt kinnituskirjast pole mockupil omaniku e-posti ega telefoni ridu, sest laenutust ei toimu. Silt neid ka ei lisa. Laenaja saab omanikule vastata Reply-To kaudu.
- `spring_mail.md` koostab kirja lihttekstina (`SimpleMailMessage`, `createText`). Mall `booking-rejected.html` asendab selle teksti HTML-kirjaga.
- Mockupi väärtused („Peeter“, „Aurupesur 1“, „Ei saa laenutada, läks katki“, „13232323?“) on kohatäitjad. Näide allpool kasutab faili `3_import.sql` tagasi lükatud broneeringut `bookingId = 3`.

## Vaate märkmed

```text
Roll: Laenaja / rentija (e-kirja saaja)
Failinimi: backend/src/main/resources/templates/email/booking-rejected.html
Frontend rada: — (e-kiri; nupp "Vaata taotlust" avab /bookings/{bookingId})

Vaatega seotud lisainfo:
Kiri saadetakse automaatselt pärast edukat PATCH /api/bookings/{bookingId}/reject päringut laenaja profiili e-posti aadressile (profile.email). Teema: "Broneering tagasi lükatud: {toolName}". Reply-To on omaniku e-post, et laenaja saaks otse omanikule vastata.

Java kood annab mallile ainult andmed: toolName, bookingId, startDate, endDate, ownerName, ownerMessage, bookingUrl. bookingUrl = {toolrental.frontend-url}/bookings/{bookingId}. Kui ownerMessage on tühi, rida "Lisainfo omanikult" jäetakse välja.

Kui laenajal pole profiili (e-posti pole), kirja ei saadeta ja see logitakse. Kirja saatmise viga ainult logitakse: broneering jääb tagasi lükatuks ja PATCH päring vastab ikka 200.
```

## Malli andmed (näide)

Näide kasutab faili `3_import.sql` broneeringut `bookingId = 3`: Liis Kask (`userId = 3`) on tagasi lükanud Marko Tamme (`userId = 1`) taotluse Hekikääridele (`toolId = 4`).

| Malli muutuja | Allikas | Näide |
|---|---|---|
| `toolName` | `tool.name` | Hekikäärid |
| `bookingId` | `booking.id` | 3 |
| `startDate` | `booking.start_date` (`dd.MM.yyyy`) | 05.10.2026 |
| `endDate` | `booking.end_date` (`dd.MM.yyyy`) | 07.10.2026 |
| `ownerName` | omaniku `app_user.first_name` + `last_name` | Liis Kask |
| `ownerMessage` | `booking.owner_message` | Soovitud kuupäevadel ei saa tööriista välja laenata. |
| `bookingUrl` | `toolrental.frontend-url` + `/bookings/{bookingId}` | http://localhost:5173/bookings/3 |

| Kirja väli | Allikas | Näide |
|---|---|---|
| Saaja (To) | laenaja `profile.email` (`booking.renter_id`) | email@Gmail.com |
| Reply-To | omaniku `profile.email` (`tool.owner_id`), tühi kui profiili pole | liis.kask@example.com |
| Teema | `"Broneering tagasi lükatud: " + toolName` | Broneering tagasi lükatud: Hekikäärid |

Näidiskiri (mockupi tekstide järgi):

```text
Sinu broneeringu taotlus on tagasi lükatud

Laenutuse taotlus

Omanik: Liis Kask
Tööriist: Hekikäärid
Periood: 05.10.2026 kuni 07.10.2026
Lisainfo omanikult: Soovitud kuupäevadel ei saa tööriista välja laenata.
Broneeringu number: 3

[Vaata taotlust] → http://localhost:5173/bookings/3
```
