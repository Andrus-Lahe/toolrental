# Oma tööriista lisamine

**Vaade:** `AddToolView.vue`, route `/tools/new`

**Roll:** Customer, sisse logitud ja täidetud profiiliga.

**Vaste balsamic mockupis:** AddToolView.vue, lehekülg 12/17 failis [Laenukas 2509.pdf](../../../balsamic/notes/Laenukas%202509.pdf).

![Mockup](./Tooriista-lisamine.png)

**Kasutaja kinnitatud täpsustus:** kollase märkuse saadavuse perioodi osa on aegunud. Alates/Kuni välju ega booking-kirjet ei looda; tööriista algstaatus on A. See asendab sama aegunud osa [AddToolView märkmetes](../../../balsamic/notes/AddToolView-markmed.md). API näite ownerId jäetakse request'ist välja, sest nii PDF-i teenuse selgitus kui OAuth leping määravad omaniku serveri sessioonist.

## Kasutajavoog

Kasutaja avab tööriista lisamise vormi, sisestab nime, valikulise kirjelduse ning valib kategooria. Soovi korral valib ta ühe pildi ja näeb selle eelvaadet ning failinime. Salvesta saadab POST /api/tools; 200 järel kuvatakse eduteade ja liigutakse Minu tööriistad vaatesse (taski tehniline ettepanek, PDF suunamist ei määra). Saadavuse kuupäevi ega broneeringut selles voos ei ole.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Logo ja navigatsioon | Ühine päis | Avaleht, Otsi tööriistu, Minu tööriistad, Sõnumid, Profiil, Logi välja; kasuta ühist autentimist. |
| Lisa uus tööriist | Pealkiri | Mockupi pealkiri. |
| Lisa pilt | Nupp ja failivalik | Üks valikuline fail; uus valik asendab varasema. |
| Pildi eelvaade | Pilt | Valitud fail, puudumisel mockupi kohatäitja. |
| Pildi nimi | Ainult loetav tekstiväli | Valitud faili nimi, serverile ei saadeta. |
| Tööriista nimi | Tekstiväli | Kohustuslik, mittetühi, kuni 150 märki. |
| Kirjeldus | Tekstiala | Valikuline, kuni 2000 märki. |
| Kategooria | Rippmenüü | Kohustuslik positiivne categoryId; „Vali kategooria“ pole kehtiv salvestusvalik. |
| Salvesta | Nupp | Keelatud saatmise ja pildi lugemise ajal ning kui kategooriad pole laaditud. |
| Vea-/eduteade | Alert | Backend message või üldine võrgu-/faililugemisviga. |

Pildi formaadi ja suuruse piirid ei ole lähteandmetes määratud; siduda need BE taskis nimetatud ühise kokkuleppega. Faililaiend või brauseri accept filter ei asenda serveri valideerimist.

## Käitumine ja valideerimine

1. Rakenda Customer sessiooni/profiili kontroll. hasProfile=false korral suuna ühisesse MyProfile voogu; FE kontroll ei asenda serveri õiguskontrolli.
2. Lae kategooriad beforeMount kaudu GET /api/categories teenusest, säilita API järjekord. Tühi loend tähendab, et kategooriat valida ega salvestada ei saa; päringu viga pole tühi loend.
3. Failivaliku tühistamisel säilita eelmine pilt. Uus valik asendab vana nime, eelvaate ja kodeeringu. Eira aegunud faililugemise vastust; vabasta kasutatud object URL-id vahetamisel ja komponendi eemaldamisel.
4. POST imageData peab olema Base64 ilma `data:image/...;base64,` prefiksita. Failinime, MIME-tüüpi ega ownerId-d ei lisata request'ile. Pildita saada imageData="". Kui pildi lugemine ebaõnnestub, kuva viga ja ära saada poolikut sisu.
5. Valideeri nimi, kirjelduse pikkus ja kategooria enne saatmist; sõnumite kuju vastab BE lepingule. Saada ainult categoryId, name, description, imageData. Rakenda ühine CSRF leping.
6. Saatmise ajal blokeeri korduv vajutus. 200 body on tühi: ära oota toolId-d ega suuna detailvaatesse oletatud ID abil. Eduka salvestuse järel Mine Minu tööriistad vaatesse ja kuva „Tööriist lisatud“ (tehniline ettepanek). Sihtvaate `/my-tools` on märkmetes kavandatud ja tuleb integreerimisel kinnitada.
7. 400/403/404/500 korral säilita sisestus ning kuva message. 404 kategooria puhul värskenda valikud. PROFILE_REQUIRED suunab profiili täitmise voogu; muu 403 ei tähenda sama äriviga.
8. Võrguvea korral ära eelda error.response olemasolu ega korda POST-i automaatselt: kadunud vastus ei tõenda salvestuse ebaõnnestumist. finally vabastab saatmisoleku.

