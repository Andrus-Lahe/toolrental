# AdminView.vue märkmed

Allikas: `Laenukas.pdf`, lehekülg 13 (AdminView.vue).

Staatus: ettepanek. Backendis pole veel `controller`/`service` klasse. OpenAPI spetsifikatsiooni ega Jira taski selle vaate kohta ei ole. API aadressid järgivad `googlega_login.md` kokkulepet, et admini API-d on `/api/admin/**` all ja nõuavad rolli `admin`. Kategooria nimi ja kasutaja profiiliandmed on kontrollitud failis `3_import.sql`.

## Mockupi tabelite seos andmetega

Mockupi tabelid (veerud, nupud ja tekstid) jäävad muutmata. Andmebaasi tabeleid (`2_create.sql`) ei muudeta.

- Kasutaja nupp **„Kustuta“** jääb alles ja kustutab kasutaja (DELETE /api/admin/users/{userId}). Kinnitatud: kui kasutajal on tööriistu või broneeringuid, siis kustutamist ei lubata; sellisel juhul saab kasutaja ainult blokeerida. Koos kasutajaga kustub tema profiil.
- Nupp **„Lisa kasutaja“** eemaldatakse vaatest. Seda silt ei kirjelda.
- Veerg „Olek“: `app_user.status = 'A'` kuvatakse kui „Aktiivne“ ja `'B'` kui „Mitteaktiivne“. Nupp „Blokeeri“ määrab kasutaja olekuks `B`.
- „Registreeritud“ veerg tuleb väljalt `profile.created_at`. Profiilita kasutajal on `email` ja `registeredAt` väärtus `null`.
- Mockupil on kategooria nimi „Muud asjad“, kuid `3_import.sql` failis on see „Muud“. JSON näidised kasutavad andmebaasi väärtust.
- „Lisa kategooria“ ja „Muuda“ avavad samal lehel modaalakna väljadega nimi, kirjeldus ja järjekord. Kategooria pildi (`category_image`) haldust selles versioonis ei ole (kinnitatud).

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminView.vue
Frontend rada: /admin

Vaatega seotud lisainfo:
Menüü link "Haldus" on nähtav ainult adminile. Vaate avamisel laaditakse kasutajad (GET /api/admin/users) ja kategooriad (GET /api/admin/categories).

Nupp "Blokeeri" saadab PATCH /api/admin/users/{userId}/status. Kasutaja "Kustuta" küsib kinnitust ja saadab DELETE /api/admin/users/{userId}. Pärast mõlemat tegevust laaditakse kasutajate tabel uuesti.

"Lisa kategooria" ja "Muuda" avavad modaalakna (nimi, kirjeldus, järjekord); "Salvesta" saadab POST või PUT päringu. "Kustuta" küsib kinnitust ja saadab DELETE päringu. Backendi veateade (message) kuvatakse AlertDanger.vue komponendiga.
```

## API märkmed — GET /api/admin/users

```text
API: GET /api/admin/users

