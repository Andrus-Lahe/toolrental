# MyProfile.vue märkmed

Allikas: `Laenukas2509.pdf`, lehekülg 11 (MyProfile.vue).

Staatus: kinnitatud 2026-09-25 (profiilita kasutajal GET vastab 200 ja hasProfile: false; pärast salvestamist suunatakse alati avalehele; PDF-i sinine märkus asendatakse Balsamiqus selle sildi tekstiga). Backendis pole veel profiili `controller`/`service` klasse. Silt järgib [googlega_login.md](googlega_login.md) lahendust (kasutaja otsus).

## Erinevused PDF-i olemasolevast märkusest

- PDF-i sinine märkus kirjeldab eraldi vaadet `RegisterView.vue` (`/register`) ja päringut `POST /api/users`, kus `googleSub` saadetakse request body's ning `app_user` ja `profile` luuakse korraga. Silt **ei kasuta seda lahendust**:
  - `googlega_login.md` järgi luuakse `app_user` juba esimesel Google'iga sisselogimisel (roll `customer`, status `A`). Profiili (`profile` + `location`) ei saa siis luua, sest `phone` ja `location_id` on NOT NULL.
  - Kasutaja ID ja `google_sub` ei tohi tulla frontendist, vaid ainult sessioonist. Muidu võiks keegi saata võõra `googleSub` väärtuse.
  - Seega on `MyProfile.vue` üks vorm kahe olukorra jaoks: profiili esmane täitmine (`hasProfile: false`) ja hilisem muutmine.
- Viga `GOOGLE_ACCOUNT_ALREADY_REGISTERED` kaob, sest kasutajat ei looda vormi kaudu. PDF-i vead `EMAIL_ALREADY_EXISTS`, `PRIMARY_KEY_NOT_FOUND` (`districtId`) ja `INCORRECT_INPUT` jäävad.
- Vormile lisatakse väli **„Korteri number (valikuline)“** Majanumbri alla (kasutaja otsus), sest andmebaasis on `location.apartment_number`.
- PDF-i `GET /api/cities` ja `GET /api/cities/{cityId}/districts` märkused on samad mis [ToolsView-markmed.md](ToolsView-markmed.md) failis. Allpool on need korratud, et iga vaate juures oleks iga kutse kohta oma kast.
- Näide kasutab faili `3_import.sql` kasutajat Liis Kask (`userId = 3`, `location.id = 3`, Tallinn / Mustamäe).

## Vaate märkmed

```text
Roll: Customer / Admin (Google'iga sisse logitud kasutaja)
Failinimi: MyProfile.vue
Frontend rada: /profile

Vaatega seotud lisainfo:
Vaade avaneb menüü lingist "Profiil" või automaatselt pärast Google'iga sisselogimist, kui GET /api/me vastab hasProfile: false. Avamisel laaditakse GET /api/users/me/profile abil vormi andmed ja GET /api/cities abil linnad; kui cityId on olemas, laaditakse ka GET /api/cities/{cityId}/districts. Esmasel täitmisel on eesnimi, perenimi ja e-post Google'i andmetega eeltäidetud.

Linna valimisel laaditakse linnaosad uuesti ja linnaosa valik tühjendatakse. Enne salvestamist kontrollitakse, et kõik väljad peale "Korteri number" on täidetud; kui mitte, kuvatakse AlertDanger.vue teade "Täida kõik kohustuslikud väljad".

Nupp "Salvesta" saadab PUT /api/users/me/profile. Eduka vastuse järel suunatakse kasutaja avalehele (/). Backendi veateade (message) kuvatakse AlertDanger.vue komponendiga.
```

## API märkmed — GET /api/users/me/profile

```text
API: GET /api/users/me/profile

ProfileDto.java
Response (200):
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

API teenuse lisainfo:
Kasutaja tuvastatakse sessioonist. Kui profiili veel pole, vastatakse samuti 200: hasProfile = false, firstName ja lastName tulevad app_user tabelist, email Google'i sessioonist ning phone, cityId, districtId, streetName, houseNumber ja apartmentNumber on null. apartmentNumber võib ka profiiliga kasutajal olla null.

Veateated: —
```

## API märkmed — PUT /api/users/me/profile

```text
API: PUT /api/users/me/profile

ProfileUpdateRequestDto.java
Request body:
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

Response (200): NONE

API teenuse lisainfo:
Kasutaja tuvastatakse sessioonist; userId ja googleSub päringus puuduvad. Kui profiili pole, luuakse ühes transaktsioonis location ja profile rida; kui on, uuendatakse app_user nimed, profile (email, phone, updated_at) ja kasutaja location rida. apartmentNumber on valikuline (null); cityId ei saadeta, sest linn tuleneb districtId-st. Roll ja status ei muutu.

Veateated:
HTTP: 403
errorCode: EMAIL_ALREADY_EXISTS
message: "Sellise e-postiga kasutaja on juba süsteemis olemas"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'districtId' väärtusega: 99"

HTTP: 400
errorCode: INCORRECT_INPUT
message: "firstName: ei tohi olla tühi"
```

## API märkmed — GET /api/cities

```text
API: GET /api/cities

CityDto.java
Response (200):
[
  {
    "cityId": 1,
    "cityName": "Tallinn"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõigi linnade nimekirja linna rippmenüü täitmiseks.

Veateated: —
```

## API märkmed — GET /api/cities/{cityId}/districts

```text
API: GET /api/cities/{cityId}/districts

DistrictDto.java
Response (200):
[
  {
    "districtId": 1,
    "districtName": "Kristiine"
  },
  ...
]

API teenuse lisainfo:
Tagastab ainult valitud linna (cityId) linnaosad linnaosa rippmenüü täitmiseks.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'cityId' väärtusega: 99"
```

## Veakäsitluse seos olemasoleva projektiga

- `PRIMARY_KEY_NOT_FOUND` tuleb olemasolevast `PrimaryKeyNotFoundException` klassist (404).
- `EMAIL_ALREADY_EXISTS` on PDF-i märkusest pärit uus kood. Seda visatakse olemasoleva `ForbiddenException` klassiga (403), kui sama e-post on **teise** kasutaja profiilil (`profile.email` on UNIQUE). Oma senise e-posti uuesti salvestamine on lubatud.
- `INCORRECT_INPUT` tuleb `@Valid` + `@NotBlank` kaudu olemasolevast `handleMethodArgumentNotValid` handlerist; teade `"firstName: ei tohi olla tühi"` eeldab eestikeelset `message` väärtust annotatsioonis.
- Sisse logimata kasutajale vastab Spring Security 401 ilma ApiError body'ta (vt `googlega_login.md`).
