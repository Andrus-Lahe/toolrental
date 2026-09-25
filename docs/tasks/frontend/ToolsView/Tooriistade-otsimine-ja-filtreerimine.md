# Tööriistade otsimine ja filtreerimine

**Vaade:** `ToolsView.vue`, route `/tools`

**Roll:** Kõik rollid (Admin, Customer, Külastaja)

**Vaste balsamic mockupis:** ToolsView.vue, lehekülg 3/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Tooriistade-otsimine-ja-filtreerimine.png`).

![Mockup](./Tooriistade-otsimine-ja-filtreerimine.png)

Täiendav allikas: [ToolsView-markmed.md](../../../balsamic/notes/ToolsView-markmed.md). Kõigi nelja teenuse backend taskid on olemas ja allpool viidatud; Controller/DTO realisatsioonid koodibaasis praegu puuduvad, seega kasutatakse taskide lepinguid.

## Kasutajavoog

Kasutaja avab tööriistade otsingu otse või avalehe kategoorialt ning näeb lehekülgedena tööriistakaarte. Ta valib kategooria, linna ja vajadusel selle linnaosa, rakendab filtrid või tühistab need. Lehenumbri või eelmise/järgmise lehe valik laadib samade rakendatud filtritega uue lehe, „Vaata detaile“ avab valitud tööriista detailvaate. Sisselogimine kasutab ühist autentimisvoogu; broneerimine, detailvaate teostus ja hinnainfo lisamine jäävad selle taski skoobist välja.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Logo ja päise lingid | Ühine navigatsioon | Avaleht, Otsi tööriistu, Minu tööriistad, Sõnumid, Profiil; kasuta ühiseid navigatsioonireegleid. |
| Logi sisse / Registreeru | Päise nupp | Külastajale ühise Google autentimise avamine; sisselogimise API ei kuulu otsinguteenustesse. |
| Otsing | Pealkiri | Lehe pealkiri. |
| Filtreeri | Külgpaneel | Mockupil kaartidest vasakul. |
| Kategooria | Rippmenüü | Valikuline categoryId; „Vali kategooria“/kõik valik vastab 0-le. |
| Linn | Rippmenüü | Valikuline cityId; „Vali linn“/kõik valik vastab 0-le. |
| Linnaosa | Rippmenüü | Valikuline districtId; valikud tulevad valitud linna järgi. Linna puudumisel ja laadimise ajal keelatud. |
| Rakenda / Tühista filtrid | Filtrite tegevused | Mockupi ühine silt teostatakse kahe selgelt eristatava tegevusena (UI täpsustus): rakendamine ja lähtestamine. |
| Tööriistakaardid | Kaardivõrgustik | Mockupil 3 veergu ja 2 rida, vaikimisi kuni 6 kaarti lehel; kitsal ekraanil vähem veerge. |
| Pilt | Kaardi pilt | imageData Base64; puudumisel või kuvamisveal kohatäitja, alt-tekstiks tööriista nimi. |
| Nimi ja kirjeldus | Kaardi tekst | toolName ja description; null-kirjeldust ei näidata tekstina „null“. |
| Hind: 5 €/päev | Mockupi tekst | DTO-s ega andmebaasis hinnavälja pole. Hinda ei kuvata väljamõeldud või fikseeritud väärtusena; hinnainfo vajab eraldi lepingut. |
| Vaata detaile | Kaardi nupp/link | Suunab `/tools/<toolId>` aadressile. |
| Eelmine, lehenumbrid, Järgmine | Paginatsioon | Lähtub API pageNumber ja totalPages väärtustest, mitte mockupi fikseeritud numbritest 1–5. |
| Laadimise-, vea- ja tühja loendi teated | Olekud | Tööriistadel ja filtrite viiteloenditel eraldi olekud; error ei ole tühi tulemus. |

`status`, `cityName` ja `districtName` kuuluvad API-sse, kuid eraldi staatusefiltrit ega asukohaveerge wireframe ei näita. Task ei lisa neid juhtnuppe. Brauseri raam, märkmelehed ja Balsamiqi vesimärk ei kuulu rakenduse UI-sse.

## Käitumine ja valideerimine

1. Alusta andmete laadimist `beforeMount` kaudu. Kategooriate, linnade ja tööriistade algpäringud võivad käia sõltumatult. Vaade ja neli GET endpoint'i on avalikud: ToolsView avamine ega detaililingile vajutamine ei nõua selle taski järgi Google modaali.
2. Vaikimisi kasuta categoryId=0, cityId=0, districtId=0, status=A, pageNumber=1 ja pageSize=6. `/tools?categoryId=1` peab juba esimeses tööriistapäringus rakendama kategooria 1 ning näitama seda pärast valikute laadimist rippmenüüs. Ära tee enne seda tarbetut filtreerimata päringut.
3. Hoia vormis valitavad filtrid ja viimati rakendatud filtrid eraldi. Rippmenüü muutmine ei lae kohe uusi tööriistu; „Rakenda“ rakendab valikud ja alustab lehelt 1. Lehevahetus kasutab rakendatud filtreid, mitte pooleliolevaid valikuid.
4. Linna vahetamisel nulli kohe districtId ja vana linnaosaloend. Positiivse cityId puhul küsi linnaosad; cityId=0 korral seda GET-i ei tehta. Kui linn muutub päringu kestel, eira vana linna vastust. Tühi edukas linnaosaloend pole 404.
5. „Tühista filtrid“ lähtestab kõik kolm ID-filtrit 0-le, tühjendab linnaosad, seab pageNumber=1 ning laadib tulemuse status=A ja pageSize=6 väärtustega. Avalehe categoryId query ei tohi lähtestamisel filtrit kohe taastada: eemalda see router query'st.
6. Toeta categoryId ja pageNumber route query muutusi ka sama komponendi kasutamisel (nt HomeView link või brauseri tagasi/edasi). Vajadusel sünkrooni valitud linn/linnaosa samasse URL-i kui tehniline täpsustus; väldi watch'i ja käsitsi laadimise topeltpäringuid. Üksik districtId ilma cityId-ta on backendis lubatud, kuid seda erijuhtu ei pea linna valimist nõudev vorm looma.
7. Ära saada tühje stringe: puuduv ID-filter saadetakse 0-na või jäetakse välja. Arvud peavad olema Java Integer vahemikus; ID-d vähemalt 0 ja lehenumber/lehesuurus vähemalt 1. Vigane toetatud URL-parameeter annab nähtava sisendivea ning lähtestamise võimaluse, mitte vaikse teise otsingu. Positiivne olematu kategooria ei asendu vaikselt filtrita otsinguga: säilita päringu filter, näita puuduvat valikut ja võimalda lähtestada.
8. Eelmine on esimesel lehel keelatud, Järgmine viimasel ning mõlemad totalPages=0 korral. Lehenumbrid pärinevad tegelikust totalPages väärtusest. Positiivne üle viimase lehe number võib API-st anda tühja tools massiivi koos mittenull metaandmetega: kuva selle lehe tühi olek ja võimalda minna olemasolevale lehele, ära väida kogu otsingut tühjaks.
9. Hoia päringute laadimisolekud eraldi ja ära lase vanal tööriistavastusel uuema filtri/lehe tulemust üle kirjutada. Kasuta päringu identifikaatorit või katkestamist; ignoreeritud vana päring ei tohi ka uuema laadimisolekut lõpetada.
10. Null-pilt ja null-asukoht ei tohi kaarti eemaldada. Base64-l pole data-URI prefiksit; impordipildid on SVG ja neid saab kuvada image/svg+xml data-URL kaudu, kuid muude pildiformaatide MIME-leping on backend taskis lahtine. Ära sisesta SVG teksti v-html kaudu. Vigase pildi korral jääb nimi ja detaililink kasutatavaks.
11. HTTP 400/404/500 korral kuva vastava päringu message, lõpeta laadimine `.finally()` kaudu ja võimalda korduskatset. Viiteloendi tõrge ei kustuta juba edukalt saadud tööriistakaarte. Vea ja vana tulemuse koos kuvamisel märgista tulemus aegunuks, mitte uue päringu eduks.
12. „Vaata detaile“ kasutab kaardi toolId-d ja navigeerib detailvaatesse; selles taskis ei tehta detail- ega broneerimise POST päringuid.

**Erinevused mockupist:** backend lepingu vaikimisi status=A annab impordiandmetel 7 tulemust, mitte pildi näites 8; 8 tulemust vastab status=0 päringule. Näidishinnale puudub leping. HomeView eraldi sisselogimise kontroll ei muuda ToolsView enda avalikku rollilepingut.

## API kutsed

Kõigil neljal päringul puudub request body. Alljärgnevad sisendid, JSON näited ja veateated pärinevad backend taskidest täies mahus. Tööriistaloendi status=U/0 ja pageSize tugi ei tähenda nendele eraldi UI juhtnuppude lisamist.

### `GET /api/tools`

**Backend task:** [Tooriistade-nimekirja-paring](../../backend/ToolsView/Tooriistade-nimekirja-paring.md).

**Sisend:**

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

**Väljund:**

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

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 400 | INCORRECT_INPUT | pageNumber: peab olema Integer-tüüpi täisarv | Kuva sisendiviga; ära näita tühja otsingutulemusena. |
| 400 | INCORRECT_INPUT | pageNumber: peab olema vähemalt 1 | Kuva sisendiviga; ära näita tühja otsingutulemusena. |
| 400 | INCORRECT_INPUT | categoryId: peab olema 0 või positiivne täisarv | Kuva sisendiviga; ära näita tühja otsingutulemusena. |
| 400 | INCORRECT_INPUT | status: lubatud väärtused on A, U ja 0 | Kuva sisendiviga; ära näita tühja otsingutulemusena. |
| 500 | INTERNAL_SERVER_ERROR | Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message vastava päringu veaolekus; võimalda korduslaadimist. |

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "pageNumber: peab olema Integer-tüüpi täisarv"
}
```

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "pageNumber: peab olema vähemalt 1"
}
```

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "categoryId: peab olema 0 või positiivne täisarv"
}
```

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "status: lubatud väärtused on A, U ja 0"
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