## API kutsed

**Backend task:** [Tööriista lisamine](../../backend/AddToolView/Tooriista-lisamine.md). Controller/DTO realisatsioon puudub; alljärgnev on uue taski leping.

### `POST /api/tools`

Autenditud Customer, kelle profiil on täidetud. Path/query parameetrid puuduvad. `ToolCreateRequestDto.java` — request body:

```json
{
  "categoryId": 2,
  "name": "Akutrell",
  "description": "18 V akutrell, kaks akut ja laadija. Sobib puurimiseks ja kruvide keeramiseks.",
  "imageData": ""
}
```

Näide kasutab PDF-i tööriistaandmeid ja pildita loomise lubatud varianti. Pildiga variandis sisaldab imageData tegelike pildibaitide Base64 esitust ilma data-URL prefiksita; `BASE64-image-data` PDF-is on kohatäitja, mitte kehtiv kodeering.

| Väli | Java tüüp | Reegel |
|---|---|---|
| categoryId | Integer | Kohustuslik positiivne ja olemasolev kategooria ID. |
| name | String | Kohustuslik mittetühi nimi, kuni 150 märki. |
| description | String | Valikuline, kuni 2000 märki; puuduv/null lubatud. |
| imageData | String | Valikuline ühe pildi Base64; tühi, puuduv või null tähendab pildita tööriista (null/puudumine on tehniline täpsustus). |

**Response (200 OK):** tühi body, DTO-d ega uut toolId-d ei tagastata. ownerId, status, kuupäevad, pildi failinimi ja isMain ei kuulu request DTO-sse. Omanik määratakse sessiooni userId järgi, status serveris A. Pilt salvestatakse olemasolul is_main=true väärtusega.


**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | — | Tühi body | Ühine aegunud sessiooni käsitlus; vorm ei näita edukat salvestust. |
| 403 | — | Tühi body, roll pole customer või CSRF kontroll ebaõnnestus | Üldine ligipääsu/sessiooni viga; ära eelda profiili puudumist. |
| 400 | INCORRECT_INPUT | <väli>: <valideerimise teade> | Kuva message ja säilita sisestatud väärtused. |
| 400 | INCORRECT_INPUT | imageData: peab olema korrektne Base64 | Kuva message ja säilita sisestatud väärtused. |
| 403 | PROFILE_REQUIRED | Tööriista lisamiseks täida oma profiil. | Kuva message ja säilita sisestatud väärtused. |
| 404 | PRIMARY_KEY_NOT_FOUND | Ei leidnud primary keyd 'categoryId' väärtusega: 123 | Kuva message ja säilita sisestatud väärtused. |
| 500 | INTERNAL_SERVER_ERROR | Tööriista lisamine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message ja säilita sisestatud väärtused. |

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "<väli>: <valideerimise teade>"
}
```

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "imageData: peab olema korrektne Base64"
}
```

```json
{
  "errorCode": "PROFILE_REQUIRED",
  "message": "Tööriista lisamiseks täida oma profiil."
}
```

