# Kategooria kustutamine

**Teenus:** `DELETE /api/admin/categories/{categoryId}`

**Vaste balsamic mockupis:** AdminView.vue (eraldi STEP-tähis puudub), lehekülg 13/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Kategooria-kustutamine.png`).

![Mockup](./Kategooria-kustutamine.png)

Täiendav allikas: [AdminView märkmed](../../../balsamic/notes/AdminView-markmed.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

## Sisend

| Parameeter | Asukoht | Java tüüp | Kohustuslik | Tähendus |
|---|---|---|---|---|
| `categoryId` | Path variable | `Integer` | Jah | Kustutatava kategooria `category.id`. |

Query parameetrid ja request body puuduvad. Näide: `DELETE /api/admin/categories/4`.

Teenus on ainult adminile (`/api/admin/**`, `hasRole("admin")`).

## Väljund

**Response (200 OK):** tühi body (`Response (200): NONE`).

Kategooria kustutatakse füüsiliselt ühes transaktsioonis (`@Transactional`):

1. Kustutatakse kategooria pilt (`category_image`), kui see on olemas.
2. Kustutatakse `category` rida.

Kustutamine on lubatud ainult siis, kui kategoorias pole ühtegi tööriista (`tool.category_id`). Tööriistade staatust (`A`/`U`) ei arvestata: iga seotud tööriist takistab kustutamist.

## Eesmärk

Teenust kasutab AdminView tabeli „Kategooriad“ nupp „Kustuta“. Admin saab eemaldada kasutamata kategooria. Vaade küsib enne päringut kinnitust ja laadib pärast edukat vastust kategooriate tabeli uuesti (`GET /api/admin/categories`). Kustutatud kategooria kaob ka avalikust kategooriate loendist.

## Seotud andmebaasi tabelid

Vt [2_create.sql](../../../database/2_create.sql).

### `category`

```sql
CREATE TABLE category (
    id serial PRIMARY KEY,
    category_name varchar(100) NOT NULL UNIQUE,
    description varchar(255),
    sequence integer NOT NULL
);
```

### `category_image`

```sql
CREATE TABLE category_image (
    id serial PRIMARY KEY,
    category_id integer NOT NULL UNIQUE REFERENCES category (id),
    image_data bytea NOT NULL
);
```

`category_image.category_id` välisvõtmel pole `ON DELETE CASCADE`, seega tuleb pilt kustutada enne kategooriat.

### `tool` (ainult kontroll)

```sql
CREATE TABLE tool (
    id serial PRIMARY KEY,
    owner_id integer NOT NULL REFERENCES app_user (id),
    category_id integer NOT NULL REFERENCES category (id),
    ...
);
```

Tabelit `tool` ei muudeta. Teenus kontrollib sellest ainult, kas kategoorias on tööriistu.

Algandmed failist [3_import.sql](../../../database/3_import.sql):

| category.id | category_name | Tööriistad (tool.id) | Pilt | Kustutamise tulemus |
|---|---|---|---|---|
| 1 | Aiatööd | 4, 5 | on | 403 `CATEGORY_IN_USE` |
| 2 | Ehitustööd | 1, 2 | on | 403 `CATEGORY_IN_USE` |
| 3 | Koristamine | 3, 6 | on | 403 `CATEGORY_IN_USE` |
| 4 | Muud | 7, 8 | on | 403 `CATEGORY_IN_USE` |

Impordiandmetes pole ühtegi kustutatavat kategooriat. Eduka kustutamise testiks tuleb luua testikategooria (nt `POST /api/admin/categories` kaudu, soovi korral koos `category_image` reaga).

## Veaolukorrad

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Sisse logitud kasutaja roll pole `admin`. | 403 Forbidden | tühi (Spring Security) |
| Kategooriat `categoryId = 123` pole. Teates kasutada tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'categoryId' väärtusega: 123"}` |
| Kategoorias on vähemalt üks tööriist. | 403 Forbidden | `{"errorCode":"CATEGORY_IN_USE","message":"Kategooriat ei saa kustutada, sest sellel on tööriistu"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kategooria kustutamine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist.
- `CATEGORY_IN_USE` on uus kood. Seda visatakse olemasoleva `ForbiddenException` klassiga.
- Kontrollide järjekord: 404, siis `CATEGORY_IN_USE`. Vea korral ei kustutata midagi.

## Vastuvõtu kriteeriumid

- [ ] `DELETE /api/admin/categories/{categoryId}` on olemas ja kättesaadav ainult `admin` rollile.
- [ ] Tööriistadeta kategooria kustutamine annab 200 tühja body'ga ning `category` ja `category_image` read on kustutatud.
- [ ] Pildita kategooria kustutamine õnnestub samuti.
- [ ] Impordikategooria (nt `categoryId = 4`) annab 403 `CATEGORY_IN_USE` ja andmed jäävad alles.
- [ ] Olematu `categoryId` annab 404 ja `PRIMARY_KEY_NOT_FOUND` teate päringu ID-ga.
- [ ] Pildi ja kategooria kustutamine toimub ühes transaktsioonis.
- [ ] Kustutatud kategooria puudub `GET /api/admin/categories` ja `GET /api/categories` vastusest.
- [ ] Sisse logimata kasutaja saab 401 ja mitte-admin 403.
- [ ] Automaattestid katavad eduka kustutamise (pildiga ja ilma), `CATEGORY_IN_USE`, 404, 500 juhtumi ja rollipõhise ligipääsu.