Arvuliste parameetrite veateates asendub näidisvälja nimi tegeliku vigase parameetri nimega. Võrguvea korral võib HTTP vastus puududa: kuva üldine laadimisviga, ära loe kontrollimata `error.response.data` väärtust.

### `GET /api/categories`

**Backend task:** [Kategooriate-nimekirja-paring](../../backend/ToolsView/Kategooriate-nimekirja-paring.md).

**Sisend:**

Teenusel puuduvad sisendid. Path variable'id, query parameetrid ja request body puuduvad. Päring on avalik: ToolsView märkmetes on lubatud Admin, Customer ja Külastaja.

**Väljund:**

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

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 500 | INTERNAL_SERVER_ERROR | Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message vastava päringu veaolekus; võimalda korduslaadimist. |

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

Arvuliste parameetrite veateates asendub näidisvälja nimi tegeliku vigase parameetri nimega. Võrguvea korral võib HTTP vastus puududa: kuva üldine laadimisviga, ära loe kontrollimata `error.response.data` väärtust.

### `GET /api/cities`

**Backend task:** [Linnade-nimekirja-paring](../../backend/ToolsView/Linnade-nimekirja-paring.md).

**Sisend:**

Teenusel puuduvad sisendid. Path variable'id, query parameetrid ja request body puuduvad. Päring on avalik: ToolsView märkmetes on lubatud Admin, Customer ja Külastaja.

