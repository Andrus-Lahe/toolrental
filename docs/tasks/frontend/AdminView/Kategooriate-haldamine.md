# Kategooriate haldamine

**Vaade:** `AdminView.vue`, route `/admin`

**Roll:** Admin

**Vaste balsamic mockupis:** AdminView.vue, lehekülg 13/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Kategooriate-haldamine.png`).

![Mockup](./Kategooriate-haldamine.png)

Allikad: [AdminView märkmed](../../../balsamic/notes/AdminView-markmed.md) ja allpool viidatud backend taskid. Märkmetes dokumenteeritud täpsustused (eemaldatud „Lisa kasutaja“, kategooria modaalid ilma pildihalduseta) täiendavad algset wireframe'i.

## Kasutajavoog

Admin näeb Haldus lehe kategooriatabelit ja avab lisamiseks tühja või muutmiseks olemasolevate väärtustega modaali. Ta täidab nime, valikulise kirjelduse ja järjekorra ning salvestab; edu järel modaal sulgub ja kategoorialoend laaditakse uuesti. Kustutamine nõuab kinnitust ja seotud tööriistadega kategooria jääb veateatega alles. Kasutajate haldus on [eraldi FE taskis](./Kasutajate-haldamine.md); kategooriapiltide haldust selles versioonis ei ole.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Haldus ja ühine päis | AdminView raam | Sama päis ja admini rollikaitse nagu kasutajate taskis. |
| Kategooriad | Jaotise pealkiri | Kasutajate jaotise all vastavalt mockupile. |
| Lisa kategooria | Nupp | Avab tühja modaali, olek create. |
| Kategooria nimi | Tabeliveerg | categoryName API-st; „Muud“ jääb andmebaasi nimeks. |
| Tegevus: Muuda | Rea nupp | Avab valitud kategooria andmetega modaali, olek edit. |
| Tegevus: Kustuta | Rea nupp | Küsib kategooria nimega kinnitust. |
| Nimi | Modaali tekstiväli | categoryName, kohustuslik, mittetühi, kuni 100 märki. |
| Kirjeldus | Modaali tekstiala | description, valikuline, kuni 255 märki, võib olla null. |
| Järjekord | Modaali arvuväli | sequence, kohustuslik Integer. |
| Salvesta | Modaali nupp | Valideerib, saadab POST või PUT; saatmisel keelatud. |
| Tühista / sulgemine | Modaali tegevus | Enne saatmist sulgeb muutusi salvestamata. |
| Kustutamise kinnitus | Modaal | Kinnita saadab DELETE, Tühista/sulgemine ei saada päringut. |
| AlertDanger | Veateade | Vormis/jaotises nähtav backend message. |
| Laadimine / tühi loend | Olek | Tühja 200 korral „Kategooriaid ei ole“; laadimisviga on eraldi. |

Tabelile ei lisata kirjelduse ega järjekorra veerge: need on muutmise modaali väljad. Modaalide täpne kujundus ei ole PDF-is joonistatud; väljad ja käitumine tulevad AdminView märkmetest.

## Käitumine ja valideerimine

1. AdminView avamisel lae admini kategoorialoend `beforeMount` kaudu. Kasuta GET /api/admin/categories, sest avalik GET /api/categories ei sisalda kõiki muutmiseks vajalikke välju. Hoia vastuse sequence-järjestus; tühi massiiv on korrektne tulemus.
2. Lisamisel tühjenda varasema modaali väärtused ja vead. Muutmisel kopeeri categoryName, description ja sequence valitud reast eraldi vormiobjekti, säilitades categoryId; props/tabelirida ei muudeta enne serveri vastust.
3. Kontrolli enne POST/PUT päringut mittetühja (mitte ainult tühikutest) nime pikkusega kuni 100, kirjeldust kuni 255 ning kohustuslikku täisarvulist sequence väärtust Java Integer vahemikus. Ära kasuta kontrolli `!sequence`, mis keelaks nulli asemel ka arvu 0. Backend ei nõua positiivset ega unikaalset sequence'i: 0 ja negatiivsed täisarvud on lubatud.
4. Saada sequence JSON arvuna, mitte stringina. Puuduv kirjeldus võib olla null; nime automaatset tõstu muutmist ega erinevat unikaalsusreeglit ei lisata. Duplikaadi otsustab backend täpse tõstutundliku võrdlusega; muutmata oma nimi on lubatud.
5. Lisa saadab POST, Muuda saadab PUT koos rea categoryId-ga; PUT saadab kõik kolm välja, sealhulgas null-kirjelduse selle eemaldamisel. Server ei tagasta uue kirje DTO-d: 200 body on tühi.
6. Edukal salvestamisel sulge modaal ja värskenda kategoorialoendit. Vea korral hoia modaal ning väärtused alles ja kuva message. Saatmise ajal keela korduv salvestamine ja sulgemine. Loendi värskendamise tõrge pärast edukat salvestust ei tähenda salvestamise ebaõnnestumist: korda ainult GET-i.
7. Kustutamise kinnitamisel saada valitud ID, mitte nimi. Edukal 200 lae loend uuesti; CATEGORY_IN_USE korral kuva message ja säilita rida. Tööriistade olemasolu ei saa AdminCategoryDto-st ette kindlaks teha, seega serveri otsus jääb määravaks.
8. Erista ärilist 403 CATEGORY_UNAVAILABLE/CATEGORY_IN_USE tühja body'ga ligipääsu 403-st. 401 korral kasuta ühise sessiooni käsitlust. Võrguvea puhul ära eelda error.response olemasolu ega korda muutvat päringut automaatselt.

## API kutsed

### `GET /api/admin/categories`

**Backend task:** [Admini-kategooriate-nimekirja-paring](../../backend/AdminView/Admini-kategooriate-nimekirja-paring.md).

**Sisend:**

Teenusel puuduvad sisendid: path variable'id, query parameetrid ja request body puuduvad.

Teenus on ainult adminile (`/api/admin/**`, `hasRole("admin")`).

**Väljund:**

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

See teenus on eraldi avalikust `GET /api/categories` teenusest (vt [Kategooriate nimekirja päring](../../backend/ToolsView/Kategooriate-nimekirja-paring.md)). Avalik teenus tagastab ainult `categoryId` ja `categoryName`. Admin vajab ka `description` ja `sequence` välju, et modaalaken „Muuda“ saaks need ette täita.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | — | Tühi body | Käivita ühine sisselogimise käsitlus ja peata admini tegevused. |
| 403 | — | Tühi body | Kuva ligipääsu puudumine ja peata admini tegevused. |
| 500 | INTERNAL_SERVER_ERROR | Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |

Veavastuste JSON näited (sõnasõnalt backend taski lepingust):

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

### `POST /api/admin/categories`

**Backend task:** [Kategooria-lisamine](../../backend/AdminView/Kategooria-lisamine.md).

**Sisend:**

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

**Väljund:**

**Response (200 OK):** tühi body (`Response (200): NONE`).

Tabelisse `category` lisatakse uus rida. Kategooria pilti (`category_image`) selles versioonis ei lisata, seega on uuel kategoorial pilt puudu.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | — | Tühi body | Käivita ühine sisselogimise käsitlus ja peata admini tegevused. |
| 403 | — | Tühi body | Kuva ligipääsu puudumine ja peata admini tegevused. |
| 403 | CATEGORY_UNAVAILABLE | Sellise nimega kategooria on juba olemas | Hoia modaal ja sisestatud väärtused alles, kuva message. |
| 400 | INCORRECT_INPUT | <väli>: <valideerimise teade> | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |
| 500 | INTERNAL_SERVER_ERROR | Kategooria lisamine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |

Veavastuste JSON näited (sõnasõnalt backend taski lepingust):

```json
{
  "errorCode": "CATEGORY_UNAVAILABLE",
  "message": "Sellise nimega kategooria on juba olemas"
}
```

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "<väli>: <valideerimise teade>"
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Kategooria lisamine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

### `PUT /api/admin/categories/{categoryId}`

**Backend task:** [Kategooria-muutmine](../../backend/AdminView/Kategooria-muutmine.md).

**Sisend:**

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

**Väljund:**

**Response (200 OK):** tühi body (`Response (200): NONE`).

Kõik kolm välja kirjutatakse üle (PUT asendab kogu kategooria sisu). Kui `description` on `null`, saab andmebaasis kirjeldus väärtuse `NULL`. Kategooria pilt (`category_image`) ja kategooria tööriistad ei muutu.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | — | Tühi body | Käivita ühine sisselogimise käsitlus ja peata admini tegevused. |
| 403 | — | Tühi body | Kuva ligipääsu puudumine ja peata admini tegevused. |
| 404 | PRIMARY_KEY_NOT_FOUND | Ei leidnud primary keyd 'categoryId' väärtusega: 123 | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |
| 403 | CATEGORY_UNAVAILABLE | Sellise nimega kategooria on juba olemas | Hoia modaal ja sisestatud väärtused alles, kuva message. |
| 400 | INCORRECT_INPUT | <väli>: <valideerimise teade> | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |
| 500 | INTERNAL_SERVER_ERROR | Kategooria muutmine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |

Veavastuste JSON näited (sõnasõnalt backend taski lepingust):

```json
{
  "errorCode": "PRIMARY_KEY_NOT_FOUND",
  "message": "Ei leidnud primary keyd 'categoryId' väärtusega: 123"
}
```

```json
{
  "errorCode": "CATEGORY_UNAVAILABLE",
  "message": "Sellise nimega kategooria on juba olemas"
}
```

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "<väli>: <valideerimise teade>"
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Kategooria muutmine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

### `DELETE /api/admin/categories/{categoryId}`

**Backend task:** [Kategooria-kustutamine](../../backend/AdminView/Kategooria-kustutamine.md).

**Sisend:**

| Parameeter | Asukoht | Java tüüp | Kohustuslik | Tähendus |
|---|---|---|---|---|
| `categoryId` | Path variable | `Integer` | Jah | Kustutatava kategooria `category.id`. |

Query parameetrid ja request body puuduvad. Näide: `DELETE /api/admin/categories/4`.

Teenus on ainult adminile (`/api/admin/**`, `hasRole("admin")`).

**Väljund:**

**Response (200 OK):** tühi body (`Response (200): NONE`).

Kategooria kustutatakse füüsiliselt ühes transaktsioonis (`@Transactional`):

1. Kustutatakse kategooria pilt (`category_image`), kui see on olemas.
2. Kustutatakse `category` rida.

Kustutamine on lubatud ainult siis, kui kategoorias pole ühtegi tööriista (`tool.category_id`). Tööriistade staatust (`A`/`U`) ei arvestata: iga seotud tööriist takistab kustutamist.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | — | Tühi body | Käivita ühine sisselogimise käsitlus ja peata admini tegevused. |
| 403 | — | Tühi body | Kuva ligipääsu puudumine ja peata admini tegevused. |
| 404 | PRIMARY_KEY_NOT_FOUND | Ei leidnud primary keyd 'categoryId' väärtusega: 123 | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |
| 403 | CATEGORY_IN_USE | Kategooriat ei saa kustutada, sest sellel on tööriistu | Kuva message; kategooria jääb tabelisse. |
| 500 | INTERNAL_SERVER_ERROR | Kategooria kustutamine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |

Veavastuste JSON näited (sõnasõnalt backend taski lepingust):

```json
{
  "errorCode": "PRIMARY_KEY_NOT_FOUND",
  "message": "Ei leidnud primary keyd 'categoryId' väärtusega: 123"
}
```

```json
{
  "errorCode": "CATEGORY_IN_USE",
  "message": "Kategooriat ei saa kustutada, sest sellel on tööriistu"
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Kategooria kustutamine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

## Komponendid ja failistruktuur

Vaade ja `/admin` route praegu puuduvad; olemas on ainult HomeView ja TestView. Backendis puuduvad selle vaate Controller/DTO realisatsioonid, seega API leping pärineb kasutaja antud backend taskidest. Need kirjeldavad soovitud käitumist, mitte juba töötavat API-t. Järgi `docs/frontend/projekti-struktuur.md` ning `docs/frontend/vue-komponendi-struktuur.md`: Options API, `beforeMount` andmete laadimiseks, `.then()` / `.catch()` / `.finally()` ja eraldi `handle...` meetodid; sündmustel `event-` eesliide.

Mõlemad AdminView taskid täiendavad ühte vaadet, mitte ei loo kahte `/admin` route'i. Ühine päis, rollikaitse, veakomponent ja kinnitamise modaal teostatakse üks kord. `/admin` ligipääs ja „Haldus“ link lubatakse ainult `roleName === 'admin'` korral; serveri rollikaitse jääb määravaks. Autentimine ja väljalogimine kasutavad olemasolevat ühise autentimise ülesannet. Muutvad päringud peavad järgima selle sessiooni/CSRF lepingut.

| Fail | Vastutus |
|---|---|
| `frontend/src/views/AdminView.vue` | Ühine vaade, loendi laadimine ja kategooriate tegevuste koordineerimine. |
| `frontend/src/components/tables/AdminCategoriesTable.vue` | Kategooriaread, event-edit-category / event-delete-category. |
| `frontend/src/components/modals/CategoryModal.vue` | Ühine lisamise/muutmise modaal, event-save / event-cancel. |
| `frontend/src/components/forms/CategoryForm.vue` | Kolm välja, valideerimine ja eraldi kopeeritud vormiandmed. |
| `frontend/src/components/modals/ConfirmDeleteModal.vue` | Kasutajate taskiga ühine kustutamise kinnitus. |
| `frontend/src/components/common/AlertDanger.vue` | Backend message'i turvaline kuvamine tekstina. |
| `frontend/src/api-services/AdminCategoryService.js` | GET, POST, PUT ja DELETE admini kategooriate jaoks. |

Kõik nimetatud komponendid/teenused praegu puuduvad. Kategooria pildi puudumine pärast lisamist on backend taski kirjeldatud käitumine, mitte põhjus lisada siia pildi üleslaadimist.

## Vastuvõtu kriteeriumid

- [ ] AdminView sisaldab mockupi kategooriate tabelit, Lisa kategooria, Muuda ja Kustuta tegevusi; pildihaldust pole.
- [ ] Kasutatakse admini detailset loendit ja selle järjestust; tühi loend ning null-kirjeldus on toetatud.
- [ ] Lisamise modaal on tühi, muutmise modaal eeltäidetud; tühistamine ei muuda tabelit ega saada päringut.
- [ ] Kontrollitud on nimi 100/101, kirjeldus 255/256, tühi nimi ja puuduv/murdarvuline sequence; 0 ja negatiivne täisarv on lubatud.
- [ ] POST/PUT saadavad kolm õiget välja ja arvulise sequence; null-kirjeldus saab olemasoleva kirjelduse eemaldada.
- [ ] 200 tühi body sulgeb salvestamise modaali ja käivitab GET värskenduse; viga säilitab vormiandmed.
- [ ] Sama nimega muutmine on lubatud; duplikaatnime CATEGORY_UNAVAILABLE kuvatakse täpse message'iga.
- [ ] DELETE nõuab kinnitust, CATEGORY_IN_USE säilitab rea, 404 ja muud kirjeldatud vead kuvatakse.
- [ ] Korduv vajutus, vastuseta võrguviga ja eduka mutatsiooni järgne GET viga on kontrollitud.
- [ ] Eduka kustutamise kontroll kasutab tööriistadeta testikategooriat; kõik neli impordikategooriat annavad CATEGORY_IN_USE.
- [ ] Mõlemad FE taskid on ühendatud üheks `/admin` vaateks, millele mitte-admin ligi ei pääse.
