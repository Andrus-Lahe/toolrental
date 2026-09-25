# BookingApprovalView.vue märkmed

Allikas: `Laenukas.pdf`, lehekülg 7 (BookingApprovalView.vue).

Staatus: kinnitatud. Backendis pole veel `BookingController`/`BookingService` klasse. `PATCH .../confirm` ja `PATCH .../reject` teenused, `BookingDecisionRequest` DTO ning omaniku kontroll järgivad faili [spring_mail.md](spring_mail.md) koodinäidet. `GET /api/bookings/{bookingId}`, uued veakoodid ja nende teated, tagasilükkamise modaali suunamine /my-tools lehele ning eestikeelne valideerimisteade on kinnitatud 2026-09-25.

## Kokkulepped

- **Üks sõnumiväli:** laenaja sõnum ja omaniku vastus kasutavad sama välja `booking.owner_message` (kasutaja otsus, tabeleid ei muudeta). Laenaja taotluses (`POST /api/bookings`, vt [BookingFormView-markmed.md](BookingFormView-markmed.md)) salvestatud tekst kuvatakse plokis „Laenaja saatis sulle lisainfo“. Kinnitamisel või tagasilükkamisel kirjutab omaniku „Lisainfo laenajale“ selle üle.
- **Kaks kasutajat, üks vaade:** rada `/bookings/{bookingId}` avatakse nii omaniku e-kirjast ([Email-new-booking-request-markmed.md](Email-new-booking-request-markmed.md)) kui ka rentija otsusekirjast (`spring_mail.md`). Omanik näeb nuppe ja sisestusvälja. Rentija näeb sama vaadet ainult vaatamiseks, ilma nuppude ja sisestusväljata; kontaktkaardil on siis omaniku andmed (kasutaja otsus).
- Mockupi „Aurupesur“ on kohatäitja. Näide kasutab faili `3_import.sql` broneeringut `bookingId = 1` (Akutrell, omanik Marko Tamm `userId = 1`, laenaja Liis Kask `userId = 3`).

## Vaate märkmed

```text
Roll: Customer / Admin (tööriista omanik; broneeringu rentija ainult vaatamiseks)
Failinimi: BookingApprovalView.vue
Frontend rada: /bookings/{bookingId}

Vaatega seotud lisainfo:
Vaade avatakse e-kirja nupust "Vaata taotlust". Avamisel laaditakse GET /api/bookings/{bookingId} abil tööriista nimi, periood, laenaja lisainfo ja kontaktkaart. Kontaktkaardi e-posti link avab Gmaili kirja koostamise akna (https://mail.google.com/mail/?view=cm&fs=1&to={contactEmail}).

Kui isOwner = true ja status = 'P', on nähtavad väli "Lisainfo laenajale (valikuline)" ning nupud "Lükka tagasi" (PATCH /api/bookings/{bookingId}/reject) ja "Kinnita" (PATCH /api/bookings/{bookingId}/confirm). Muul juhul (rentija või juba otsustatud taotlus) on väljad ainult loetavad ja nupud peidetud.

Eduka "Kinnita" järel kuvatakse modaal "Taotlus kinnitatud", eduka "Lükka tagasi" järel modaal "Taotlus tagasi lükatud". Modaali ristist sulgemisel suunatakse kasutaja vaatele Minu tööriistad (/my-tools). Backendi veateade (message) kuvatakse AlertDanger.vue komponendiga.
```

## API märkmed — GET /api/bookings/{bookingId}

```text
API: GET /api/bookings/{bookingId}

BookingApprovalDto.java
Response (200):
{
  "bookingId": 1,
  "toolId": 1,
  "toolName": "Akutrell",
  "startDate": "2026-10-02",
  "endDate": "2026-10-04",
  "status": "P",
  "ownerMessage": null,
  "isOwner": true,
  "contactName": "Liis Kask",
  "contactEmail": "liis.kask@example.com",
  "contactPhone": "55501002"
}

API teenuse lisainfo:
Kasutaja tuvastatakse sessioonist. isOwner = true, kui päringu teeb tööriista omanik; siis on contact* väljadel laenaja andmed, rentija puhul omaniku andmed (profile tabelist). ownerMessage on laenaja sõnum (ootel taotlusel) või omaniku vastus (otsustatud taotlusel) ja võib olla null. status: P = ootel, C = kinnitatud, R = tagasi lükatud.

Veateated:
HTTP: 403
errorCode: BOOKING_ACCESS_DENIED
message: "Sul pole õigust seda broneeringut vaadata"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'bookingId' väärtusega: 123"
```