**Väljund:**

**Response (200 OK):** `List<CityDto>`, JSON massiiv kõigist viiteandmete kirjetest.

```json
[
  {
    "cityId": 1,
    "cityName": "Tallinn"
  },
  {
    "cityId": 2,
    "cityName": "Tartu"
  },
  {
    "cityId": 3,
    "cityName": "Pärnu"
  }
]
```

`CityDto.java` sisaldab ainult `Integer cityId` ja `String cityName`. Need vastavad tabeli `city` väljadele `id` ja `city_name`. Lehekülgjaotust pole.

PDF-is on loendi esimene element ja jätkumist tähistav `...`; siin on täielik vastus [3_import.sql](../../../database/3_import.sql) andmetega. Järjestus on `city.id ASC`. PDF ei määra sortimist; ID-järjestus on taski tehniline täpsustus korratavaks vastuseks.

Tühja tabeli korral tagastada HTTP 200 ja `[]`, mitte `null` või 404. Andmed loetakse andmebaasist, mitte koodi kirjutatud loendist.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 500 | INTERNAL_SERVER_ERROR | Linnade laadimine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message vastava päringu veaolekus; võimalda korduslaadimist. |

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Linnade laadimine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

Arvuliste parameetrite veateates asendub näidisvälja nimi tegeliku vigase parameetri nimega. Võrguvea korral võib HTTP vastus puududa: kuva üldine laadimisviga, ära loe kontrollimata `error.response.data` väärtust.

