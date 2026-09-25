# BookingFormView.vue märkmed

## Vaate märkmed

```text
Roll: Customer / Admin (sisse logitud kasutaja, kes pole tööriista omanik)
Failinimi: BookingFormView.vue
Frontend rada: /tools/{toolId}/booking

Vaatega seotud lisainfo:
Vaade avatakse tööriista detailvaatelt (ToolDetailView.vue) "Laenuta" nupu kaudu. Avanemisel laetakse GET /api/tools/{toolId} abil tööriista põhiinfo, mida kuvatakse vormi kohal. Kui tööriista status ei ole 'A' (saadaval), on saatmisnupp keelatud ja kuvatakse teade, et tööriist pole saadaval. Nupule "Saada" vajutamisel tehakse päring POST /api/bookings ning eduka vastuse järel kuvatakse kinnitusmodaal teatega "Taotlus saadetud", mille sulgemisel suunatakse kasutaja lehele /my-bookings. Nupule "Tühista" vajutamisel suunatakse kasutaja tagasi eelmisele vaatele ilma API kutseta.
```

## API märkmed — POST /api/bookings

```text
API: POST /api/bookings

BookingCreateRequestDto.java
Request body:
{
  "toolId": 5,
  "startDate": "2026-10-10",
  "endDate": "2026-10-12",
  "ownerMessage": "Sooviksin muruniiduki kätte saada reede õhtul."
}

BookingResponseDto.java
Response (200):
{
  "bookingId": 4,
  "toolId": 5,
  "renterId": 3,
  "startDate": "2026-10-10",
  "endDate": "2026-10-12",
  "status": "P",
  "ownerMessage": "Sooviksin muruniiduki kätte saada reede õhtul."
}

API teenuse lisainfo:
renterId võetakse sisse logitud kasutaja sessioonist, mitte päringu body'st. Uue broneeringu staatus on alati algselt 'P' (ootel) — tööriista omanik kinnitab ('C') või lükkab selle hiljem tagasi ('R'). startDate peab olema täna või tulevikus (Europe/Tallinn aja järgi). ownerMessage on valikuline laenaja sõnum omanikule (kuni 500 märki). Taotlus ei tohi kattuda sama tööriista ootel ('P') või kinnitatud ('C') broneeringuga (kattumine: olemasolev start_date <= uus endDate ja olemasolev end_date >= uus startDate); tagasi lükatud ('R') broneering perioodi ei hõiva. Pärast salvestamist saadetakse omanikule e-kiri new-booking-request; kirja saatmise viga ainult logitakse.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'toolId' väärtusega: 123"

HTTP: 400
errorCode: INCORRECT_INPUT
message: "endDate: peab olema startDate'iga samal päeval või hiljem"

HTTP: 400
errorCode: INCORRECT_INPUT
message: "startDate: ei tohi olla minevikus"

HTTP: 403
errorCode: OWN_TOOL_BOOKING_FORBIDDEN
message: "Enda tööriista ei saa laenata"

HTTP: 403
errorCode: TOOL_UNAVAILABLE
message: "Tööriist pole hetkel saadaval"

HTTP: 403
errorCode: TOOL_ALREADY_BOOKED
message: "Tööriist on valitud perioodil juba broneeritud"
```
