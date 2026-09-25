# Kategooria muutmine

**Teenus:** `PUT /api/admin/categories/{categoryId}`

**Vaste balsamic mockupis:** AdminView.vue (eraldi STEP-tähis puudub), lehekülg 13/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Kategooria-muutmine.png`).

![Mockup](./Kategooria-muutmine.png)

Täiendav allikas: [AdminView märkmed](../../../balsamic/notes/AdminView-markmed.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

## Sisend

| Parameeter | Asukoht | Java tüüp | Kohustuslik | Tähendus |
|---|---|---|---|---|
| `categoryId` | Path variable | `Integer` | Jah | Muudetava kategooria `category.id`. |

Request body (`CategoryRequestDto.java`, sama DTO nagu lisamisel):

| Väli | Java tüüp | Kohustuslik | Reegel |
|---|---|---|---|
| `categoryName` | `String` | Jah | Mitte tühi, kuni 100 märki, ei tohi kattuda teise kategooria nimega |
| `description` | `String` | Ei | Kuni 255 märki, võib olla `null` |
| `sequence` | `Integer` | Jah | Kategooriate järjekord (väiksem enne) |

Näide `PUT /api/admin/categories/1`:

```json
{
  "categoryName": "Aiatööd",
  "description": "Muruniidukid, labidad, rehad",
  "sequence": 100
}
```

Teenus on ainult adminile (`/api/admin/**`, `hasRole("admin")`).

## Väljund

**Response (200 OK):** tühi body (`Response (200): NONE`).

Kõik kolm välja kirjutatakse üle (PUT asendab kogu kategooria sisu). Kui `description` on `null`, saab andmebaasis kirjeldus väärtuse `NULL`. Kategooria pilt (`category_image`) ja kategooria tööriistad ei muutu.

## Eesmärk

Teenust kasutab AdminView tabeli „Kategooriad“ nupp „Muuda“. Nupp avab modaalakna, mis on täidetud `GET /api/admin/categories` vastuse andmetega; „Salvesta“ saadab selle päringu. Pärast edukat vastust laadib vaade kategooriate tabeli uuesti. Muudatus kajastub ka avalikus kategooriate loendis.

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

Algandmed failist [3_import.sql](../../../database/3_import.sql):

| id | category_name | description | sequence |
|---|---|---|---|
| 1 | Aiatööd | Muruniidukid, labidad, rehad | 100 |
| 2 | Ehitustööd | Trellid, ketassaed, redelid | 200 |
| 3 | Koristamine | Tekstiilipesurid, aknapesurid, aurupesurid | 300 |
| 4 | Muud | Lumelabidad, naabrimehed, naabrinaised | 10000 |

`category_image` ja `tool` ei muutu.

## Veaolukorrad

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Sisse logitud kasutaja roll pole `admin`. | 403 Forbidden | tühi (Spring Security) |
| Kategooriat `categoryId = 123` pole. Teates kasutada tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'categoryId' väärtusega: 123"}` |
| Uus nimi kuulub mõnele teisele kategooriale (nt `categoryId = 1` nimeks `"Ehitustööd"`). | 403 Forbidden | `{"errorCode":"CATEGORY_UNAVAILABLE","message":"Sellise nimega kategooria on juba olemas"}` |
| `categoryName` puudub, on tühi või liiga pikk; `sequence` puudub; `description` on liiga pikk. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"<väli>: <valideerimise teade>"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kategooria muutmine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist.
- `CATEGORY_UNAVAILABLE` on uus kood. Seda visatakse olemasoleva `ForbiddenException` klassiga.
- Sama kategooria enda nime uuesti salvestamine on lubatud: nime kontroll peab välistama muudetava kategooria (`id <> categoryId`).
- Kontrollide järjekord: 400 (valideerimine), 404, siis 403.

## Vastuvõtu kriteeriumid

- [ ] `PUT /api/admin/categories/{categoryId}` on olemas ja kättesaadav ainult `admin` rollile.
- [ ] Kehtiv päring annab 200 tühja body'ga ja `category` rea kõik kolm välja on uuendatud.
- [ ] Muutmata nimega salvestamine (näite body `categoryId = 1` jaoks) annab 200.
- [ ] Teise kategooria nimi annab 403 `CATEGORY_UNAVAILABLE` ja andmed ei muutu.
- [ ] Olematu `categoryId` annab 404 ja `PRIMARY_KEY_NOT_FOUND` teate päringu ID-ga.
- [ ] Valideerimisvead annavad 400 `INCORRECT_INPUT`.
- [ ] Kategooria tööriistad ja pilt jäävad muutmata.
- [ ] Sisse logimata kasutaja saab 401 ja mitte-admin 403.
- [ ] Automaattestid katavad eduka muutmise, sama nimega salvestamise, duplikaatnime, 404, valideerimisvead, 500 juhtumi ja rollipõhise ligipääsu.