### `GET /api/cities/{cityId}/districts`

**Backend task:** [Valitud-linna-linnaosade-paring](../../backend/ToolsView/Valitud-linna-linnaosade-paring.md).

**Sisend:**

| Parameeter | Asukoht | Java tüüp | Kohustuslik | Tähendus |
|---|---|---|---|---|
| `cityId` | Path variable | `Integer` | Jah | Valitud linna `city.id`. |

Query parameetrid ja request body puuduvad. Päring on avalik kõigile ToolsView rollidele, sh külalisele. Näide: `GET /api/cities/1/districts`.

`cityId` peab olema Java Integer-iks teisendatav. See on konkreetse linna ID: erinevalt tööriistade otsingu valikulisest filtrist ei tähenda siin `0` filtri ignoreerimist. Kui vastavat linna pole, tagastada 404.

**Väljund:**

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

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 404 | PRIMARY_KEY_NOT_FOUND | Ei leidnud primary keyd 'cityId' väärtusega: 123 | Kuva puuduva linna viga, tühjenda linnaosaloend ja keela selle valik. |
| 400 | INCORRECT_INPUT | cityId: peab olema Integer-tüüpi täisarv | Kuva sisendiviga; ära näita tühja otsingutulemusena. |
| 500 | INTERNAL_SERVER_ERROR | Linnaosade laadimine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message vastava päringu veaolekus; võimalda korduslaadimist. |

```json
{
  "errorCode": "PRIMARY_KEY_NOT_FOUND",
  "message": "Ei leidnud primary keyd 'cityId' väärtusega: 123"
}
```

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "cityId: peab olema Integer-tüüpi täisarv"
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Linnaosade laadimine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

Arvuliste parameetrite veateates asendub näidisvälja nimi tegeliku vigase parameetri nimega. Võrguvea korral võib HTTP vastus puududa: kuva üldine laadimisviga, ära loe kontrollimata `error.response.data` väärtust.

## Komponendid ja failistruktuur