AdminUserDto.java
Response (200):
[
  {
    "userId": 1,
    "firstName": "Marko",
    "lastName": "Tamm",
    "email": "email@Gmail.com",
    "roleName": "admin",
    "registeredAt": "2026-09-18",
    "status": "A"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõik kasutajad (sh blokeeritud). email ja registeredAt tulevad profile tabelist (created_at) ja on null, kui kasutajal profiili pole. status: A = Aktiivne, B = Mitteaktiivne (blokeeritud).
Sisse logimata kasutajale vastab Spring Security koodiga 401, mitte-adminile koodiga 403, mõlemal juhul ilma ApiError body'ta.

Veateated: —
```

## API märkmed — PATCH /api/admin/users/{userId}/status

```text
API: PATCH /api/admin/users/{userId}/status

UserStatusRequestDto.java
Request body:
{
  "status": "B"
}

Response (200): NONE

API teenuse lisainfo:
Nupp "Blokeeri" saadab status = "B"; vaates kuvatakse olek siis "Mitteaktiivne". Blokeeritud kasutaja ei saa sisse logida.

Veateated:
HTTP: 403
errorCode: SELF_BLOCK_NOT_ALLOWED
message: "Iseennast ei saa blokeerida"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'userId' väärtusega: 123"
```

## API märkmed — DELETE /api/admin/users/{userId}

```text
API: DELETE /api/admin/users/{userId}

Response (200): NONE

API teenuse lisainfo:
Koos kasutajaga kustutatakse tema profiil. Kasutajat, kellel on tööriistu (tool.owner_id) või broneeringuid (booking.renter_id), kustutada ei saa; sel juhul tuleb ta blokeerida.

Veateated:
HTTP: 403
errorCode: SELF_DELETE_NOT_ALLOWED
message: "Iseennast ei saa kustutada"

HTTP: 403
errorCode: USER_HAS_DATA
message: "Kasutajat ei saa kustutada, sest tal on tööriistu või broneeringuid"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'userId' väärtusega: 123"
```

## API märkmed — GET /api/admin/categories

```text
API: GET /api/admin/categories

AdminCategoryDto.java
Response (200):
[
  {
    "categoryId": 1,
    "categoryName": "Aiatööd",
    "description": "Muruniidukid, labidad, rehad",
    "sequence": 100
  },
  ...
]

API teenuse lisainfo:
Erinevalt avalikust GET /api/categories päringust tagastab see ka description ja sequence väljad, mida muutmise modaalaken vajab. Kategooriad on järjestatud sequence järgi kasvavalt.

Veateated: —
```

## API märkmed — POST /api/admin/categories

```text
API: POST /api/admin/categories

CategoryRequestDto.java
Request body:
{
  "categoryName": "Aiatööd",
  "description": "Muruniidukid, labidad, rehad",
  "sequence": 100
}

Response (200): NONE

API teenuse lisainfo:
categoryName ja sequence on kohustuslikud, description võib olla null. Kategooria nimi peab olema unikaalne.

Veateated:
HTTP: 403
errorCode: CATEGORY_UNAVAILABLE
message: "Sellise nimega kategooria on juba olemas"
```

## API märkmed — PUT /api/admin/categories/{categoryId}

```text
API: PUT /api/admin/categories/{categoryId}

CategoryRequestDto.java
Request body:
{
  "categoryName": "Aiatööd",
  "description": "Muruniidukid, labidad, rehad",
  "sequence": 100
}

Response (200): NONE

API teenuse lisainfo:
Uus nimi ei tohi kattuda mõne teise kategooria nimega. Sama nime uuesti salvestamine on lubatud.

Veateated:
HTTP: 403
errorCode: CATEGORY_UNAVAILABLE
message: "Sellise nimega kategooria on juba olemas"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'categoryId' väärtusega: 123"
```

## API märkmed — DELETE /api/admin/categories/{categoryId}

```text
API: DELETE /api/admin/categories/{categoryId}

Response (200): NONE

API teenuse lisainfo:
Kategooriat, millel on tööriistu, kustutada ei saa. Koos kategooriaga kustutatakse ka selle pilt (category_image).

Veateated:
HTTP: 403
errorCode: CATEGORY_IN_USE
message: "Kategooriat ei saa kustutada, sest sellel on tööriistu"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'categoryId' väärtusega: 123"
```

## Veakäsitluse seos olemasoleva projektiga

- `PRIMARY_KEY_NOT_FOUND` tuleb olemasolevast `PrimaryKeyNotFoundException` klassist. `RestExceptionHandler` teisendab selle koodiks 404.
- `SELF_BLOCK_NOT_ALLOWED`, `SELF_DELETE_NOT_ALLOWED`, `USER_HAS_DATA`, `CATEGORY_UNAVAILABLE` ja `CATEGORY_IN_USE` on uued pakutavad koodid. Neid saab visata olemasoleva `ForbiddenException` klassiga, mis annab koodi 403.
- Kui `@Valid` kontroll lisatakse, annab valideerimisviga koodi 400 ja `errorCode: INCORRECT_INPUT`. Siis on `message` kujul `<väli>: <põhjus>`.
