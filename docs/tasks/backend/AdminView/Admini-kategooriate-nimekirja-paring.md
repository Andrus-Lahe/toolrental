# Admini kategooriate nimekirja päring

**Teenus:** `GET /api/admin/categories`

**Vaste balsamic mockupis:** AdminView.vue (eraldi STEP-tähis puudub), lehekülg 13/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Admini-kategooriate-nimekirja-paring.png`).

![Mockup](./Admini-kategooriate-nimekirja-paring.png)

Täiendav allikas: [AdminView märkmed](../../../balsamic/notes/AdminView-markmed.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

## Sisend

Teenusel puuduvad sisendid: path variable'id, query parameetrid ja request body puuduvad.

Teenus on ainult adminile (`/api/admin/**`, `hasRole("admin")`).

## Väljund

**Response (200 OK):** `List<AdminCategoryDto>`, kõik kategooriad. Näide impordiandmete põhjal:

```json
[
  {
    "categoryId": 1,
    "categoryName": "Aiatööd",
    "description": "Muruniidukid, labidad, rehad",
    "sequence": 100
  },
  {
    "categoryId": 2,
    "categoryName": "Ehitustööd",
    "description": "Trellid, ketassaed, redelid",
    "sequence": 200
  },
  {
    "categoryId": 3,
    "categoryName": "Koristamine",
    "description": "Tekstiilipesurid, aknapesurid, aurupesurid",
    "sequence": 300
  },
  {
    "categoryId": 4,
    "categoryName": "Muud",
    "description": "Lumelabidad, naabrimehed, naabrinaised",
    "sequence": 10000
  }
]
```

| Väli | Java tüüp | Allikas |
|---|---|---|
| `categoryId` | `Integer` | `category.id` |
| `categoryName` | `String` | `category.category_name` |
| `description` | `String` | `category.description` (võib olla `null`) |
| `sequence` | `Integer` | `category.sequence` |

Järjestus on `category.sequence ASC`. Mockupil on viimane kategooria „Muud asjad“, andmebaasis „Muud“; vastus kasutab andmebaasi väärtust. Kategooria pilti (`category_image`) vastuses pole.

See teenus on eraldi avalikust `GET /api/categories` teenusest (vt [Kategooriate nimekirja päring](../ToolsView/Kategooriate-nimekirja-paring.md)). Avalik teenus tagastab ainult `categoryId` ja `categoryName`. Admin vajab ka `description` ja `sequence` välju, et modaalaken „Muuda“ saaks need ette täita.

## Eesmärk

Teenus täidab AdminView tabeli „Kategooriad“. Admin näeb kõiki kategooriaid ja saab neid lisada (`POST /api/admin/categories`), muuta (`PUT /api/admin/categories/{categoryId}`) ja kustutada (`DELETE /api/admin/categories/{categoryId}`). Pärast neid tegevusi kutsub vaade teenust uuesti. Päring ei muuda andmeid.

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

Algandmed failist [3_import.sql](../../../database/3_import.sql): 4 kategooriat (id 1–4), vt JSON näidet ülal.

`category_image` ja `tool` ei osale päringus.

## Veaolukorrad

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Sisse logitud kasutaja roll pole `admin`. | 403 Forbidden | tühi (Spring Security) |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

## Vastuvõtu kriteeriumid

- [ ] `GET /api/admin/categories` on olemas ja kättesaadav ainult `admin` rollile.
- [ ] HTTP 200 vastus on massiiv täpselt väljadega `categoryId`, `categoryName`, `description`, `sequence`.
- [ ] Impordiandmetega tagastatakse 4 kategooriat järjestuses `sequence ASC` ja näites toodud väärtustega.
- [ ] Kategooria, mille `description` on `null`, tagastatakse `"description": null` väärtusega.
- [ ] Kategooriateta andmebaasi korral on vastus 200 ja `[]`.
- [ ] Avalik `GET /api/categories` jääb muutmata.
- [ ] Sisse logimata kasutaja saab 401 ja mitte-admin 403; andmebaasi tõrge annab kirjeldatud 500 vastuse.
- [ ] Automaattestid katavad DTO kuju, järjestuse, `null` kirjelduse, tühja loendi, rollipõhise ligipääsu ja 500 juhtumi.
