# Kategooria lisamine

**Teenus:** `POST /api/admin/categories`

**Vaste balsamic mockupis:** AdminView.vue (eraldi STEP-tähis puudub), lehekülg 13/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Kategooria-lisamine.png`).

![Mockup](./Kategooria-lisamine.png)

Täiendav allikas: [AdminView märkmed](../../../balsamic/notes/AdminView-markmed.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

## Sisend

Request body (`CategoryRequestDto.java`):

| Väli | Java tüüp | Kohustuslik | Reegel |
|---|---|---|---|
| `categoryName` | `String` | Jah | Mitte tühi, kuni 100 märki, unikaalne |
| `description` | `String` | Ei | Kuni 255 märki, võib olla `null` |
| `sequence` | `Integer` | Jah | Kategooriate järjekord (väiksem enne) |

Näide:

```json
{
  "categoryName": "Talvetööd",
  "description": "Lumelabidad, jääpurustajad",
  "sequence": 400
}
```

Näide kasutab uut kategooria nime. AdminView märkme näide `"Aiatööd"` on impordiandmetes juba olemas ja annaks vea `CATEGORY_UNAVAILABLE`.

Teenus on ainult adminile (`/api/admin/**`, `hasRole("admin")`).

## Väljund

**Response (200 OK):** tühi body (`Response (200): NONE`).

Tabelisse `category` lisatakse uus rida. Kategooria pilti (`category_image`) selles versioonis ei lisata, seega on uuel kategoorial pilt puudu.

## Eesmärk

Teenust kasutab AdminView nupp „Lisa kategooria“. Nupp avab modaalakna väljadega nimi, kirjeldus ja järjekord; „Salvesta“ saadab selle päringu. Pärast edukat vastust laadib vaade kategooriate tabeli uuesti (`GET /api/admin/categories`). Uus kategooria ilmub ka avalikku `GET /api/categories` loendisse.

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

`category_name` on andmebaasis `UNIQUE`. Teenus kontrollib nime olemasolu enne salvestamist, et anda arusaadav 403 viga, mitte andmebaasi erind.

Algandmed failist [3_import.sql](../../../database/3_import.sql):

| id | category_name | sequence |
|---|---|---|
| 1 | Aiatööd | 100 |
| 2 | Ehitustööd | 200 |
| 3 | Koristamine | 300 |
| 4 | Muud | 10000 |

`category_image` ja `tool` ei muutu.

## Veaolukorrad

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Sisse logitud kasutaja roll pole `admin`. | 403 Forbidden | tühi (Spring Security) |
| Sama nimega kategooria on juba olemas (nt `"Aiatööd"`). | 403 Forbidden | `{"errorCode":"CATEGORY_UNAVAILABLE","message":"Sellise nimega kategooria on juba olemas"}` |
| `categoryName` puudub, on tühi või liiga pikk; `sequence` puudub; `description` on liiga pikk. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"<väli>: <valideerimise teade>"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kategooria lisamine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- `CATEGORY_UNAVAILABLE` on uus kood. Seda visatakse olemasoleva `ForbiddenException` klassiga.
- 400 tuleb `@Valid` + `@NotBlank`/`@Size`/`@NotNull` kaudu olemasolevast `handleMethodArgumentNotValid` handlerist.
- Nime võrdlus on täpne (tõstutundlik), nagu andmebaasi `UNIQUE` piirang.

## Vastuvõtu kriteeriumid

- [ ] `POST /api/admin/categories` on olemas ja kättesaadav ainult `admin` rollile.
- [ ] Näites toodud body annab 200 tühja body'ga ning `category` tabelis on uus rida sama nime, kirjelduse ja järjekorraga.
- [ ] `description: null` või puuduv `description` on lubatud.
- [ ] Olemasolev nimi (`"Aiatööd"`) annab 403 `CATEGORY_UNAVAILABLE` ja uut rida ei lisata.
- [ ] Puuduv/tühi `categoryName`, puuduv `sequence` ja liiga pikad väljad annavad 400 `INCORRECT_INPUT`.
- [ ] Uus kategooria on näha nii `GET /api/admin/categories` kui ka `GET /api/categories` vastuses.
- [ ] Sisse logimata kasutaja saab 401 ja mitte-admin 403.
- [ ] Automaattestid katavad eduka lisamise, `null` kirjelduse, duplikaatnime, valideerimisvead, 500 juhtumi ja rollipõhise ligipääsu.
