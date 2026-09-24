# Kategooriate nimekirja päring

**Teenus:** `GET /api/categories`

**Vaste balsamic mockupis:** ToolsView.vue (eraldi STEP-tähis puudub), lehekülg 3/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Kategooriate-nimekirja-paring.png`).

![Mockup](./Kategooriate-nimekirja-paring.png)

Täiendav allikas: [ToolsView märkmed](../../../balsamic/notes/ToolsView-markmed.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

## Sisend

Teenusel puuduvad sisendid. Path variable'id, query parameetrid ja request body puuduvad. Päring on avalik: ToolsView märkmetes on lubatud Admin, Customer ja Külastaja.

## Väljund

**Response (200 OK):** `List<CategoryDto>`, JSON massiiv kõigist viiteandmete kirjetest.

```json
[
  {
    "categoryId": 1,
    "categoryName": "Aiatööd"
  },
  {
    "categoryId": 2,
    "categoryName": "Ehitustööd"
  },
  {
    "categoryId": 3,
    "categoryName": "Koristamine"
  },
  {
    "categoryId": 4,
    "categoryName": "Muud"
  }
]
```

`CategoryDto.java` sisaldab ainult `Integer categoryId` ja `String categoryName`. Need vastavad tabeli `category` väljadele `id` ja `category_name`. Lehekülgjaotust pole.

PDF-is on loendi esimene element ja jätkumist tähistav `...`; siin on täielik vastus [3_import.sql](../../../database/3_import.sql) andmetega. Järjestus on `category.sequence ASC`, võrdsete väärtuste korral `category.id ASC`. PDF nõuab `sequence` järgi sortimist; võrdsete väärtuste ID-järjestus on taski tehniline täpsustus.

Tühja tabeli korral tagastada HTTP 200 ja `[]`, mitte `null` või 404. Andmed loetakse andmebaasist, mitte koodi kirjutatud loendist.

## Eesmärk

Teenus täidab ToolsView otsinguvormi kategooria rippmenüü. Valiku ID kasutatakse tööriistade päringu filtrina. Viiteandmete loend sisaldab ka kirjeid, millega pole parajasti seotud ühtegi tööriista.

Teenust ei dubleerita, kui sama URL-i teenus on teise vaate jaoks juba olemas. Järgida skill'ide ja backend/CLAUDE.md kihtide konventsioone; Controller väljastab DTO-sid. Päring ei muuda andmeid.

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

`category_name` on kohustuslik ja unikaalne. `description` ja `sequence` ei kuulu DTO-sse; `sequence` kasutatakse sortimiseks. `category_image`, `tool` ja `tool_image` ei osale päringus. See on filtrite lihtloend, mitte avalehe `GET /api/categories/detailed-info` teenus.

Näidisandmete ID-d ja nimed on esitatud eespool olevas täielikus JSON-is.

## Veaolukorrad

PDF-is on „Veateated: —”. Allolev 500 on taski tehniline täpsustus. Sisendita avaliku loendi jaoks ei lisata 400/401/403/404 ärivigu.

| Olukord | Status code | Response body |
|---|---|---|
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

Kasuta olemasoleva `ApiError` kuju: `message` ja `errorCode` on String-väljad. Praegune `RestExceptionHandler` ei taga veel seda 500 lepingut. SQL-i ega stack trace'i kliendile ei tagastata.

## Vastuvõtu kriteeriumid

- [ ] `GET /api/categories` on kättesaadav ka ilma sisselogimiseta ega vaja päringu sisendeid.
- [ ] Vastus on HTTP 200 ja massiiv, mille elementidel on ainult `categoryId` ning `categoryName`.
- [ ] Tagastatakse kõik tabeli `category` kirjed järjestuses `category.sequence ASC`, võrdsete väärtuste korral `category.id ASC`.
- [ ] Impordiandmetega vastab tulemus eespool toodud 4 kirjega JSON-ile.
- [ ] Tööriistadeta viitekirjed säilivad vastuses; tühja tabeli korral on vastus `[]`.
- [ ] Andmebaasi tõrkel tagastatakse kirjeldatud 500 `ApiError`; tehnilisi detaile ei avaldata.
- [ ] Päring ei muuda andmeid ega lisa paralleelset endpoint'i sama funktsiooni jaoks.
- [ ] Automaattestid kontrollivad avalikku ligipääsu, DTO kuju, täielikku loendit, sortimist, tühja tulemust ja 500 vastust. Sortimise testandmed peavad eristama nõutud järjekorda juhuslikust sisestamisjärjekorrast.
