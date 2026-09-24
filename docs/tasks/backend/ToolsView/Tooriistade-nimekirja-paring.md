# Tööriistade nimekirja päring

**Teenus:** `GET /api/tools`

**Vaste balsamic mockupis:** ToolsView.vue (eraldi STEP-tähis puudub), lehekülg 3/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Tooriistade-nimekirja-paring.png`).

![Mockup](./Tooriistade-nimekirja-paring.png)

Täiendav allikas: [ToolsView märkmed](../../../balsamic/notes/ToolsView-markmed.md). Task käsitleb backend'i teenust; Vue otsinguvaate teostus ei kuulu siia.

## Sisend

Path variable'id ja request body puuduvad. Endpoint on avalik: maketi rollid on Admin, Customer ja Külastaja.

| Query parameeter | Java tüüp | Kohustuslik | Vaikeväärtus | Tähendus |
|---|---|---|---|---|
| `categoryId` | `Integer` | Ei | `0` | `0` või puudumine jätab kategooriafiltri rakendamata; positiivne väärtus filtreerib `tool.category_id` järgi. |
| `cityId` | `Integer` | Ei | `0` | `0` või puudumine jätab linnafiltri rakendamata; positiivne väärtus filtreerib omaniku aadressi linna järgi. |
| `districtId` | `Integer` | Ei | `0` | `0` või puudumine jätab linnaosafiltri rakendamata; positiivne väärtus filtreerib omaniku aadressi linnaosa järgi. |
| `status` | `String` | Ei | `A` | `A` = saadaval, `U` = pole saadaval, `0` = staatusefiltrit ei rakendata. `0` on ainult päringu eriväärtus, mitte andmebaasi staatus. |
| `pageNumber` | `Integer` | Ei | `1` | Lehenumber algab ühest; peab olema vähemalt 1. |
| `pageSize` | `Integer` | Ei | `6` | Kirjete arv lehel; peab olema vähemalt 1. |

**Kasutajaga kinnitatud:** puuduv `status` tähendab `A`, `status=0` tähendab kõiki staatusi; vaikimisi `pageNumber=1`, `pageSize=6`; tulemused järjestatakse `tool.id ASC`.

Kõik etteantud filtrid ühendatakse AND-tingimusega. `districtId` võib olla antud ka ilma `cityId`-ta. `cityId` ja `districtId` vastuoluline kombinatsioon annab tühja tulemuse, mitte teise linna tööriistu. Positiivne filter, mille ID-d andmebaasis pole, annab samuti tühja tulemuse; see loendipäring ei kasuta puuduva filtriväärtuse jaoks 404. Need servajuhtumid on taski tehnilised täpsustused, mida PDF eraldi ei kirjelda.

Negatiivne ID-filter, alla ühe lehenumber või lehe suurus, Integer-iks teisendamatu arv ja tundmatu `status` annavad 400. Selgelt saadetud tühi parameetriväärtus ei ole sama mis puuduv parameeter: tagastada 400. `status` lubatud väärtused on täpselt `A`, `U` ja `0`.

Näidispäring, mille täielik vastus on allpool:

```http
GET /api/tools?categoryId=2&cityId=1&districtId=1&status=A&pageNumber=1&pageSize=6
```

## Väljund

**Response (200 OK):** `ToolsResponse.java` lehekülje metaandmete ja tööriistade loendiga.

```json
{
  "pageNumber": 1,
  "pageSize": 6,
  "totalPages": 1,
  "totalElements": 1,
  "tools": [
    {
      "toolId": 1,
      "toolName": "Akutrell",
      "description": "18 V akutrell, kaks akut ja laadija. Sobib puurimiseks ja kruvide keeramiseks.",
      "imageData": "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNDAiIGhlaWdodD0iMjQwIiB2aWV3Qm94PSIwIDAgMjQwIDI0MCI+PHJlY3Qgd2lkdGg9IjI0MCIgaGVpZ2h0PSIyNDAiIHJ4PSIxNiIgZmlsbD0iI2YxZjVmOSIvPjxwYXRoIGZpbGw9IiMyNTYzZWIiIGQ9Ik01MCA2MGgxMDV2NDVINTB6IE04NSAxMDVoMzV2NjBIODV6XCIvPjxwYXRoIGZpbGw9IiMzMzQxNTUiIGQ9Ik0xNTUgNzBoMjV2MjVoLTI1eiBNMTgwIDc4aDMwdjloLTMweiBNNzAgMTU1aDY1djE1SDcwelwiLz48dGV4dCB4PSIxMjAiIHk9IjIxNSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9InNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMjAiIGZpbGw9IiMwZjE3MmEiPkFrdXRyZWxsPC90ZXh0Pjwvc3ZnPg==",
      "status": "A",
      "cityName": "Tallinn",
      "districtName": "Kristiine"
    }
  ]
}
```

Näite ainus sobiv tööriist on impordi Akutrell. Sama kategooria Redel on staatusega `U` ega sobi `status=A` filtriga. JSON-i pildistring on saadud `3_import.sql` tegelikust `tool_image.image_data` SVG-sisust, mitte kohatäitjast; lähtefaili pildisisu ei parandata ega muudeta kodeerimise käigus.

| Väli | Java tüüp | Tähendus |
|---|---|---|
| `pageNumber` | `Integer` | Päringus kasutatud 1-põhine lehenumber. |
| `pageSize` | `Integer` | Päringus kasutatud lehe suurus; viimasel lehel võib `tools` olla lühem. |
| `totalPages` | `Integer` | Ümardus üles: `totalElements / pageSize`; tühja tulemuse puhul 0. |
| `totalElements` | `Long` | Kõigile filtritele vastavate erinevate tööriistade arv enne lehekülgjaotust. |
| `tools` | `List<ToolListItemDto>` | Käesoleva lehe kaardid. `ToolListItemDto` on selle taski nimetus; PDF nimetab ainult ümbrist `ToolsResponse`. |

`ToolListItemDto` väljad:

| Väli | Java tüüp | Andmeallikas |
|---|---|---|
| `toolId` | `Integer` | `tool.id`. |
| `toolName` | `String` | `tool.name`. |
| `description` | `String` | `tool.description`, võib olla NULL. |
| `imageData` | `String` | `tool_image.image_data` Base64-na, ainult kirjest `is_main = true`; põhipildi puudumisel NULL. |
| `status` | `String` | `tool.status`, `A` või `U`. |
| `cityName` | `String` | Omaniku profiili aadressi linna nimi; profiili puudumisel NULL. |
| `districtName` | `String` | Omaniku profiili aadressi linnaosa nimi; profiili puudumisel NULL. |

**Kasutajaga kinnitatud:** kui omanikul puudub profiil, säilib tema tööriist loendis, kui ülejäänud filtrid sobivad ja asukohafiltreid pole; `cityName` ning `districtName` on `null`. Positiivse linna- või linnaosafiltri korral selline tööriist ei sobitu.

Põhipildi puudumine ei eemalda tööriista loendist. Ka siis, kui lisapilte on olemas, ei valita suvalist lisapilti põhipildi asemele. `imageData` sisaldab Base64-baite ilma data-URI prefiksita; `description`, `imageData`, `cityName` ja `districtName` NULL-välju JSON-ist ei eemaldata. Impordipildid on SVG-d; MIME-tüübi ega pildi üleslaadimise uut lepingut see task ei lisa.

Lehekülgjaotus rakendub pärast filtreerimist. `tools` sisaldab iga tööriista kõige rohkem ühe korra; pildiseosed ei tohi suurendada `totalElements` väärtust. Viimasest lehest suurem positiivne `pageNumber` annab 200 ja `tools: []`, säilitades tegelikud `totalElements` ning `totalPages` väärtused. Täiesti tühja tulemuse näide:

```json
{
  "pageNumber": 1,
  "pageSize": 6,
  "totalPages": 0,
  "totalElements": 0,
  "tools": []
}
```

Kontrollnäited kogu praeguse [3_import.sql](../../../database/3_import.sql) kohta:

| Päringu tingimus | totalElements | totalPages (pageSize=6) | Esimese lehe toolId-d |
|---|---|---|---|
| Vaikimisi `status=A` | 7 | 2 | 1, 3, 4, 5, 6, 7 |
| `status=0` | 8 | 2 | 1, 2, 3, 4, 5, 6 |
| `status=U` | 1 | 1 | 2 |
| `categoryId=2&status=A` | 1 | 1 | 1 |
| `cityId=1&districtId=2&status=A` | 3 | 1 | 3, 4, 8 |
| `cityId=2&status=0` | 0 | 0 | tühi |

Vaikimisi päringu teisel lehel on ainult `toolId=8`. PDF-i `totalElements=8` näide sobib `status=0` päringule; vaikimisi `A` korral on andmebaasis 7 sobivat kirjet. PDF-i kaardil olevat hinda ei lisata API-sse: kirjeldatud DTO ega SQL ei sisalda hinnavälja.

## Eesmärk

Teenus laadib ToolsView tööriistakaardid vaate avamisel, filtrite rakendamisel ja lehekülje vahetamisel. Tööriista asukoht tuletatakse omaniku profiili aadressist ning kaardipilt tööriista põhipildist. Vastuse metaandmed võimaldavad frontendil kuvada leheküljenumbreid.

Filtrite valikuloendid tulevad sama kausta eraldi taskides kirjeldatud teenustest. „Vaata detaile” navigeerib `/tools/{toolId}` vaatesse; detailpäring ei kuulu siia. `booking`-põhist perioodi saadavust ega omaniku konto staatuse järgi lisafiltrit kirjeldatud leping ei nõua; neid ei lisata vaikimisi.

Järgida skill'ide ja backend/CLAUDE.md kihtide konventsioone. Controller tagastab DTO-d. Teenus ei lisa, muuda ega kustuta tööriistu või seotud andmeid.

## Seotud andmebaasi tabelid

Vt [2_create.sql](../../../database/2_create.sql).

### `tool`

Põhitabel: omanik, kategooria, nimi, kirjeldus ja A/U staatus. `category_id` kasutatakse kategooriafiltris.

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

Ainult `is_main = true` kirje annab kaardipildi. Tööriist võib eksisteerida ilma ühegi pildita või ainult lisapiltidega.

```sql
CREATE TABLE tool_image (
    id serial PRIMARY KEY,
    tool_id integer NOT NULL REFERENCES tool (id) ON DELETE CASCADE,
    image_data bytea NOT NULL,
    is_main boolean NOT NULL DEFAULT false,
    CONSTRAINT tool_image_data_unique UNIQUE (tool_id, image_data)
);
```

### `category`

Kategooriafilter viitab `tool.category_id` kaudu sellele tabelile. Kategooria nime pole tööriistakaardi vastuses vaja; nime rippmenüüsse annab eraldi endpoint.

```sql
CREATE TABLE category (
    id serial PRIMARY KEY,
    category_name varchar(100) NOT NULL UNIQUE,
    description varchar(255),
    sequence integer NOT NULL
);
```

### `app_user`

`tool.owner_id` viitab omanikule; avalikku vastusesse ei anta Google-identifikaatorit ega kasutaja kontaktandmeid. Rolli tabelit päringuks vaja pole.

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

Omanikul on kõige rohkem üks profiil; profiili puudumine on SQL-is lubatud. `profile.user_id = tool.owner_id` annab aadressiseose.

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

### `location`

`profile.location_id` annab tööriista omaniku aadressi. Täpset tänavat, maja- ja korterinumbrit avalik loend ei tagasta.

```sql
CREATE TABLE location (
    id serial PRIMARY KEY,
    district_id integer NOT NULL REFERENCES district (id),
    street_name varchar(150) NOT NULL,
    house_number varchar(20) NOT NULL,
    apartment_number varchar(20),
    lng decimal(10, 7),
    lat decimal(10, 7)
);
```

### `district`

`location.district_id` annab linnaosa nime ja linna viite. Linn ja linnaosa peavad pärinema samast aadressiahelast.

```sql
CREATE TABLE district (
    id serial PRIMARY KEY,
    city_id integer NOT NULL REFERENCES city (id),
    district_name varchar(100) NOT NULL,
    CONSTRAINT district_city_name_unique UNIQUE (city_id, district_name)
);
```

### `city`

`district.city_id` annab linna nime ja linnafiltri väärtuse.

```sql
CREATE TABLE city (
    id serial PRIMARY KEY,
    city_name varchar(100) NOT NULL UNIQUE
);
```

Põhipildi lisapiirang SQL-is:

```sql
CREATE UNIQUE INDEX tool_image_one_main_unique ON tool_image (tool_id) WHERE is_main = true;
```

Seosed:

```text
tool.owner_id → app_user.id → profile.user_id
profile.location_id → location.id → district.id (location.district_id)
district.city_id → city.id
tool.category_id → category.id
tool.id ← tool_image.tool_id, is_main = true
```

`profile.user_id` unikaalsus ja põhipildi osaline unikaalne indeks võimaldavad tagastada iga tööriista ühe korra. Profiili ja põhipildi valikulisus peab säilima nii andmepäringus kui ka koguarvu arvutamisel.

Seotud impordiandmed:

| Omanik | Profiil / asukoht | Linn / linnaosa | Tööriistad |
|---|---|---|---|
| `app_user.id=1`, Marko Tamm | `profile.id=1`, `location.id=1` | Tallinn (1), Kristiine (1) | 1 Akutrell A; 2 Redel U; 5 Muruniiduk A; 6 Survepesur A; 7 Matkatelk A |
| `app_user.id=3`, Liis Kask | `profile.id=3`, `location.id=3` | Tallinn (1), Mustamäe (2) | 3 Tolmuimeja A; 4 Hekikäärid A; 8 Projektor A |

Impordis on kõigil kaheksal tööriistal üks põhipilt. Pildita või profiilita olukorrad tuleb testida eraldatud testandmestikuga, mitte impordifaili muutes. `booking`, `category_image` ja `role` pole selle loendi andmeallikad.

## Veaolukorrad

PDF-is on „Veateated: —”, kuid päringul on filtreerimise ja lehekülgjaotuse sisendid. Allolevad 400 ja 500 vastused on taski tehnilised täpsustused. Veavastus järgib olemasolevat `ApiError` kuju: String-väljad `errorCode` ja `message`.

| Olukord | Status code | Response body |
|---|---|---|
| Arvuline parameeter on tühi, pole täisarv või ei mahu Integer vahemikku; näites `pageNumber`. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"pageNumber: peab olema Integer-tüüpi täisarv"}`; teise parameetri puhul kasutada selle nime. |
| `pageNumber < 1` või `pageSize < 1`. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"pageNumber: peab olema vähemalt 1"}` või sama teade väljaga `pageSize`. |
| ID-filter on negatiivne; näites `categoryId`. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"categoryId: peab olema 0 või positiivne täisarv"}`; kasutada vastava filtri nime. |
| `status` pole `A`, `U` ega `0`, sh tühi string. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"status: lubatud väärtused on A, U ja 0"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

