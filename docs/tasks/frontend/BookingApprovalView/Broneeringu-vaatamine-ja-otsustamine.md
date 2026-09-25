# Broneeringu vaatamine ja otsustamine

**Vaade:** `BookingApprovalView.vue`, route `/bookings/:bookingId`.

**Roll:** sisse logitud Customer või Admin, kes on tööriista omanik või selle broneeringu rentija. Admin roll ei anna kõrvalise broneeringu vaatamise ega otsustamise õigust.

**Vaste balsamic mockupis:** BookingApprovalView.vue BE-taski maketipildil; vastav vaade PDF-is lk 7/17: [Laenukas2509.pdf](../../../balsamic/notes/Laenukas2509.pdf#page=7).

![Mockup](./Broneeringu-vaatamine-ja-otsustamine.png)

Allikad: [BookingApprovalView märkmed](../../../balsamic/notes/BookingApprovalView-markmed.md) ning allpool viidatud kolm BE-taski. Maketipilt on kopeeritud BE-taski juurest. `Laenukas2509.pdf` sama lehe BookingConfirmationView nimi, `/bookings/{bookingId}/confirm` vaaterada ja ühine `/status` API on varasem variant: siin järgitakse kasutaja antud BookingApprovalView BE-lepingut ja kinnitatud märkmeid.

## Kasutajavoog

Kasutaja avab e-kirja „Vaata taotlust“ lingi või broneeringu lingi ning vaade laadib taotluse andmed. Omanik näeb rentija kontakte, rentija omaniku kontakte. Ootel taotluse omanik võib lisada sõnumi ja vajutada „Kinnita“ või „Lükka tagasi“; rentija ning otsustatud taotluse omanik näevad ainult lugemisvaadet. Eduka otsuse järel kuvatakse vastav kinnitusmodaal, mille ristist sulgemine viib „Minu tööriistad“ vaatesse (`/my-tools`). Kirjade saatmine ja ühise päise funktsionaalsus kuuluvad teistele taskidele.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Logo, navigatsioon ja Logi välja | Ühine päis | Kasuta ühist päist; ära teosta selle taskiga eraldi sõnumite vaadet ega autentimisteenust. |
| Laenutuse taotlus | Pealkiri | Maketi pealkiri. |
| Tööriista nimi | Tekst | `toolName`; „Aurupesur“ on maketi kohatäitja. |
| Laenutuse periood: Alates / Kuni | Ainult loetavad kuupäevad | `startDate`, `endDate`; kuvamine `dd.MM.yyyy`, kuupäevi siin ei muudeta. |
| Laenaja saatis sulle lisainfo | Ainult loetav tekst | Ootel taotluse `ownerMessage`; otsustatud taotlusel sama väli on omaniku vastus ja pealkiri „Lisainfo omanikult“. |
| Kontaktkaart | Tekst | `contactName`, `contactEmail`, `contactPhone`. Omanikule laenaja, rentijale omaniku kaart. |
| Kontaktkaardi e-post | Link | Gmaili koostamisaken, siht `https://mail.google.com/mail/?view=cm&fs=1&to={contactEmail}`; aadress URL-kodeerida. |
| Lisainfo laenajale (valikuline) | Textarea | Omaniku uus vastus, kuni 500 märki; ainult `isOwner === true && status === 'P'`. |
| Lükka tagasi | Nupp | `PATCH /api/bookings/{bookingId}/reject`, ainult otsustamisõigusega omanikule. |
| Kinnita | Nupp | `PATCH /api/bookings/{bookingId}/confirm`, sama nähtavusreegel. |
| Taotlus kinnitatud / Taotlus tagasi lükatud | Modaal | Kuvatakse ainult vastava PATCH 200 järel; sulgemine suunab `/my-tools`. |
| AlertDanger | Veateade | Backend'i `message`; puuduva vastuse puhul eestikeelne üldteade. |

## Käitumine ja valideerimine

1. Lae `beforeMount` ajal route'i `bookingId` järgi andmed. Tehniline ettepanek: kui parameeter puudub või pole Java Integer vahemikus täisarv, kuva „Vigane broneeringu ID.“ ning ära saada päringut. Sama vaate ID muutumisel lae uus broneering ja puhasta eelmise andmed/sõnum; vananenud päringuvastus ei tohi uut broneeringut üle kirjutada.
2. Laadimise ajal kuva laadimisolek ja peida otsustamisvorm. GET vea või tühja/ootamatu vastuse korral ära näita eelnevalt laaditud isikuandmeid ega nuppe. GET ei tagasta nimekirja: 404 on veateade, mitte tühi nimekiri.
3. Arvuta `canDecide` backend'i `isOwner` ja `status` põhjal. Ära tuleta õigust Admin rollist ega kliendi salvestatud kasutaja ID-st. `P` = ootel, `C` = kinnitatud, `R` = tagasi lükatud; tundmatu oleku korral nuppe ei näidata.
4. Hoia serveri `booking.ownerMessage` ja sisestatav `decision.ownerMessage` eraldi. Uus vastuseväli algab tühjana; laenaja sõnumit sinna ei kopeerita. Backend kasutab mõlema poole jaoks sama andmebaasivälja: otsus kirjutab vana sõnumi üle. Vaade ei luba vana sõnumi säilimist pärast otsust.
5. Omaniku sõnum on valikuline. Sea `maxlength="500"` ja kontrolli pikkust enne päringut; ületamisel kuva „Sõnum võib olla kuni 500 märki“ ning päringut ei saadeta. Tühja välja korral saada `ownerMessage: null`; ülejäänud tekst saada muutmata. Kuupäevi, staatust, omaniku/rentija ID-d ega e-posti otsuse body ei sisalda.
6. PATCH ajal keela mõlemad otsusenupud ja sisestusväli (`isSubmitting`); üks vajutus saadab ühe päringu. `.finally()` lõpetab ooteoleku ka vea korral. Vormil pole automaatset kordussaatmist.
7. Ainult HTTP 200 korral muuda kohalik olek C/R-ks, peida otsustamine ning ava õige tekstiga modaal. Tühjast response body'st JSON-i ei loeta. Modaali `event-modal-closed` suunab `/my-tools`; ka üldise modaali teised lubatud sulgemisviisid peavad läbima sama handleri.
8. `BOOKING_NOT_PENDING` korral kuva teade, keela otsustamine ja lae GET abil värske seis. `BOOKING_NOT_OWNER` korral ära luba uuesti otsustada. 404 korral eemalda taotluse sisu. 400 korral säilita sisestatud sõnum parandamiseks.
9. 401 korral kuva „Palun logi sisse.“ ja kasuta ühist Google sisselogimisvoogu; ära leiuta uut login-route'i. Sisselogimise järel lae broneering uuesti, ära korda automaatselt varasemat PATCH päringut.
10. Võrguvea/PATCH 500 korral kuva veateade ja ära näita edumodaali. Tehniline ettepanek: enne käsitsi kordamist lae broneering uuesti, sest katkestuse korral võib toiming serveris juba õnnestuda. Ära väida, et e-kiri on kohale jõudnud: selle viga ei muuda BE 200 vastust.
11. Null-sõnumi ja puuduvate kontaktandmete korral kuva „—“, mitte `null`. Puuduva e-posti korral linki ei looda. Gmaili link avaneb uuel vahelehel (`target="_blank"`, `rel="noopener noreferrer"`); rakendus ise selle lingiga kirja ei saada. Nimesid ja sõnumeid kuva Vue tekstisidumisega, mitte `v-html` kaudu.

## API kutsed

Kontrollitud koodibaasis puuduvad BookingController, BookingService ja broneeringu DTO-d. API allikaks on kasutaja antud BE-taskid; nende lepinguid ei asendata maketi vanema API märkmega. Kõik päringud kasutavad ühist sessioonipõhist Axios seadistust. Ära lisa body/query sisse kasutaja ID-d.

### `GET /api/bookings/{bookingId}`

**Backend task:** [Broneeringu-andmete-paring.md](../../backend/BookingApprovalView/Broneeringu-andmete-paring.md).

Path variable `bookingId`: Integer. Query parameetrid ja request body puuduvad. Näide: `GET /api/bookings/1`.

`BookingApprovalDto.java` — response (200), omaniku vaade:

```json
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
```

Sama taotlus rentija vaates, response (200):

```json
{
  "bookingId": 1,
  "toolId": 1,
  "toolName": "Akutrell",
  "startDate": "2026-10-02",
  "endDate": "2026-10-04",
  "status": "P",
  "ownerMessage": null,
  "isOwner": false,
  "contactName": "Marko Tamm",
  "contactEmail": "email@Gmail.com",
  "contactPhone": "56565656"
}
```

`startDate` ja `endDate` on `yyyy-MM-dd` kuupäevastringid; ära teisenda neid ajavööndiga kuupäevaks, mis võib muuta päeva. `ownerMessage`, `contactEmail` ja `contactPhone` võivad olla null. `isOwner` on boolean, mitte string.

**Veateated:** `ApiError` kuju on `{"errorCode":"...","message":"..."}`; 401 body on tühi. Allpool on BE-taski kõik veakoodid ja sõnastused.

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 Unauthorized | `—` | Body puudub | Peida otsustamine, kuva „Palun logi sisse.“ ja kasuta ühist sisselogimisvoogu. |
| 404 Not Found | `PRIMARY_KEY_NOT_FOUND` | Ei leidnud primary keyd 'bookingId' väärtusega: 123 | Kuva AlertDanger kaudu message; edumodaali ei avata. Eemalda taotluse sisu ja otsustamisvorm. |
| 403 Forbidden | `BOOKING_ACCESS_DENIED` | Sul pole õigust seda broneeringut vaadata | Kuva AlertDanger kaudu message; edumodaali ei avata. Peida keelatud andmed/toimingud vastavalt päringule. |
| 400 Bad Request | `INCORRECT_INPUT` | bookingId: peab olema Integer-tüüpi täisarv | Kuva AlertDanger kaudu message; edumodaali ei avata. Paranda sisend; PATCH korral säilita sõnum. |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | Broneeringu laadimine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva AlertDanger kaudu message; edumodaali ei avata. |

404 teate `123` on näidis; kuva backend’i tegelik message. Võrguvea või ootamatu veakuju puhul kasuta „Päring ebaõnnestus. Palun proovi uuesti.“ (FE tehniline ettepanek).

### `PATCH /api/bookings/{bookingId}/confirm`

**Backend task:** [Broneeringu-kinnitamine.md](../../backend/BookingApprovalView/Broneeringu-kinnitamine.md).

Path variable `bookingId`: Integer. Query parameetrid puuduvad. `BookingDecisionRequest.java` — request body:

```json
{
  "ownerMessage": "Palun tagasta redel 21. septembril enne kella 18."
}
```

Ilma sõnumita:

```json
{"ownerMessage": null}
```

**Response (200):** tühi body. Staatus muutub C-ks. Backend kirjutab `owner_message` üle, sh null-väärtusega.

**Veateated:** `ApiError` kuju on `{"errorCode":"...","message":"..."}`; 401 body on tühi. Allpool on BE-taski kõik veakoodid ja sõnastused.

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 Unauthorized | `—` | Body puudub | Peida otsustamine, kuva „Palun logi sisse.“ ja kasuta ühist sisselogimisvoogu. |
| 400 Bad Request | `INCORRECT_INPUT` | ownerMessage: Sõnum võib olla kuni 500 märki | Kuva AlertDanger kaudu message; edumodaali ei avata. Paranda sisend; PATCH korral säilita sõnum. |
| 404 Not Found | `PRIMARY_KEY_NOT_FOUND` | Ei leidnud primary keyd 'bookingId' väärtusega: 123 | Kuva AlertDanger kaudu message; edumodaali ei avata. Eemalda taotluse sisu ja otsustamisvorm. |
| 403 Forbidden | `BOOKING_NOT_OWNER` | Ainult tööriista omanik saab taotlust kinnitada või tagasi lükata | Kuva AlertDanger kaudu message; edumodaali ei avata. Peida keelatud andmed/toimingud vastavalt päringule. |
| 403 Forbidden | `BOOKING_NOT_PENDING` | Taotlus on juba kinnitatud või tagasi lükatud | Kuva AlertDanger kaudu message; edumodaali ei avata. Keela otsustamine ja lae seis uuesti. |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | Taotluse kinnitamine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva AlertDanger kaudu message; edumodaali ei avata. |

404 teate `123` on näidis; kuva backend’i tegelik message. Võrguvea või ootamatu veakuju puhul kasuta „Päring ebaõnnestus. Palun proovi uuesti.“ (FE tehniline ettepanek).

### `PATCH /api/bookings/{bookingId}/reject`

**Backend task:** [Broneeringu-tagasilukkamine.md](../../backend/BookingApprovalView/Broneeringu-tagasilukkamine.md).

Path variable `bookingId`: Integer. Query parameetrid puuduvad. `BookingDecisionRequest.java` — request body:

```json
{
  "ownerMessage": "Soovitud kuupäevadel ei saa tööriista välja laenata."
}
```

Ilma sõnumita:

```json
{"ownerMessage": null}
```

**Response (200):** tühi body. Staatus muutub R-ks. Backend kirjutab `owner_message` üle, sh null-väärtusega.

**Veateated:** `ApiError` kuju on `{"errorCode":"...","message":"..."}`; 401 body on tühi. Allpool on BE-taski kõik veakoodid ja sõnastused.

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 Unauthorized | `—` | Body puudub | Peida otsustamine, kuva „Palun logi sisse.“ ja kasuta ühist sisselogimisvoogu. |
| 400 Bad Request | `INCORRECT_INPUT` | ownerMessage: Sõnum võib olla kuni 500 märki | Kuva AlertDanger kaudu message; edumodaali ei avata. Paranda sisend; PATCH korral säilita sõnum. |
| 404 Not Found | `PRIMARY_KEY_NOT_FOUND` | Ei leidnud primary keyd 'bookingId' väärtusega: 123 | Kuva AlertDanger kaudu message; edumodaali ei avata. Eemalda taotluse sisu ja otsustamisvorm. |
| 403 Forbidden | `BOOKING_NOT_OWNER` | Ainult tööriista omanik saab taotlust kinnitada või tagasi lükata | Kuva AlertDanger kaudu message; edumodaali ei avata. Peida keelatud andmed/toimingud vastavalt päringule. |
| 403 Forbidden | `BOOKING_NOT_PENDING` | Taotlus on juba kinnitatud või tagasi lükatud | Kuva AlertDanger kaudu message; edumodaali ei avata. Keela otsustamine ja lae seis uuesti. |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | Taotluse tagasilükkamine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva AlertDanger kaudu message; edumodaali ei avata. |

404 teate `123` on näidis; kuva backend’i tegelik message. Võrguvea või ootamatu veakuju puhul kasuta „Päring ebaõnnestus. Palun proovi uuesti.“ (FE tehniline ettepanek).

## Komponendid ja failistruktuur

Järgi [projekti struktuuri](../../../frontend/projekti-struktuur.md) ja [Vue Options API juhist](../../../frontend/vue-komponendi-struktuur.md).

| Fail | Vastutus |
|---|---|
| `frontend/src/views/BookingApprovalView.vue` | Route'i ID, laadimine, eraldi otsusevormi olek, rollipõhine kuvamine, API vastused ja modaal. |
| `frontend/src/api-services/BookingService.js` | `sendGetBookingRequest(bookingId)`, `sendConfirmBookingRequest(bookingId, bookingDecisionRequest)`, `sendRejectBookingRequest(bookingId, bookingDecisionRequest)`; Axios tagastab promise'i. |
| `frontend/src/components/common/BookingContactCard.vue` | Teise poole nimi, e-post ja telefon; Gmaili link. |
| `frontend/src/components/forms/BookingDecisionForm.vue` | Sõnum, 500 märgi kontroll, mõlemad nupud; emits `event-confirm`, `event-reject`. |
| `frontend/src/components/modals/BookingDecisionModal.vue` | Otsuse kinnitus ja `event-modal-closed`. |
| `frontend/src/components/common/AlertDanger.vue` | Backend'i message ja tehnilise vea fallback; taaskasuta ühist komponenti, kui see teostamise hetkeks olemas on. |
| `frontend/src/navigation/NavigationService.js` | Ühine suunamine „Minu tööriistad“ vaatesse. |
| `frontend/src/router/index.js` | Lisa `/bookings/:bookingId` ja vaate seos. |

**Hetkeseis:** vaade, nimetatud alamkomponendid ja API teenus puuduvad. Routeris on praegu ainult `/` ja `/test`; `/my-tools` ning ühine autentimine on sõltuvused, mitte juba olemasolevad lahendused. Sihtvaate teostus kuulub MyToolsView taski.

Kasuta Options API-t: `data`, `computed`, `methods`, `beforeMount`; iga API päring `.then()` / `.catch()` / `.finally()` ahelana eraldi `handle...Response` ja `handle...Error` meetoditega. Serveri objekt ja vastusevorm hoitakse eraldi; `computed` määrab `canDecide`. Lehe sisu ei tohi lubada manipuleeritud kliendiõigustega teenuse kasutamist — lõplik kontroll on backendis.

## Vastuvõtu kriteeriumid

- [ ] `/bookings/1` avamisel laetakse GET kaudu Akutrelli taotlus ja periood 02.10.2026–04.10.2026.
- [ ] Omanik näeb Liis Kase kontakte; rentija Marko Tamme kontakte. Kasutatakse GET `contact*` välju, mitte lisapäringut kasutaja kontaktidele.
- [ ] Ainult `isOwner === true && status === 'P'` kuvab vastusevälja ja mõlemad otsusenupud; Admin roll ei anna erandit.
- [ ] Kuupäevad, taotleja sõnum ja kontaktid on ainult loetavad. Omaniku vastus algab tühjana ega kasuta vaikimisi laenaja sõnumit.
- [ ] Null-sõnum, puuduv e-post ja telefon ei tekita vigast linki ega `null` teksti.
- [ ] 500 märki on lubatud; 501 märgi korral päringut ei saadeta. Tühi sõnum saadetakse nullina.
- [ ] Mõlemad PATCH kutsed saadavad ainult `ownerMessage`; URL sisaldab vaadatava broneeringu ID-d.
- [ ] Topeltvajutus ega teise otsusenupu vajutamine poolelioleva päringu ajal ei tekita teist päringut.
- [ ] Confirm 200 avab „Taotlus kinnitatud“, reject 200 „Taotlus tagasi lükatud“; sulgemine viib `/my-tools`.
- [ ] C/R olekus omanik ning P/C/R olekus rentija ei saa otsustada. Otsustatud taotlusel käsitletakse `ownerMessage` välja omaniku vastusena.
- [ ] Kõik API tabelite 400/401/403/404/500 vead ning võrguviga on kaetud; viga ei ava edumodaali.
- [ ] `BOOKING_NOT_PENDING` värskendab andmeid; laadimisviga ei jäta nähtavale varasema broneeringu kontakte.
- [ ] E-posti link sisaldab URL-kodeeritud `contactEmail` väärtust ja avab Gmaili koostamisakna; puuduv e-post pole klikitav.
- [ ] Andmete laadimine, ID muutumine, lugemisvaade, valideerimine ja modaalide suunamine on kontrollitud mockitud API vastustega; kontrollid ei saada päris e-kirju.
