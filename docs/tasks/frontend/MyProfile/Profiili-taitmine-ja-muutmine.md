# Profiili täitmine ja muutmine

**Vaade:** `MyProfile.vue`, route `/profile`

**Roll:** Customer / Admin (Google'iga sisse logitud kasutaja)

**Vaste balsamic mockupis:** "MyProfile.vue", lehekülg 11/17 failis [Laenukas2509.pdf](../../../balsamic/notes/Laenukas2509.pdf) (vt lisatud pilt `Profiili-taitmine-ja-muutmine.png`)

![Mockup](./Profiili-taitmine-ja-muutmine.png)

Allikas: kinnitatud silt [MyProfile-markmed.md](../../../balsamic/notes/MyProfile-markmed.md). PDF-i sinine märkus kirjeldab vana lahendust (`RegisterView.vue`, `/register`, `POST /api/users`); silt asendab selle ja see task järgib silti.

## Kasutajavoog

Kasutaja jõuab vaatesse päise lingist „Profiil“ või automaatselt pärast esimest Google'iga sisselogimist, kui `GET /api/me` vastab `hasProfile: false` (see suunamine kuulub päise/sessioonikontrolli taski, vt [Header märkmed](../../../balsamic/notes/Header-markmed.md)). Vaate avamisel laaditakse profiili andmed ja linnade nimekiri. Esmasel täitmisel on eesnimi, perenimi ja e-post Google'i andmetega eeltäidetud, hilisemal muutmisel näeb kasutaja oma salvestatud andmeid. Kasutaja valib linna, seejärel linnaosa, täidab ülejäänud väljad ja vajutab „Salvesta“. Eduka salvestamise järel suunatakse ta avalehele (`/`).

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| „Minu profiil“ | Pealkiri | Vaate pealkiri. |
| Veateade | `AlertDanger.vue` | Frontendi valideerimise teade või backendi `message`. Peidetud, kui viga pole. |
| Eesnimi | Tekstiväli, kohustuslik | `firstName`, kuni 100 märki. |
| Perenimi | Tekstiväli, kohustuslik | `lastName`, kuni 100 märki. Google võib anda tühja perenime (`""`), siis peab kasutaja selle täitma. |
| E-post | E-posti väli, kohustuslik | `email`, kuni 254 märki. |
| Telefon | Tekstiväli, kohustuslik | `phone`, kuni 32 märki. |
| Tänava nimi | Tekstiväli, kohustuslik | `streetName`, kuni 150 märki. |
| Majanumber | Tekstiväli, kohustuslik | `houseNumber`, kuni 20 märki. |
| Korteri number (valikuline) | Tekstiväli, valikuline | `apartmentNumber`, kuni 20 märki. **Mockupil puudub**; silt lisab selle Majanumbri alla. |
| Linnaosa | Rippmenüü, kohustuslik | Valitud linna linnaosad (`GET /api/cities/{cityId}/districts`). Keelatud, kuni linn pole valitud. Esimene valik **„Vali linnaosa“** (mockupil „Kõik linnaosad“). |
| Linn | Rippmenüü, kohustuslik | Kõik linnad (`GET /api/cities`). Esimene valik **„Vali linn“** (mockupil „Kõik linnad“). |
| Salvesta | Nupp | Käivitab valideerimise ja `PUT /api/users/me/profile`. Päringu ajal keelatud. |

**Väljade järjekord** on täpselt nagu mockupis (kinnitatud): Eesnimi, Perenimi, E-post, Telefon, Tänava nimi, Majanumber, Korteri number (valikuline), **Linnaosa**, **Linn**. Linnaosa rippmenüü on küll Linna kohal, kuid jääb keelatuks, kuni linn on valitud.

**Rippmenüüde esimene valik** (kinnitatud): „Vali linn“ ja „Vali linnaosa“. Mockupi „Kõik linnad“ / „Kõik linnaosad“ on ToolsView filtri tekstid; profiilivormis tähendab esimene valik „pole valitud“.

## Käitumine ja valideerimine

1. **Avamine (`beforeMount`):** käivita paralleelselt `GET /api/users/me/profile` ja `GET /api/cities`.
2. **Profiili vastus:** täida vormi väljad. Kui `cityId` ei ole `null`, lae `GET /api/cities/{cityId}/districts` ja vali `districtId`. `hasProfile: false` korral on nimi ja e-post eeltäidetud, ülejäänud väljad tühjad (`null` → tühi väli).
3. **Linna muutmine:** tühjenda linnaosa valik (`districtId`) ja lae valitud linna linnaosad. Kui linn tühjendatakse, tühjenda ka linnaosade nimekiri.
4. **Salvesta vajutamisel frontendi valideerimine:** kõik väljad peale „Korteri number“ peavad olema täidetud (tühikud eemaldatakse `trim()`-iga) ning linn ja linnaosa valitud. Kui mõni puudub, näita `AlertDanger.vue`-s teadet **„Täida kõik kohustuslikud väljad“** ja ära saada päringut.
5. **Päringu keha:** saada `firstName`, `lastName`, `email`, `phone`, `districtId`, `streetName`, `houseNumber`, `apartmentNumber`. `cityId`, `userId` ja `hasProfile` ei saadeta. Tühi korterinumber saada kujul `null`.
6. **Edu (200):** uuenda rakenduse ühist kasutajaolekut (nt korda `GET /api/me`, et `hasProfile` oleks `true` ja päise nimi uueneks) ning suuna kasutaja avalehele `/`.
7. **Viga:** näita backendi `message` väärtust `AlertDanger.vue`-s (vt veatabel). Vorm jääb täidetuks, et kasutaja saaks parandada.
8. **Laadimise olek:** kuni profiili vastus pole saabunud, ära luba salvestada (nt nupp keelatud või vorm laadimise olekus).

## API kutsed

### `GET /api/users/me/profile`

**Backend task:** vt [docs/tasks/backend/MyProfile/Profiili-andmete-paring.md](../../backend/MyProfile/Profiili-andmete-paring.md). Backendi koodi (controller/DTO) veel pole; kontrakt on backend taskist.

Sisendid ja request body puuduvad; kasutaja tuvastatakse sessiooniküpsisest.

`ProfileDto.java` — response (200), profiiliga kasutaja:
```json
{
  "userId": 3,
  "firstName": "Liis",
  "lastName": "Kask",
  "email": "liis.kask@example.com",
  "phone": "55501002",
  "cityId": 1,
  "districtId": 2,
  "streetName": "Sõpruse pst",
  "houseNumber": "120",
  "apartmentNumber": "8",
  "hasProfile": true
}
```

`ProfileDto.java` — response (200), profiilita kasutaja (esimene sisselogimine):
```json
{
  "userId": 583,
  "firstName": "Mari",
  "lastName": "Maasikas",
  "email": "user@gmail.com",
  "phone": null,
  "cityId": null,
  "districtId": null,
  "streetName": null,
  "houseNumber": null,
  "apartmentNumber": null,
  "hasProfile": false
}
```

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | — | — (tühi body) | Sessioon puudub või aegus: tühjenda kasutajaolek ja suuna avalehele (`/`), kus on nupp „Logi sisse / Registreeru“. |
| 500 | INTERNAL_SERVER_ERROR | „Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti.“ | Näita `message` `AlertDanger.vue`-s; salvestamine keelatud. |
| Vastus puudub | — | — | Võrguviga: näita üldist veateadet; ära eelda `error.response` olemasolu. |

### `PUT /api/users/me/profile`

**Backend task:** vt [docs/tasks/backend/MyProfile/Profiili-salvestamine.md](../../backend/MyProfile/Profiili-salvestamine.md). Backendi koodi veel pole; kontrakt on backend taskist.

`ProfileUpdateRequestDto.java` — request body:
```json
{
  "firstName": "Liis",
  "lastName": "Kask",
  "email": "liis.kask@example.com",
  "phone": "55501002",
  "districtId": 2,
  "streetName": "Sõpruse pst",
  "houseNumber": "120",
  "apartmentNumber": "8"
}
```

Response (200): **NONE** (tühi body).

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 403 | EMAIL_ALREADY_EXISTS | „Sellise e-postiga kasutaja on juba süsteemis olemas“ | Näita `message`; kasutaja muudab e-posti. |
| 404 | PRIMARY_KEY_NOT_FOUND | „Ei leidnud primary keyd 'districtId' väärtusega: 99“ | Näita `message`; lae linnaosad uuesti. |
| 400 | INCORRECT_INPUT | „firstName: ei tohi olla tühi“ (üldiselt `<väli>: <teade>`) | Näita `message`. Frontendi valideerimine peaks selle enamasti ära hoidma. |
| 401 | — | — (tühi body) | Nagu GET päringu juures. |
| 500 | INTERNAL_SERVER_ERROR | „Profiili salvestamine ebaõnnestus. Palun proovi hiljem uuesti.“ | Näita `message`; vorm jääb täidetuks. |
| Vastus puudub | — | — | Võrguviga: näita üldist veateadet. |

### `GET /api/cities`

**Backend task:** vt [docs/tasks/backend/ToolsView/Linnade-nimekirja-paring.md](../../backend/ToolsView/Linnade-nimekirja-paring.md). Sama teenus mis ToolsView filtris; avalik (`googlega_login.md` `permitAll`).

`CityDto.java` — response (200):
```json
[
  {
    "cityId": 1,
    "cityName": "Tallinn"
  },
  {
    "cityId": 2,
    "cityName": "Tartu"
  },
  {
    "cityId": 3,
    "cityName": "Pärnu"
  }
]
```

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 500 | INTERNAL_SERVER_ERROR | „Linnade laadimine ebaõnnestus. Palun proovi hiljem uuesti.“ | Näita `message`; linna valida ei saa. |

### `GET /api/cities/{cityId}/districts`

**Backend task:** vt [docs/tasks/backend/ToolsView/Valitud-linna-linnaosade-paring.md](../../backend/ToolsView/Valitud-linna-linnaosade-paring.md). Avalik.

`DistrictDto.java` — response (200), näide `cityId = 1` (Tallinn, lühendatud):
```json
[
  {
    "districtId": 1,
    "districtName": "Kristiine"
  },
  {
    "districtId": 2,
    "districtName": "Mustamäe"
  }
]
```

Tallinnas on 8 linnaosa (id 1–8), Tartus 18 (9–26), Pärnus 7 (27–33).

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 404 | PRIMARY_KEY_NOT_FOUND | „Ei leidnud primary keyd 'cityId' väärtusega: 123“ | Näita `message`; tühjenda linnaosade nimekiri. |
| 400 | INCORRECT_INPUT | „cityId: peab olema Integer-tüüpi täisarv“ | Ei tohiks rippmenüü valikuga tekkida; näita `message`. |
| 500 | INTERNAL_SERVER_ERROR | „Linnaosade laadimine ebaõnnestus. Palun proovi hiljem uuesti.“ | Näita `message`. |

## Komponendid ja failistruktuur

| Fail | Vastutus / praegune seis |
|---|---|
| `frontend/src/views/MyProfile.vue` | **Puudub.** Vaade: vormi olek, andmete laadimine `beforeMount`-is, valideerimine, salvestamine. Failinimi sildi järgi `MyProfile.vue` (mitte `MyProfileView.vue`). |
| `frontend/src/components/common/AlertDanger.vue` | **Puudub.** Korduvkasutatav veateate komponent (prop `errorMessage`); kasutavad ka teised vaated. |
| `frontend/src/components/forms/CitiesDropdown.vue` | **Puudub.** Linna rippmenüü (props `cities`, `selectedCityId`, emit `event-new-city-selected`). Saab jagada ToolsView filtriga. |
| `frontend/src/components/forms/DistrictsDropdown.vue` | **Puudub.** Linnaosa rippmenüü (props `districts`, `selectedDistrictId`, `isDisabled`, emit `event-new-district-selected`). |
| `frontend/src/api-services/ProfileService.js` | **Puudub.** `sendGetMyProfileRequest()`, `sendPutMyProfileRequest(profile)`. |
| `frontend/src/api-services/CityService.js` | **Puudub.** `sendGetCitiesRequest()`, `sendGetCityDistrictsRequest(cityId)`. Jagatud ToolsView'ga. |
| `frontend/src/navigation/NavigationService.js` | **Puudub.** Nt `navigateToHomeView()`. |
| `frontend/src/router/index.js` | Sisaldab ainult `/` ja `/test`. **Rada `/profile` puudub** — lisada (nt `name: 'profileRoute'`). |
| `frontend/vite.config.js` | Proxy `/api` on olemas; profiili päringute jaoks muud vaja pole. |

Järgi `docs/frontend/vue-komponendi-struktuur.md` Options API järjekorda (`name`, `components`, `props`, `emits`, `data`, `computed`, `methods`, `beforeMount`), `event-` eesliitega sündmusi ja `.then()/.catch()/.finally()` mustrit eraldi `handle...` meetoditega. Olemasolevat koodi selle taski koostamisel ei muudetud.

## Vastuvõtu kriteeriumid

- [ ] Rada `/profile` avab `MyProfile.vue` vaate pealkirjaga „Minu profiil“.
- [ ] Vaates on kõik mockupi väljad (Eesnimi, Perenimi, E-post, Telefon, Tänava nimi, Majanumber, Linnaosa, Linn), lisaks „Korteri number (valikuline)“ ja nupp „Salvesta“.
- [ ] Väljad on mockupi järjekorras: Linnaosa rippmenüü on Linna rippmenüü kohal.
- [ ] Rippmenüüde esimene valik on „Vali linn“ ja „Vali linnaosa“.
- [ ] Profiiliga kasutajal (nt Liis Kask) on kõik väljad eeltäidetud, sh Linn = Tallinn ja Linnaosa = Mustamäe.
- [ ] Profiilita kasutajal on eesnimi, perenimi ja e-post eeltäidetud, ülejäänud väljad tühjad.
- [ ] Linna muutmisel laaditakse uue linna linnaosad ja eelmine linnaosa valik tühjendatakse; ilma linnata on linnaosa valik keelatud.
- [ ] Tühja kohustusliku välja korral näidatakse „Täida kõik kohustuslikud väljad“ ja päringut ei saadeta; korterinumbri võib tühjaks jätta.
- [ ] Salvestamisel saadetakse ainult kokkulepitud väljad (`cityId`, `userId`, `hasProfile` puuduvad), tühi korterinumber kujul `null`.
- [ ] Eduka salvestamise järel uueneb kasutajaolek (`hasProfile: true`) ja kasutaja suunatakse avalehele.
- [ ] `EMAIL_ALREADY_EXISTS`, `PRIMARY_KEY_NOT_FOUND`, `INCORRECT_INPUT` ja 500 korral näidatakse backendi `message` ning vorm jääb täidetuks.
- [ ] 401 korral tühjendatakse kasutajaolek ja suunatakse avalehele; võrguvea korral näidatakse üldist viga.
- [ ] Salvesta nupp on laadimise ja päringu ajal keelatud (topeltsaatmist ei toimu).