Avalik päring ei nõua 401/403 vastuseid. Puuduv positiivne filtri-ID, omavahel sobimatud asukohafiltrid, tühi tulemus ja vahemikust väljas positiivne lehenumber ei ole 404 vead. Query teisendusvigade ja 500 JSON kuju tuleb teostamisel tagada; olemasolev request body väljade handler neid automaatselt ei kata. SQL-i ja stack trace'i ei väljastata.

## Vastuvõtu kriteeriumid

- [ ] `GET /api/tools` on avalik, ilma body ja path variable'ita; tagastab HTTP 200 ja `ToolsResponse`.
- [ ] Vaikeväärtused on `categoryId=0`, `cityId=0`, `districtId=0`, `status=A`, `pageNumber=1`, `pageSize=6`.
- [ ] ID-filtri `0` tähendab filtri puudumist; `status=0` lubab A/U tööriistad. Filtrid toimivad eraldi ja AND-kombinatsioonina.
- [ ] Tulemused on `tool.id ASC` järjekorras; lehenumber on API-s 1-põhine. Loendus vastab samadele filtritele nagu kaardipäring.
- [ ] Vastus sisaldab täpselt kirjeldatud metaandmeid ning tööriista seitset välja; kontakti-, Google- ja täpseid aadressiandmeid ei väljastata.
- [ ] Põhipilt on ainult `is_main=true` kirjest ja Base64 dekodeerimisel vastavad baidid andmebaasi väärtusele. Lisapilte ei valita asemele.
- [ ] Pildita tööriist säilib `imageData: null` väärtusega ning NULL-kirjeldus säilib JSON-is.
- [ ] Profiilita omaniku sobiv tööriist säilib ilma asukohafiltrita, asukohanimed on NULL. Positiivse asukohafiltri korral see ei sobitu.
- [ ] Tööriist ei kordu mitme pildi tõttu ning lehekülje koguarv loendab erinevaid tööriistu.
- [ ] Impordiandmetega vastavad vaikepäring, status=0, status=U ja filtrinäited eespool toodud kontrolltabelile; vaikimisi teisel lehel on ainult Projektor (8).
- [ ] Puuduv filtri-ID ja vastuoluline linn/linnaosa annavad tühja loendi. `totalElements=0` korral on `totalPages=0`.
- [ ] Viimasest lehest suurem positiivne lehenumber annab tühja `tools` loendi, säilitades tegeliku koguarvu ja lehtede arvu.
- [ ] Vigased sisendid annavad kirjeldatud 400 ja andmebaasi tõrge 500 `ApiError` vastuse.
- [ ] Automaattestid katavad avaliku ligipääsu, DTO/JSON kuju, kõik vaikeväärtused ja filtrid, järjekorra ning lehekülgjaotuse (sh viimane, tühi ja üle piiri leht).
- [ ] Andmebaasipõhised testid katavad põhipildi valiku mitme pildi seast, pildita tööriista, profiilita omaniku, erinevate linnade/linnaosade filtreerimise ning koguarvu õigsuse. Eraldi testid katavad 400/500 vastused.
- [ ] Päring ei muuda andmeid; SQL-i ega impordifaili ei muudeta testistsenaariumide loomiseks.
