# BookingConfirmationView.vue märkmed

## Vaate märkmed

```text
Roll: Customer / Admin (sisse logitud kasutaja, kes on tööriista omanik)
Failinimi: BookingConfirmationView.vue
Frontend rada: /bookings/{bookingId}/confirm

Vaatega seotud lisainfo:
Vaade avatakse e-kirjas oleva lingi "Vaata taotlust" kaudu või teavituste/broneeringute nimekirjast. Avanemisel laetakse GET /api/bookings/{bookingId} abil taotluse andmed (tööriista nimi, laenutuse periood, laenaja kontaktandmed ja laenaja saadetud lisainfo). Tööriista omanik saab sisestada omapoolse lisainfo laenajale ning vajutada nuppu "Kinnita" (staatus 'C') või "Lükka tagasi" (staatus 'R'), mis teeb päringu PATCH /api/bookings/{bookingId}/status. Nupule "Kinnita" vajutamisel kuvatakse modaal "Taotlus kinnitatud" ja nupule "Lükka tagasi" vajutamisel modaal "Taotlus tagasi lükatud", mille ristist sulgemisel suunatakse kasutaja vaatesse Minu tööriistad (/my-tools).
```

## API märkmed — GET /api/bookings/{bookingId}

```text
API: GET /api/bookings/{bookingId}

BookingDetailResponse.java
Response (200):
{
  "bookingId": 2,
  "toolName": "Aurupesur",
  "startDate": "2026-09-18",
  "endDate": "2026-09-21",
  "status": "P",
  "renterFirstName": "Peeter",
  "renterLastName": "Kask",
  "renterEmail": "peeter@gmail.com",
  "renterPhone": "55123456",
  "renterMessage": "Palun nädalavahetuseks lisainfoga",
  "ownerMessage": ""
}

API teenuse lisainfo:
Tagastab konkreetse broneeringu taotluse andmed koos laenaja kontaktinfo ja sõnumiga omanikule ülevaatamiseks.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'bookingId' väärtusega: 123"

HTTP: 403
errorCode: FORBIDDEN
message: "Sul puudub õigus selle broneeringu vaatamiseks"
```

## API märkmed — PATCH /api/bookings/{bookingId}/status

```text
API: PATCH /api/bookings/{bookingId}/status

BookingStatusUpdateRequest.java
Request body:
{
  "status": "C",
  "ownerMessage": "Tule kohe järgi, redel on valmis."
}

Response (200): NONE

API teenuse lisainfo:
Tööriista omanik saab kinnitada taotluse (status = 'C') või lükata selle tagasi (status = 'R'). ownerMessage on valikuline väli lisainfo edastamiseks laenajale. Staatuse muutmisel saadetakse laenajale vastav teavituskiri e-mailile.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'bookingId' väärtusega: 123"

HTTP: 403
errorCode: FORBIDDEN
message: "Ainult tööriista omanik saab broneeringu staatust muuta"
```