## API märkmed — PATCH /api/bookings/{bookingId}/confirm

```text
API: PATCH /api/bookings/{bookingId}/confirm

BookingDecisionRequest.java
Request body:
{
  "ownerMessage": "Palun tagasta redel 21. septembril enne kella 18."
}

Response (200): NONE

API teenuse lisainfo:
Omaniku ID võetakse sessioonist. Broneeringu status muutub 'C' ja owner_message kirjutatakse üle päringu väärtusega (võib olla null, kuni 500 märki). Seejärel saadetakse rentijale e-kiri "Broneering kinnitatud"; kirja saatmise viga ainult logitakse.

Veateated:
HTTP: 403
errorCode: BOOKING_NOT_OWNER
message: "Ainult tööriista omanik saab taotlust kinnitada või tagasi lükata"

HTTP: 403
errorCode: BOOKING_NOT_PENDING
message: "Taotlus on juba kinnitatud või tagasi lükatud"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'bookingId' väärtusega: 123"

HTTP: 400
errorCode: INCORRECT_INPUT
message: "ownerMessage: Sõnum võib olla kuni 500 märki"
```

## API märkmed — PATCH /api/bookings/{bookingId}/reject

```text
API: PATCH /api/bookings/{bookingId}/reject

BookingDecisionRequest.java
Request body:
{
  "ownerMessage": "Soovitud kuupäevadel ei saa tööriista välja laenata."
}

Response (200): NONE

API teenuse lisainfo:
Omaniku ID võetakse sessioonist. Broneeringu status muutub 'R' ja owner_message kirjutatakse üle päringu väärtusega (võib olla null, kuni 500 märki). Seejärel saadetakse rentijale e-kiri "Broneering tagasi lükatud"; kirja saatmise viga ainult logitakse.

Veateated:
HTTP: 403
errorCode: BOOKING_NOT_OWNER
message: "Ainult tööriista omanik saab taotlust kinnitada või tagasi lükata"

HTTP: 403
errorCode: BOOKING_NOT_PENDING
message: "Taotlus on juba kinnitatud või tagasi lükatud"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'bookingId' väärtusega: 123"

HTTP: 400
errorCode: INCORRECT_INPUT
message: "ownerMessage: Sõnum võib olla kuni 500 märki"
```

## Veakäsitluse seos olemasoleva projektiga

- `PRIMARY_KEY_NOT_FOUND` tuleb olemasolevast `PrimaryKeyNotFoundException` klassist (404). `spring_mail.md` kasutab seda sama `bookingId` kontrolli jaoks.
- `BOOKING_NOT_OWNER` ja `BOOKING_NOT_PENDING` on `spring_mail.md` koodis `ForbiddenException(...)` kohad, millele see silt annab koodi ja teksti. `BOOKING_ACCESS_DENIED` on uus kood `GET` päringu jaoks. Kõik kolm annavad olemasoleva `ForbiddenException` kaudu 403.
- 400 tuleb `@Size(max = 500)` valideerimisest olemasoleva `handleMethodArgumentNotValid` handleri kaudu (`<väli>: <teade>`). Eestikeelne teade antakse annotatsiooniga `@Size(max = 500, message = "Sõnum võib olla kuni 500 märki")`.
- Näidisandmetes (`3_import.sql`) on ootel ainult `bookingId = 1`; broneeringud 2 (`C`) ja 3 (`R`) annavad kinnitamisel ja tagasilükkamisel vea `BOOKING_NOT_PENDING`.
