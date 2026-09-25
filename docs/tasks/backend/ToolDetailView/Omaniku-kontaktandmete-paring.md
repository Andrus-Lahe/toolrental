# Omaniku kontaktandmete päring

**Teenus:** `GET /api/users/{userId}`

**Vaste balsamic mockupis:** ToolDetailView.vue (eraldi STEP-tähis puudub), lehekülg 4/17 failis [Laenukas2509.pdf](../../../balsamic/notes/Laenukas2509.pdf) (vt `Omaniku-kontaktandmete-paring.png`).

![Mockup](./Omaniku-kontaktandmete-paring.png)

Täiendavad allikad: [ToolDetailView märkmed](../../../balsamic/notes/ToolDetailView-markmed.md) ja [googlega_login.md](../../../balsamic/notes/googlega_login.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

PDF-i sinine märkus nimetab kontaktandmeid „avalikeks“. Kinnitatud silt muudab seda: omaniku kontaktid on nähtavad **ainult sisse logitud kasutajale** (kasutaja otsus).

## Sisend

| Parameeter | Asukoht | Java tüüp | Kohustuslik | Tähendus |
|---|---|---|---|---|
| `userId` | Path variable | `Integer` | Jah | Tööriista omaniku `app_user.id` (vastusest `GET /api/tools/{toolId}` → `ownerId`). |

Query parameetrid ja request body puuduvad. Näide: `GET /api/users/1`.

Päring nõuab sisselogimist. `/api/users/**` ei ole `SecurityConfig` avalike GET-teede hulgas (vt `googlega_login.md`), seega annab Spring Security külastajale 401. Iga sisse logitud kasutaja (customer, admin) võib vaadata iga kasutaja kontakte, mitte ainult vaadatava tööriista omaniku omi (kinnitatud). Ka blokeeritud kasutaja (`status = 'B'`) kontaktid tagastatakse, sest tema tööriistad on detailvaates endiselt nähtavad (kinnitatud).

Aadressid `/api/users/me/profile` (MyProfile) ja `/api/users/me/tools` (MyToolsView) on pikemad ja ei kattu selle teenusega. `GET /api/users/me` ei ole kokkulepitud aadress. Kasutaja ise saab oma andmed `GET /api/me` kaudu.

## Väljund

**Response (200 OK):** `UserDetailResponse`, kasutaja nimi ja kontaktandmed.

```json
{
  "userId": 1,
  "firstName": "Marko",
  "lastName": "Tamm",
  "email": "email@Gmail.com",
  "phone": "56565656"
}
```

Näide on sildilt ja ühtib failiga `3_import.sql` (`app_user.id = 1`, `profile.id = 1`).

| Väli | Java tüüp | Allikas | Selgitus |
|---|---|---|---|
| `userId` | `Integer` | `app_user.id` | |
| `firstName` | `String` | `app_user.first_name` | |
| `lastName` | `String` | `app_user.last_name` | |
| `email` | `String` | `profile.email` | `null`, kui kasutajal pole profiili |
| `phone` | `String` | `profile.phone` | `null`, kui kasutajal pole profiili |

Profiili puudumine ei ole viga: vastus on 200, `email` ja `phone` on `null`. `google_sub`, roll, status ja aadress (`location`) vastusesse ei lähe. Päring ei muuda andmeid.

## Eesmärk

Teenus täidab ToolDetailView vaate paremal oleva kasti „Omaniku kontaktinfo“ (nimi, e-post, telefon). Vaade kutsub seda ainult sisse logitud kasutajale, pärast `GET /api/tools/{toolId}` vastust, kasutades `ownerId` väärtust. Nii saab laenata sooviv kasutaja omanikuga ühendust võtta, kuid külastaja ei näe kontaktandmeid.

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

Profiil on valikuline: otsida `Optional` tüübiga.

Algandmed failist [3_import.sql](../../../database/3_import.sql):

| app_user.id | Nimi | profile.email | profile.phone |
|---|---|---|---|
| 1 | Marko Tamm | email@Gmail.com | 56565656 |
| 3 | Liis Kask | liis.kask@example.com | 55501002 |

`app_user.id = 2` impordiandmetes puudub (404 test). Profiilita kasutaja testiks tuleb luua `app_user` rida ilma profiilita. `location`, `tool` ja `role` ei osale päringus.

## Veaolukorrad

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Kasutajat `userId = 123` pole. Teates kasutada tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'userId' väärtusega: 123"}` |
| `userId` pole täisarv. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"userId: peab olema Integer-tüüpi täisarv"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist.
- Path variable'i 400 ja ühtne 500 kuju tuleb teostamisel tagada, sest praegune `RestExceptionHandler` neid automaatselt ei käsitle.

## Vastuvõtu kriteeriumid

- [ ] `GET /api/users/{userId}` on olemas ja nõuab sisselogimist; külastaja saab 401.
- [ ] Vastus sisaldab täpselt välju `userId`, `firstName`, `lastName`, `email`, `phone`.
- [ ] `userId = 1` annab näites toodud väärtused; `userId = 3` annab Liis Kase andmed.
- [ ] Profiilita kasutaja annab 200 ning `email` ja `phone` on `null`.
- [ ] Blokeeritud kasutaja (`status = 'B'`) kontaktid tagastatakse 200-ga.
- [ ] `google_sub`, roll, status ja aadress pole vastuses.
- [ ] Olematu `userId` (nt 2) annab 404 ja `PRIMARY_KEY_NOT_FOUND` teate päringu ID-ga; vigane `userId` annab 400.
- [ ] Päring ei muuda andmeid.
- [ ] Automaattestid katavad sisse logitud ligipääsu, 401, DTO kuju, profiilita kasutaja ning 400/404/500 juhtumid.
