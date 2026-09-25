# Kasutaja kustutamine

**Teenus:** `DELETE /api/admin/users/{userId}`

**Vaste balsamic mockupis:** AdminView.vue (eraldi STEP-tähis puudub), lehekülg 13/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Kasutaja-kustutamine.png`).

![Mockup](./Kasutaja-kustutamine.png)

Täiendav allikas: [AdminView märkmed](../../../balsamic/notes/AdminView-markmed.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

## Sisend

| Parameeter | Asukoht | Java tüüp | Kohustuslik | Tähendus |
|---|---|---|---|---|
| `userId` | Path variable | `Integer` | Jah | Kustutatava kasutaja `app_user.id`. |

Query parameetrid ja request body puuduvad. Näide: `DELETE /api/admin/users/3`.

Teenus on ainult adminile (`/api/admin/**`, `hasRole("admin")`).

## Väljund

**Response (200 OK):** tühi body (`Response (200): NONE`).

Kasutaja kustutatakse füüsiliselt ühes transaktsioonis (`@Transactional`):

1. Kustutatakse kasutaja `profile` rida, kui see on olemas.
2. Kustutatakse `app_user` rida.

Kustutamine on lubatud ainult siis, kui kasutajal pole ühtegi tööriista (`tool.owner_id`) ega broneeringut (`booking.renter_id`). Muul juhul tuleb kasutaja blokeerida (`PATCH /api/admin/users/{userId}/status`). Nii jäävad `tool` ja `booking` välisvõtmed terveks ja teiste kasutajate broneeringute ajalugu ei kao.

Profiili `location` rida jääb alles. AdminView märkmed selle kustutamist ei nõua.

## Eesmärk

Teenust kasutab AdminView tabeli „Kasutajad“ nupp „Kustuta“. Admin saab eemaldada kasutaja, kes pole süsteemi kasutama hakanud (tööriistu ega broneeringuid pole). Vaade küsib enne päringut kinnitust ja laadib pärast edukat vastust kasutajate tabeli uuesti (`GET /api/admin/users`).

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

`profile.user_id` välisvõtmel pole `ON DELETE CASCADE`, seega tuleb profiil kustutada enne kasutajat.

### `tool` ja `booking` (ainult kontroll)

```sql
CREATE TABLE tool (
    id serial PRIMARY KEY,
    owner_id integer NOT NULL REFERENCES app_user (id),
    ...
);

CREATE TABLE booking (
    id serial PRIMARY KEY,
    tool_id integer NOT NULL REFERENCES tool (id),
    renter_id integer NOT NULL REFERENCES app_user (id),
    ...
);
```

Neid tabeleid ei muudeta. Teenus kontrollib nendest ainult, kas kasutajal on seotud ridu.

Algandmed failist [3_import.sql](../../../database/3_import.sql):

| app_user.id | Nimi | Tööriistu (owner_id) | Broneeringuid (renter_id) | Kustutamise tulemus |
|---|---|---|---|---|
| 1 | Marko Tamm | 5 (id 1, 2, 5, 6, 7) | 1 (id 3) | 403 `USER_HAS_DATA` (ja admin ise: `SELF_DELETE_NOT_ALLOWED`) |
| 3 | Liis Kask | 3 (id 3, 4, 8) | 2 (id 1, 2) | 403 `USER_HAS_DATA` |

Impordiandmetes pole ühtegi kustutatavat kasutajat. Eduka kustutamise testiks tuleb luua testikasutaja (soovi korral koos profiiliga), kellel pole tööriistu ega broneeringuid.

## Veaolukorrad

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Sisse logitud kasutaja roll pole `admin`. | 403 Forbidden | tühi (Spring Security) |
| Kasutajat `userId = 123` pole. Teates kasutada tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'userId' väärtusega: 123"}` |
| Admin üritab kustutada iseennast. | 403 Forbidden | `{"errorCode":"SELF_DELETE_NOT_ALLOWED","message":"Iseennast ei saa kustutada"}` |
| Kasutajal on vähemalt üks tööriist või broneering. | 403 Forbidden | `{"errorCode":"USER_HAS_DATA","message":"Kasutajat ei saa kustutada, sest tal on tööriistu või broneeringuid"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kasutaja kustutamine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist.
- `SELF_DELETE_NOT_ALLOWED` ja `USER_HAS_DATA` on uued koodid. Neid visatakse olemasoleva `ForbiddenException` klassiga.
- Kontrollide järjekord: 404, `SELF_DELETE_NOT_ALLOWED`, siis `USER_HAS_DATA`. Vea korral ei kustutata midagi.

## Vastuvõtu kriteeriumid

- [ ] `DELETE /api/admin/users/{userId}` on olemas ja kättesaadav ainult `admin` rollile.
- [ ] Tööriistade ja broneeringuteta kasutaja kustutamine annab 200 tühja body'ga ning kasutaja `app_user` ja `profile` read on kustutatud.
- [ ] Profiilita kasutaja kustutamine õnnestub samuti.
- [ ] Impordikasutaja `userId = 3` annab 403 `USER_HAS_DATA` ja andmed jäävad alles.
- [ ] Admin ei saa iseennast kustutada: vastus 403 `SELF_DELETE_NOT_ALLOWED`.
- [ ] Olematu `userId` annab 404 ja `PRIMARY_KEY_NOT_FOUND` teate päringu ID-ga.
- [ ] Profiili ja kasutaja kustutamine toimub ühes transaktsioonis; vea korral ei jää osaliselt kustutatud andmeid.
- [ ] Sisse logimata kasutaja saab 401 ja mitte-admin 403.
- [ ] Automaattestid katavad eduka kustutamise (profiiliga ja ilma), `USER_HAS_DATA` (tööriista ja broneeringu korral eraldi), `SELF_DELETE_NOT_ALLOWED`, 404, 500 ja rollipõhise ligipääsu.
