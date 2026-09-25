# Email template booking-accepted märkmed

Allikas: `Laenukas.pdf`, lehekülg 8 (Email template booking-accepted).

Staatus: kinnitatud (saaja = laenaja, nupp „Vaata taotlust“, rida „Periood“ ja HTML-mall kinnitati 2026-09-25). Backendis pole veel e-kirja saatmise koodi. Kiri järgib sama mustrit nagu [Email-new-booking-request-markmed.md](Email-new-booking-request-markmed.md): Thymeleaf mall kaustas `templates/email/` (lehekülje 6 kollase märkme failipuus on selle nimi `booking-confirmed.html`). Saatmise loogika (millal, kellele, Reply-To, vigade logimine) tuleb failist [spring_mail.md](spring_mail.md).

## Erinevus tavalisest vaate sildist ja mockupist

- See leht on **e-kiri**, mitte Vue vaade. `Failinimi` väljal on backendi HTML-mall ja `Frontend rada` väljal kirjas, kuhu viib kirja nupp.
- Kirjal **pole oma API märget**. Kiri saadetakse backendis automaatselt pärast edukat `PATCH /api/bookings/{bookingId}/confirm` päringut (vt [BookingApprovalView-markmed.md](BookingApprovalView-markmed.md)).
- Kollane märge ütleb „läheb **omanikule** e-mail“, kuid märke algus („Teade laenajale“) ja kirja sisu („Palun võta ühendust omanikuga“) näitavad, et saaja on **laenaja**. Silt kasutab laenajat, nagu ka `spring_mail.md`.
- Kollane märge nimetab nuppu „Vaata taotlust“, mida mockupi kirjapildil pole. Silt lisab selle nupu, nagu new-booking-request kirjal. Nupp viib BookingApprovalView lehele (`/bookings/{bookingId}`), kus rentija näeb broneeringut ainult vaatamiseks.
- Rida „Periood“ pole mockupil, kuid see on `spring_mail.md` kirjas ja new-booking-request kirjas. Silt lisab selle.
- `spring_mail.md` koostab kirja lihttekstina (`SimpleMailMessage`, `createText`). Mall `booking-confirmed.html` asendab selle teksti HTML-kirjaga, nagu new-booking-request kirja puhul.
- Mockupi väärtused („Peeter“, „pets@test.ee“, „55661111“, „Aurupesur 1“, „tule kohe järgi“, „13232323?“) on kohatäitjad. Näide allpool kasutab faili `3_import.sql` kinnitatud broneeringut `bookingId = 2`.

## Vaate märkmed

```text
Roll: Laenaja / rentija (e-kirja saaja)
Failinimi: backend/src/main/resources/templates/email/booking-confirmed.html
Frontend rada: — (e-kiri; nupp "Vaata taotlust" avab /bookings/{bookingId})

Vaatega seotud lisainfo:
Kiri saadetakse automaatselt pärast edukat PATCH /api/bookings/{bookingId}/confirm päringut laenaja profiili e-posti aadressile (profile.email). Teema: "Broneering kinnitatud: {toolName}". Reply-To on omaniku e-post, et laenaja saaks otse omanikule vastata.

Java kood annab mallile ainult andmed: toolName, bookingId, startDate, endDate, ownerName, ownerEmail, ownerPhone, ownerMessage, bookingUrl. bookingUrl = {toolrental.frontend-url}/bookings/{bookingId}. Kui ownerMessage on tühi, rida "Lisainfo omanikult" jäetakse välja. Kui omanikul pole profiili, jäetakse read "E-mail" ja "Telefon" välja.

Kui laenajal pole profiili (e-posti pole), kirja ei saadeta ja see logitakse. Kirja saatmise viga ainult logitakse: broneering jääb kinnitatuks ja PATCH päring vastab ikka 200.
```

## Malli andmed (näide)

Näide kasutab faili `3_import.sql` broneeringut `bookingId = 2`: Marko Tamm (`userId = 1`) on kinnitanud Liis Kase (`userId = 3`) taotluse Redelile (`toolId = 2`).

| Malli muutuja | Allikas | Näide |
|---|---|---|
| `toolName` | `tool.name` | Redel |
| `bookingId` | `booking.id` | 2 |
| `startDate` | `booking.start_date` (`dd.MM.yyyy`) | 18.09.2026 |
| `endDate` | `booking.end_date` (`dd.MM.yyyy`) | 21.09.2026 |
| `ownerName` | omaniku `app_user.first_name` + `last_name` | Marko Tamm |
| `ownerEmail` | omaniku `profile.email` | email@Gmail.com |
| `ownerPhone` | omaniku `profile.phone` | 56565656 |
| `ownerMessage` | `booking.owner_message` | Palun tagasta redel 21. septembril enne kella 18. |
| `bookingUrl` | `toolrental.frontend-url` + `/bookings/{bookingId}` | http://localhost:5173/bookings/2 |

| Kirja väli | Allikas | Näide |
|---|---|---|
| Saaja (To) | laenaja `profile.email` (`booking.renter_id`) | liis.kask@example.com |
| Reply-To | omaniku `profile.email` (`tool.owner_id`) | email@Gmail.com |
| Teema | `"Broneering kinnitatud: " + toolName` | Broneering kinnitatud: Redel |

Näidiskiri (mockupi tekstide järgi):

```text
Sinu laenutuse taotlus on kinnitatud

Laenutuse taotlus

Palun võta ühendust omanikuga

Omanik: Marko Tamm
E-mail: email@Gmail.com
Telefon: 56565656
Tööriist: Redel
Periood: 18.09.2026 kuni 21.09.2026
Lisainfo omanikult: Palun tagasta redel 21. septembril enne kella 18.
Broneeringu number: 2

[Vaata taotlust] → http://localhost:5173/bookings/2
```
