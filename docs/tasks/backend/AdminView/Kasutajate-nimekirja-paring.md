# Kasutajate nimekirja päring

**Teenus:** `GET /api/admin/users`

**Vaste balsamic mockupis:** AdminView.vue (eraldi STEP-tähis puudub), lehekülg 13/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Kasutajate-nimekirja-paring.png`).

![Mockup](./Kasutajate-nimekirja-paring.png)

Täiendav allikas: [AdminView märkmed](../../../balsamic/notes/AdminView-markmed.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

## Sisend

Teenusel puuduvad sisendid: path variable'id, query parameetrid ja request body puuduvad.

Teenus on ainult adminile. Kokkuleppe järgi (vt [googlega_login.md](../../../balsamic/notes/googlega_login.md)) kaitseb Spring Security kõiki `/api/admin/**` aadresse reegliga `hasRole("admin")`.

## Väljund

**Response (200 OK):** `List<AdminUserDto>`, kõik süsteemi kasutajad, sh blokeeritud. Näide impordiandmete põhjal:

```json
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
  {
    "userId": 3,
    "firstName": "Liis",
    "lastName": "Kask",
    "email": "liis.kask@example.com",
    "roleName": "customer",
    "registeredAt": "2026-09-18",
    "status": "A"
  }
]
```

| Väli | Java tüüp | Allikas | Selgitus |
|---|---|---|---|
| `userId` | `Integer` | `app_user.id` | |
| `firstName` | `String` | `app_user.first_name` | |
| `lastName` | `String` | `app_user.last_name` | |
| `email` | `String` | `profile.email` | `null`, kui kasutajal pole profiili |
| `roleName` | `String` | `role.role_name` | `admin` või `customer`; mockupis kuvatakse `customer` rolli tekstina „kasutaja“ |
| `registeredAt` | `LocalDate` | `profile.created_at` kuupäevaosa | `null`, kui kasutajal pole profiili |
| `status` | `String` | `app_user.status` | `A` = Aktiivne, `B` = Mitteaktiivne (blokeeritud) |

Mockupi näiteread (Liis Kask `liis@kask.ee`, Malle Haab) on visuaalsed kohatäitjad. JSON näide kasutab faili [3_import.sql](../../../database/3_import.sql) andmeid.

Profiil on valikuline, seega tuleb `profile` tabel liita LEFT JOIN-iga: ka profiilita kasutaja peab loendis olema. Järjestus on `app_user.id ASC` (taski tehniline täpsustus, mockup järjestust ei määra). Lehekülgjaotust ei ole.

## Eesmärk

Teenus täidab AdminView lehe tabeli „Kasutajad“ (veerud Nimi, E-post, Roll, Registreeritud, Olek). Admin näeb kõiki kasutajaid ja nende olekut ning saab tabelist kasutajat blokeerida (`PATCH /api/admin/users/{userId}/status`) või kustutada (`DELETE /api/admin/users/{userId}`). Pärast neid tegevusi kutsub vaade teenust uuesti. Päring ei muuda andmeid.

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

`google_sub` vastusesse ei lähe.

### `role`

```sql
CREATE TABLE role (
    id serial PRIMARY KEY,
    role_name varchar(20) NOT NULL UNIQUE
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

Algandmed failist [3_import.sql](../../../database/3_import.sql):

| app_user.id | Nimi | role | status | profile.email | profile.created_at |
|---|---|---|---|---|---|
| 1 | Marko Tamm | admin (1) | A | email@Gmail.com | 2026-09-18 10:00:00 |
| 3 | Liis Kask | customer (2) | A | liis.kask@example.com | 2026-09-18 10:30:00 |

`location`, `tool` ja `booking` ei osale päringus.

## Veaolukorrad

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`). 401 ja 403 annab Spring Security enne controllerit, seega nende body on tühi.

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi |
| Sisse logitud kasutaja roll pole `admin`. | 403 Forbidden | tühi |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kasutajate laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

Ühtne 500 kuju tuleb teostamisel tagada, sest praegune `RestExceptionHandler` seda ei käsitle. SQL-i ega stack trace'i ei tagastata.

## Vastuvõtu kriteeriumid

- [ ] `GET /api/admin/users` on olemas ja kättesaadav ainult `admin` rollile.
- [ ] HTTP 200 vastus on massiiv täpselt väljadega `userId`, `firstName`, `lastName`, `email`, `roleName`, `registeredAt`, `status`.
- [ ] Impordiandmetega tagastatakse 2 kasutajat (id 1 ja 3) järjestuses `app_user.id ASC` ja näites toodud väärtustega.
- [ ] Blokeeritud (`status = 'B'`) kasutaja on samuti loendis.
- [ ] Profiilita kasutaja on loendis ning tema `email` ja `registeredAt` on `null`.
- [ ] `registeredAt` on kuupäev kujul `yyyy-MM-dd`; `google_sub` pole vastuses.
- [ ] Sisse logimata kasutaja saab 401 ja mitte-admin 403.
- [ ] Andmebaasi tõrge annab kirjeldatud 500 vastuse.
- [ ] Automaattestid katavad DTO kuju, järjestuse, profiilita ja blokeeritud kasutaja, rollipõhise ligipääsu ning 500 juhtumi.