| Fail | Vastutus |
|---|---|
| `frontend/src/views/ToolsView.vue` | Uus vaade, route'i algväärtused, eraldi vormi/rakendatud filtrid, päringud ja navigeerimine. |
| `frontend/src/components/forms/ToolsFilterForm.vue` | Kolm rippmenüüd ja rakendamise/lähtestamise tegevused; props valikutele ja olekutele, event-apply-filters / event-reset-filters / event-city-changed. |
| `frontend/src/components/common/ToolCard.vue` | Tööriista pilt, nimi, kirjeldus ja event-view-details toolId-ga. |
| `frontend/src/components/common/Pagination.vue` | Lehenumbrid ja eelmine/järgmine, event-page-changed. |
| `frontend/src/components/common/AlertDanger.vue` | Veateadete kuvamine tekstina; kasuta ühist komponenti, kui see on teise taskiga loodud. |
| `frontend/src/api-services/ToolService.js` | GET /api/tools, parameetrid Axiose params kaudu. |
| `frontend/src/api-services/CategoryService.js` | GET /api/categories; eraldi HomeView detailsest kategooriapäringust. |
| `frontend/src/api-services/CityService.js` | GET /api/cities ja GET /api/cities/{cityId}/districts. |
| `frontend/src/navigation/NavigationService.js` | Detailvaatesse navigeerimine. |
| `frontend/src/router/index.js` | Lisa `/tools`; `/tools/:toolId` detailvaade on integratsiooni sõltuvus. |

Praegu puuduvad ToolsView, detailvaade, nimetatud teenused ja `/tools` route; router sisaldab ainult `/` ning `/test`. Kavandatud failid sobita teiste taskidega, ära dubleeri nende ühiskomponente. Järgi `docs/frontend/projekti-struktuur.md` ja `docs/frontend/vue-komponendi-struktuur.md`: Options API järjekord `name`, `components`, `props`, `emits`, `data`, `computed`, `methods`, `beforeMount`; päringud `.then()` / `.catch()` / `.finally()` ning eraldi handle-meetodid.

## Vastuvõtu kriteeriumid

- [ ] ToolsView avaneb rajal `/tools` nii külastajale kui Customerile ja Adminile.
- [ ] Mockupi päis, kolm filtrit, rakendamine/lähtestamine, kaardid ja paginatsioon on olemas; fiktiivset hinda ei kuvata.
- [ ] HomeView categoryId query rakendub esimeses päringus ja rippmenüüs; lähtestamine eemaldab selle mõju ka URL-ist.
- [ ] Linna muutmine nullib linnaosa, cityId=0 ei kutsu districts endpoint'i ning vana vastus ei kirjuta uue linna valikuid üle.
- [ ] Rakendamine alustab lehelt 1, lehevahetus säilitab rakendatud filtrid, lähtestamine taastab status=A ja pageSize=6 vaikimisi otsingu.
- [ ] Vaikimisi impordi esimene leht sisaldab toolId-sid 1, 3, 4, 5, 6, 7 ning teine leht 8; metaandmed on 7 tulemust ja 2 lehte.
- [ ] cityId=2 otsing annab korrektse tühja tulemuse; olematu positiivne ID ja üle viimase lehe number ei tekita väljamõeldud 404 viga.
- [ ] Paginatsioon kasutab vastuse metaandmeid ja keelab piiril mittesobivad tegevused.
- [ ] Null-kirjeldus, puuduv/vigane pilt ja profiilita omaniku null-asukoht ei lõhu kaarti.
- [ ] Kõigi nelja päringu vead ja tühjad vastused on eristatud; võrguvea käsitlus ei eelda error.response olemasolu.
- [ ] Kiire filtri-/lehevahetus ei lase vanal vastusel uut tulemust asendada; route query muutus ei tekita dubleerivaid päringuid.
- [ ] Vaata detaile navigeerib valitud toolId-ga detailvaatesse, mille route on integratsiooniks loodud.
- [ ] Kontrollitud on avalik alglaadimine, kategoorialink, seotud rippmenüüd, filtreerimine, lähtestamine, lehevahetus, null-väljad, sisendi- ja võrguvead.

Lahtised detailid: üldise pildiformaadi MIME-leping ning mockupi hinnainfo pole backendis määratud. Need on eraldi täpsustused; käesolev task ei lisa API välju ega muuda backend taskide lepingut.
