# Valitud linna linnaosade päring

**Teenus:** `GET /api/cities/{cityId}/districts`

**Vaste balsamic mockupis:** ToolsView.vue (eraldi STEP-tähis puudub), lehekülg 3/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Valitud-linna-linnaosade-paring.png`).

![Mockup](./Valitud-linna-linnaosade-paring.png)

Täiendav allikas: [ToolsView märkmed](../../../balsamic/notes/ToolsView-markmed.md). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

## Sisend

| Parameeter | Asukoht | Java tüüp | Kohustuslik | Tähendus |
|---|---|---|---|---|
| `cityId` | Path variable | `Integer` | Jah | Valitud linna `city.id`. |

Query parameetrid ja request body puuduvad. Päring on avalik kõigile ToolsView rollidele, sh külalisele. Näide: `GET /api/cities/1/districts`.

`cityId` peab olema Java Integer-iks teisendatav. See on konkreetse linna ID: erinevalt tööriistade otsingu valikulisest filtrist ei tähenda siin `0` filtri ignoreerimist. Kui vastavat linna pole, tagastada 404.

## Väljund

**Response (200 OK):** `List<DistrictDto>`, ainult valitud linna linnaosad. Näide Tallinna (`cityId = 1`) kohta:

```json
[
  {
    "districtId": 1,
    "districtName": "Kristiine"
  },
  {
    "districtId": 2,
    "districtName": "Mustamäe"
  },
  {
    "districtId": 3,
    "districtName": "Haabersti"
  },
  {
    "districtId": 4,
    "districtName": "Kesklinn"
  },
  {
    "districtId": 5,
    "districtName": "Lasnamäe"
  },
  {
    "districtId": 6,
    "districtName": "Nõmme"
  },
  {
    "districtId": 7,
    "districtName": "Pirita"
  },
  {
    "districtId": 8,
    "districtName": "Põhja-Tallinn"
  }
]
```

`DistrictDto.java` sisaldab ainult `Integer districtId` ja `String districtName`, vastavalt `district.id` ning `district.district_name`. PDF-i lühendatud näide on laiendatud kõigi Tallinna linnaosadega failist [3_import.sql](../../../database/3_import.sql).

Tagastada kõik `district.city_id = cityId` kirjed. PDF ei määra sortimist; taski tehniline täpsustus on `district.id ASC`, ilma lehekülgjaotuseta. Olemasoleva linna tühi linnaosaloend annab 200 ja `[]`; puuduv linn annab 404. Neid olukordi tuleb eristada linna olemasolu kontrolliga.

## Eesmärk

Teenus täidab ToolsView linnaosa rippmenüü pärast linna valimist. Kasutajale kuvatakse ainult valitud linna linnaosad. Valitud linnaosa ID-d kasutatakse `GET /api/tools` filtrina.

Teenust saab jagada registreerumisvormiga; sama URL-i paralleelset teostust ei looda. Järgida skill'ide ja backend/CLAUDE.md kihtide konventsioone. Päring ei muuda andmeid.

## Seotud andmebaasi tabelid

Vt [2_create.sql](../../../database/2_create.sql).

### `city`

```sql
CREATE TABLE city (
    id serial PRIMARY KEY,
    city_name varchar(100) NOT NULL UNIQUE
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

`district.city_id` on kohustuslik välisvõti tabelisse `city`. Piirang `UNIQUE (city_id, district_name)` keelab nime kordumise ühe linna sees, kuid lubab sama nime eri linnades. `city` kasutatakse linna olemasolu kontrollimiseks.

Algandmed failist [3_import.sql](../../../database/3_import.sql):

| city.id | city_name | district.id väärtused | Linnaosade arv |
|---|---|---|---|
| 1 | Tallinn | 1–8 | 8 |
| 2 | Tartu | 9–26 | 18 |
| 3 | Pärnu | 27–33 | 7 |

PDF-i veanäites kasutatud `cityId = 123` impordiandmetes puudub. `location`, `profile` ja `tool` ei osale päringus: tööriistadeta linnaosad jäävad loendisse.

## Veaolukorrad

404 leping pärineb PDF-ist; 400 ja 500 on sisendi ning tehnilise tõrke käsitlemise täpsustused. Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Linn `cityId = 123` puudub. Muu puuduva ID puhul kasutada teates tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'cityId' väärtusega: 123"}` |
| `cityId` pole täisarv või ei mahu Java Integer vahemikku. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"cityId: peab olema Integer-tüüpi täisarv"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Linnaosade laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

404 saab anda olemasoleva `PrimaryKeyNotFoundException` kaudu. Path variable'i teisendamise 400 ja ühtne 500 kuju tuleb teostamisel tagada; praegune väljade valideerimise handler ei taga neid automaatselt. SQL-i ega stack trace'i ei tagastata.

## Vastuvõtu kriteeriumid

- [ ] Avalik `GET /api/cities/{cityId}/districts` võtab vastu Integer-tüüpi linna ID.
- [ ] HTTP 200 vastus on massiiv ainult väljadega `districtId` ja `districtName`.
- [ ] Vastuses on ainult valitud linna linnaosad, järjestuses `district.id ASC`.
- [ ] Impordi Tallinn annab 8, Tartu 18 ja Pärnu 7 linnaosa; teiste linnade kirjeid vastuses pole.
- [ ] Olemasoleva linna tühi loend annab 200 ja `[]`; puuduv linn annab 404 ning päringu ID-ga `PRIMARY_KEY_NOT_FOUND` teate.
- [ ] `cityId = 123` annab PDF-is kirjeldatud täpse veavastuse.
- [ ] Vigane arvuvorming annab kirjeldatud 400 ja andmebaasi tõrge kirjeldatud 500 vastuse.
- [ ] Päring ei sõltu tööriistade või profiilide olemasolust ega muuda andmeid.
- [ ] Automaattestid katavad avaliku ligipääsu, DTO kuju, eri linnade filtreerimise, järjestuse, tööriistadeta linnaosa, olemasoleva linna tühja loendi ning 400/404/500 juhtumid.
