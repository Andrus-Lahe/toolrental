# Profiili andmete päring

**Teenus:** `GET /api/users/me/profile`

**Vaste balsamic mockupis:** MyProfile.vue (eraldi STEP-tähis puudub), lehekülg 11/17 failis [Laenukas2509.pdf](../../../balsamic/notes/Laenukas2509.pdf) (vt `Profiili-andmete-paring.png`).

![Mockup](./Profiili-andmete-paring.png)

Täiendavad allikad: [MyProfile märkmed](../../../balsamic/notes/MyProfile-markmed.md) ja [googlega_login.md](../../../balsamic/notes/googlega_login.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

PDF-i sinine märkus kirjeldab vana lahendust (`RegisterView.vue`, `POST /api/users`). Kinnitatud silt asendab selle `googlega_login.md` lahendusega: `app_user` luuakse Google'iga sisselogimisel ja profiil täidetakse hiljem MyProfile vormis.

## Sisend

Teenusel puuduvad sisendid: path variable'id, query parameetrid ja request body puuduvad.

Päring nõuab sisselogimist. Kasutaja ID ja Google'i e-post võetakse sessioonist (`@AuthenticationPrincipal AppUserPrincipal`: `getUserId()`, `getEmail()`), mitte frontendist.

## Väljund

**Response (200 OK):** `ProfileDto`, sisse logitud kasutaja profiiliandmed vormi täitmiseks.

Näide profiiliga kasutajast Liis Kask (`userId = 3`):

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

Näide kasutajast, kes on esimest korda Google'iga sisse loginud ja kellel profiili veel pole. Nimi ja e-post on võetud PDF-i kollase märkuse näitest (Mari Maasikas, `user@gmail.com`, `user_id = 583`):

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

| Väli | Java tüüp | Allikas (profiiliga) | Profiilita |
|---|---|---|---|
| `userId` | `Integer` | `app_user.id` | sama |
| `firstName` | `String` | `app_user.first_name` | sama |
| `lastName` | `String` | `app_user.last_name` | sama (Google'i puuduva perenime korral `""`) |
| `email` | `String` | `profile.email` | Google'i e-post sessioonist (`principal.getEmail()`) |
| `phone` | `String` | `profile.phone` | `null` |
| `cityId` | `Integer` | `district.city_id` (`profile.location_id` → `location.district_id`) | `null` |
| `districtId` | `Integer` | `location.district_id` | `null` |
| `streetName` | `String` | `location.street_name` | `null` |
| `houseNumber` | `String` | `location.house_number` | `null` |
| `apartmentNumber` | `String` | `location.apartment_number`, võib olla `null` | `null` |
| `hasProfile` | `Boolean` | `true` | `false` |

Profiili puudumine ei ole viga: vastus on alati 200 (kasutaja otsus). `cityId` on vajalik, et frontend saaks linna valida ja laadida `GET /api/cities/{cityId}/districts` abil linnaosad. `location.lng`, `location.lat`, `google_sub`, `role` ja `status` vastusesse ei lähe. Päring ei muuda andmeid.

## Eesmärk

Teenus täidab MyProfile vaate (`/profile`) vormi. Vaade avaneb menüü lingist „Profiil“ või automaatselt pärast esimest Google'iga sisselogimist, kui `GET /api/me` vastab `hasProfile: false`. Esmasel täitmisel on nimi ja e-post Google'i andmetega eeltäidetud, hilisemal muutmisel näeb kasutaja oma salvestatud andmeid. Salvestamine käib teenusega `PUT /api/users/me/profile` (vt [Profiili salvestamine](./Profiili-salvestamine.md)).

## Seotud andmebaasi tabelid

Vt [2_create.sql](../../../database/2_create.sql).

### `app_user`

```sql
CREATE TABLE app_user (
    id serial PRIMARY KEY,
    role_id integer NOT NULL REFERENCES role (id),
    first_name varchar(100) NOT NULL,
    last_name varchar(100) NOT NULL,
    google_sub varchar(255) NOT NULL UNIQUE,
    status char(1) NOT NULL DEFAULT 'A' CHECK (status IN ('A', 'B'))
);
```

### `profile`

```sql
CREATE TABLE profile (
    id serial PRIMARY KEY,
    user_id integer NOT NULL UNIQUE REFERENCES app_user (id),
    location_id integer NOT NULL REFERENCES location (id),
    email varchar(254) NOT NULL UNIQUE,
    phone varchar(32) NOT NULL,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp
);
```

Kasutajal on kuni üks profiil (`user_id UNIQUE`). Profiili otsida `Optional` tüübiga.

### `location`

```sql
CREATE TABLE location (
    id serial PRIMARY KEY,
    district_id integer NOT NULL REFERENCES district (id),
    street_name varchar(150) NOT NULL,
    house_number varchar(20) NOT NULL,
    apartment_number varchar(20),
    lng decimal(10, 7),
    lat decimal(10, 7)
);
```

### `district`

```sql
CREATE TABLE district (
    id serial PRIMARY KEY,
    city_id integer NOT NULL REFERENCES city (id),
    district_name varchar(100) NOT NULL,
    CONSTRAINT district_city_name_unique UNIQUE (city_id, district_name)
);
```

Kasutatakse ainult `city_id` leidmiseks. Linna ja linnaosa nimesid vastusesse ei panda: need tulevad rippmenüüde päringutest ([Linnade nimekirja päring](../ToolsView/Linnade-nimekirja-paring.md), [Valitud linna linnaosade päring](../ToolsView/Valitud-linna-linnaosade-paring.md)).

Algandmed failist [3_import.sql](../../../database/3_import.sql):

| app_user.id | Nimi | profile.email | profile.phone | location | Linn / linnaosa |
|---|---|---|---|---|---|
| 1 | Marko Tamm | email@Gmail.com | 56565656 | 1: Teddre 28, korter `NULL` | Tallinn (1) / Kristiine (1) |
| 3 | Liis Kask | liis.kask@example.com | 55501002 | 3: Sõpruse pst 120-8 | Tallinn (1) / Mustamäe (2) |

Impordiandmetes pole profiilita kasutajat. `hasProfile: false` testiks tuleb luua `app_user` rida ilma profiilita.

## Veaolukorrad

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

Profiili puudumine ei ole veaolukord (200, `hasProfile: false`). Ühtne 500 kuju tuleb teostamisel tagada, sest praegune `RestExceptionHandler` seda ei käsitle.

## Vastuvõtu kriteeriumid

- [ ] `GET /api/users/me/profile` on olemas ja nõuab sisselogimist.
- [ ] Vastus sisaldab täpselt välju `userId`, `firstName`, `lastName`, `email`, `phone`, `cityId`, `districtId`, `streetName`, `houseNumber`, `apartmentNumber`, `hasProfile`.
- [ ] Liis Kask (`userId = 3`) saab näites toodud väärtused, sh `cityId = 1` ja `apartmentNumber = "8"`.
- [ ] Marko Tamm (`userId = 1`) saab `apartmentNumber = null` ja `districtId = 1`.
- [ ] Profiilita kasutaja saab 200, `hasProfile = false`, nime `app_user` tabelist, e-posti sessioonist ja ülejäänud väljad `null`.
- [ ] Kasutaja näeb ainult enda andmeid; `userId` pole päringus.
- [ ] `google_sub`, `lng`, `lat`, roll ja status pole vastuses.
- [ ] Sisse logimata kasutaja saab 401; andmebaasi tõrge annab kirjeldatud 500 vastuse.
- [ ] Automaattestid katavad profiiliga kasutaja, profiilita kasutaja, `null` korterinumbri, 401 ja 500 juhtumi.