```json
{
  "errorCode": "PRIMARY_KEY_NOT_FOUND",
  "message": "Ei leidnud primary keyd 'categoryId' väärtusega: 123"
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Tööriista lisamine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

404 pärineb PDF-ist. Valideerimise ja 500 sõnastused on taski tehnilised täpsustused. PROFILE_REQUIRED konkretiseerib Google juhendi nõude, et tööriista ei lisata enne profiili täitmist. Role Customer tuleneb AddToolView märkmetest; Adminile ligipääsu laiendamist see task ei lisa. HTTP 403 äriviga eristatakse tühja body'ga autentimis-/CSRF veast.

### `GET /api/categories`

**Backend task:** [Kategooriate nimekirja päring](../../backend/ToolsView/Kategooriate-nimekirja-paring.md).

Teenusel puuduvad sisendid. Path variable'id, query parameetrid ja request body puuduvad. Päring on avalik: ToolsView märkmetes on lubatud Admin, Customer ja Külastaja.

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

PDF-is on „Veateated: —”. Allolev 500 on taski tehniline täpsustus. Sisendita avaliku loendi jaoks ei lisata 400/401/403/404 ärivigu.

| Olukord | Status code | Response body |
|---|---|---|
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

Kasuta olemasoleva `ApiError` kuju: `message` ja `errorCode` on String-väljad. Praegune `RestExceptionHandler` ei taga veel seda 500 lepingut. SQL-i ega stack trace'i kliendile ei tagastata.

500 INTERNAL_SERVER_ERROR korral kuva „Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti.“ ning võimalda korduslaadimine. Võrguvea korral kuva üldine laadimisviga.

## Komponendid ja failistruktuur

- `frontend/src/views/AddToolView.vue`: uus vaade, andmete laadimine ja POST käsitlemine.
- `frontend/src/components/forms/ToolCreateForm.vue`: vorm, props kategooriate/oleku jaoks, event-save.
- `frontend/src/components/common/ImagePicker.vue`: üks pilt, failinimi, eelvaade, event-image-selected; korduvkasuta, kui teise taskiga olemas.
- `frontend/src/api-services/ToolService.js`: lisa loomise POST otsingu GET kõrvale.
- `frontend/src/api-services/CategoryService.js`: taaskasuta olemasoleva taski kategooriate lihtloendi päringut.
- `frontend/src/navigation/NavigationService.js`: Minu tööriistad ja MyProfile suunamised.
- `frontend/src/router/index.js`: lisa /tools/new; erista staatilist new teed /tools/:toolId detailrajast.

Praegu puuduvad AddToolView, nimetatud teenused ja /tools/new; routeris ainult / ja /test. Järgi projekti Options API struktuuri, beforeMount, event- sündmusi ja .then()/.catch()/.finally() ahelaid eraldi handle-meetoditega. Vue faile taski koostamisel ei muudeta.

## Vastuvõtu kriteeriumid

- [ ] Vorm sisaldab mockupi välju ja ühte valikulist pilti; saadavuse kuupäevi pole.
- [ ] Kategooriad laetakse, tühi loend/viga on eristatavad ja ebasobiv valik peatab saatmise.
- [ ] Failinimi on ainult kuvamiseks, pildi vahetus/tühistamine/eelvaade ja faililugemisviga töötavad.
- [ ] POST ei sisalda ownerId-d ega failinime; imageData on prefiksita Base64 või tühi string.
- [ ] Nime 150/151 ja kirjelduse 2000/2001 piirid on kontrollitud.
- [ ] 200 tühja body järel ei loeta olematut DTO-d; toimub kokkulepitud suunamine ja eduteade.
- [ ] Ärivigade, sessiooni/CSRF vea ja võrguvea korral pole vale eduteadet ega automaatset kordus-POST-i.
- [ ] Customer/profiili kontroll ja /tools/new route töötavad; MyToolsView sõltuvus on enne integreeritud testi olemas.
- [ ] Pildiformaadi/mahu lahtine leping on enne pildi lõplikku teostust täpsustatud.
