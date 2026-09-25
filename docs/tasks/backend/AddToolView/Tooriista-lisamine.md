# Tööriista lisamine

**Teenus:** `POST /api/tools`

**Vaste balsamic mockupis:** AddToolView.vue (eraldi STEP-tähis puudub), lehekülg 12/17 failis [Laenukas2509.pdf](../../../balsamic/notes/Laenukas2509.pdf) (vt `Tooriista-lisamine.png`).

![Mockup](./Tooriista-lisamine.png)

Täiendavad allikad: [AddToolView märkmed](../../../balsamic/notes/AddToolView-markmed.md) ja [googlega_login.md](../../../balsamic/notes/googlega_login.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

## Sisend

Request body (`ToolCreateRequestDto.java`):

| Väli | Java tüüp | Kohustuslik | Reegel | Andmebaasi veerg |
|---|---|---|---|---|
| `ownerId` | `Integer` | — | Ei kasutata: omanik võetakse sessioonist (vt allpool) | `tool.owner_id` |
| `categoryId` | `Integer` | Jah | Olemasoleva kategooria `category.id` | `tool.category_id` |
| `name` | `String` | Jah | Mitte tühi, kuni 150 märki | `tool.name` |
| `description` | `String` | Ei | Kuni 2000 märki, võib olla `null` | `tool.description` |
| `imageData` | `String` | Jah | Pildifaili Base64 kuju (ilma `data:` prefiksita); `""` tähendab, et pilti ei lisata | `tool_image.image_data` |

Näide (sildilt):

```json
{
  "ownerId": 3,
  "categoryId": 2,
  "name": "Akutrell",
  "description": "18 V akutrell, kaks akut ja laadija. Sobib puurimiseks ja kruvide keeramiseks.",
  "imageData": "BASE64-image-data"
}
```

`ownerId = 3` on Liis Kask ja `categoryId = 2` on „Ehitustööd“ failis `3_import.sql`. Nimi ja kirjeldus on võetud impordi tööriistalt `tool.id = 1` (mille omanik on Marko); siin on see näide uuest tööriistast, mille Liis lisab. `"BASE64-image-data"` on kohatäitja.

**Omanik:** sildi lisainfo järgi tuleb `ownerId` sisse logitud kasutaja sessioonist. Teenus kasutab `@AuthenticationPrincipal AppUserPrincipal` → `getUserId()` (vt `googlega_login.md`: „Kasutaja ID-d ei tohi võtta frontendist“). Body's saadetud `ownerId` väärtust ei kasutata.

Päring nõuab sisselogimist. Silt nimetab rolliks Customer; `SecurityConfig` rolli ei piira (`anyRequest().authenticated()`).

Väli „Pildi nimi“ on ainult kasutajaliidese info ja backendile seda ei saadeta (`tool_image` tabelis nimeveergu pole).

## Väljund

**Response (200 OK):** tühi body (`Response (200): NONE`).

Ühes transaktsioonis (`@Transactional`):

1. Luuakse `tool` rida: `owner_id` sessioonist, `category_id`, `name`, `description`, `status = 'A'`, `created_at` ja `updated_at` = praegune aeg.
2. Kui `imageData` ei ole tühi string, luuakse `tool_image` rida: `tool_id` = uus tööriist, `image_data` = `Base64.getDecoder().decode(imageData)` (pildi tegelikud baidid), `is_main = true`. Vorm toetab loomisel ainult ühte (pea)pilti.
3. Kui `imageData = ""`, pilti ei looda.

Impordiandmete järel saab uus tööriist `tool.id = 9` ja pilt `tool_image.id = 9` (`setval` seab järjendid maksimaalsele ID-le).

Andmebaasi salvestatakse pildi tegelikud baidid, nagu impordiandmetes (toored SVG-baidid). Lugemisel kodeerivad `GET /api/tools` ja `GET /api/tools/{toolId}` need tagasi Base64-ks (`Base64.getEncoder()`), seega saab frontend sama stringi tagasi. `StringBytesConverter` (UTF-8 tekst) siin ei sobi, sest see salvestaks Base64 teksti baidid, mitte pildi baidid.

**Saadavus:** sildil on lahtine küsimus väljade „Saadavus: Alates / Kuni“ kohta (`booking.start_date`, `end_date`, `renter_id` NOT NULL). Selle teenuse request body's kuupäevi pole, seega `booking` tabelit see teenus ei muuda. Küsimus jääb lahtiseks ja see task seda ei lahenda.

## Eesmärk

Teenust kasutab AddToolView vaate (`/tools/new`) nupp „Salvesta“. Sisse logitud kasutaja lisab oma tööriista rendile: pildi, nime, kirjelduse ja kategooria. Uus tööriist ilmub staatusega `A` (saadaval) tööriistade otsingusse (`GET /api/tools`) ja kasutaja „Minu tööriistad rendiks“ loendisse.

## Seotud andmebaasi tabelid

Vt [2_create.sql](../../../database/2_create.sql).

### `tool`

```sql
CREATE TABLE tool (
    id serial PRIMARY KEY,
    owner_id integer NOT NULL REFERENCES app_user (id),
    category_id integer NOT NULL REFERENCES category (id),
    name varchar(150) NOT NULL,
    description varchar(2000),
    status char(1) NOT NULL DEFAULT 'A' CHECK (status IN ('A', 'U')),
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp
);
```

### `tool_image`

```sql
CREATE TABLE tool_image (
    id serial PRIMARY KEY,
    tool_id integer NOT NULL REFERENCES tool (id) ON DELETE CASCADE,
    image_data bytea NOT NULL,
    is_main boolean NOT NULL DEFAULT false,
    CONSTRAINT tool_image_data_unique UNIQUE (tool_id, image_data)
);

CREATE UNIQUE INDEX tool_image_one_main_unique ON tool_image (tool_id) WHERE is_main = true;
```

Uuel tööriistal on kuni üks põhipilt, seega unikaalsuspiirangud ei sega.

### `category` (ainult kontroll)

```sql
CREATE TABLE category (
    id serial PRIMARY KEY,
    category_name varchar(100) NOT NULL UNIQUE,
    description varchar(255),
    sequence integer NOT NULL
);
```

`categoryId` olemasolu kontrollitakse enne salvestamist.

Algandmed failist [3_import.sql](../../../database/3_import.sql):

| category.id | category_name |
|---|---|
| 1 | Aiatööd |
| 2 | Ehitustööd |
| 3 | Koristamine |
| 4 | Muud |

Impordis on tööriistad `tool.id` 1–8 ja nende pildid `tool_image.id` 1–8. Kasutajad: Marko Tamm (`userId = 1`) ja Liis Kask (`userId = 3`). `booking`, `profile` ja `location` ei muutu.

## Veaolukorrad

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Kategooriat `categoryId = 123` pole. Teates kasutada tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'categoryId' väärtusega: 123"}` |
| `name` puudub või on tühi, `categoryId` või `imageData` puudub, väli on liiga pikk või `imageData` pole kehtiv Base64. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"<väli>: <valideerimise teade>"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Tööriista lisamine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist (sildi ainus veateade).
- 400 tuleb `@Valid` + `@NotBlank`/`@NotNull`/`@Size` kaudu olemasolevast `handleMethodArgumentNotValid` handlerist. Vigane Base64 (`Base64.getDecoder().decode` viskab `IllegalArgumentException`) tuleb eraldi kinni püüda ja anda 400 `INCORRECT_INPUT` teatega `imageData: ...`, muidu tuleks 500.
- Ühtne 500 kuju tuleb teostamisel tagada, sest praegune `RestExceptionHandler` seda ei käsitle.
- Kontrollide järjekord: 400, siis 404. Vea korral ei looda ei `tool` ega `tool_image` rida.

## Vastuvõtu kriteeriumid

- [ ] `POST /api/tools` on olemas ja nõuab sisselogimist.
- [ ] Kehtiv päring annab 200 tühja body'ga ning loob `tool` rea väljadega `category_id`, `name`, `description`, `status = 'A'`.
- [ ] `tool.owner_id` on sessiooni kasutaja ID; body's antud teistsugune `ownerId` ei mõjuta tulemust.
- [ ] Mittetühja `imageData` korral luuakse üks `tool_image` rida `is_main = true`, mille `image_data` on Base64-st dekodeeritud baidid; `GET /api/tools/{toolId}` tagastab sama Base64 stringi. `imageData = ""` korral pilti ei looda.
- [ ] `description: null` on lubatud.
- [ ] Uus tööriist on nähtav `GET /api/tools` ja `GET /api/tools/{toolId}` vastuses.
- [ ] Olematu `categoryId` annab 404 ja `PRIMARY_KEY_NOT_FOUND` teate päringu ID-ga.
- [ ] Valideerimisvead annavad 400 `INCORRECT_INPUT`; sisse logimata kasutaja saab 401.
- [ ] Vea korral ei jää andmebaasi poolikut tööriista ega pilti (transaktsioon).
- [ ] `booking` tabel ei muutu.
- [ ] Automaattestid katavad pildiga ja pildita lisamise, `null` kirjelduse, sessioonist võetud omaniku, olematu kategooria, valideerimisvead, 401 ja 500 juhtumi.
