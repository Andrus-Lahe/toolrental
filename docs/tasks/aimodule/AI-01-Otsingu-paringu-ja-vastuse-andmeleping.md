# AI-01 — Otsingu päringu ja vastuse andmeleping

**Teenus:** `POST /api/ai/ask` (olemasolev endpoint)

## Eesmärk

Määrata AI tööriistaotsingu püsiv API leping enne NL→SQL lahenduse teostamist. Kasutaja saadab eestikeelse loomuliku keele küsimuse ning saab vastu tööriistade struktureeritud loendi. Igal tulemusel on tööriista ID, nimi, kategooria ja asukoht; `toolId` võimaldab kasutajaliidesel teha tööriista nimest lingi detailvaatele.

See task määrab ainult päringu ja vastuse kuju. NL→SQL prompt, andmevaade, saadavuse päring, SQL-i kontroll, tulemuste käivitamine ja frontendi lingi teostus kuuluvad järgmistesse taskidesse.

## Sisend

Endpoint võtab vastu JSON request body:

```json
{
  "question": "Millised tööriistad on saadaval Kristiines?"
}
```

| Väli | Tüüp | Kohustuslik | Piirang | Tähendus |
|---|---|---:|---|---|
| `question` | `String` | Jah | Ei tohi olla tühi ega koosneda ainult tühikutest; maksimaalselt 500 märki | Kasutaja loomulikus keeles tööriistaotsingu küsimus. |

Olemasolev päring on sisselogimist nõudev. See task ei muuda autentimise lepingut.

## Väljund

**Response (200 OK):** JSON objekt väljaga `tools`. Iga tööriista kirje sisaldab ainult otsingutulemuse kuvamiseks vajalikke välju.

```json
{
  "tools": [
    {
      "toolId": 1,
      "name": "Akutrell",
      "category": "Ehitustööd",
      "location": "Kristiine"
    },
    {
      "toolId": 5,
      "name": "Muruniiduk",
      "category": "Aiatööd",
      "location": "Kristiine"
    },
    {
      "toolId": 6,
      "name": "Survepesur",
      "category": "Koristamine",
      "location": "Kristiine"
    },
    {
      "toolId": 7,
      "name": "Matkatelk",
      "category": "Muud",
      "location": "Kristiine"
    }
  ]
}
```

Näite ID-d, tööriistad ja kategooriad vastavad faili [3_import.sql](../../database/3_import.sql) algandmetele. Kõik neli tööriista kuuluvad Kristiine asukohaga omanikule ning nende staatus on `A`.

| Väli | JSON tüüp | Tähendus |
|---|---|---|
| `tools` | massiiv | Küsimusele vastavad tööriistad; vaste puudumisel tühi massiiv. |
| `tools[].toolId` | täisarv | Tööriista ID. Kasutajaliides kasutab seda olemasoleva `/tools/{toolId}` detailmarsruudi avamiseks. |
| `tools[].name` | string | Tööriista nimi tabelist `tool`. See ei ole omaniku ega muu kasutaja nimi. |
| `tools[].category` | string | Tööriista kategooria nimi. |
| `tools[].location` | string või `null`* | Tööriista omaniku asukoha linnaosa nimi, näiteks `Kristiine`; täpset tänava-aadressi ega koordinaate ei tagastata. Puuduva asukoha käsitlus on avatud küsimus allpool. |

Vaste puudumise näide:

```json
{
  "tools": []
}
```

Vastus ei sisalda vabatekstilist `answer` välja. Kasutajaliides kuvab tulemused ise näiteks kujul `Akutrell · Ehitustööd · Kristiine` ning muudab tööriista nime lingiks. Isikuandmeid, nagu omaniku või rentniku nimi, e-post, telefon või `google_sub`, vastuses ei tagastata.

## Seotud andmebaasi andmed

Vt [2_create.sql](../../database/2_create.sql) ja [3_import.sql](../../database/3_import.sql).

- `tool.id`, `tool.name` ja `tool.category_id` annavad tööriista ID, nime ning kategooriaseose.
- `category.id` ja `category.category_name` annavad kategooria nime.
- Omaniku asukohani viib praeguses skeemis seos `tool.owner_id` → `profile.user_id` → `profile.location_id` → `location.id` → `location.district_id` → `district.id`.
- Asukohanime leidmiseks vajaliku seose kasutamine ei anna luba LLM-ile kasutaja isikuandmete avaldamiseks. Response sisaldab ainult linnaosa nime; isikuandmete väljad ei kuulu lepingusse.
- `booking`-u põhjal filtreerimise reegel määratakse eraldi AI-03 taskis; see task fikseerib üksnes vastuse väljad.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---:|---|
| `question` puudub, on tühi või pikem kui 500 märki | 400 Bad Request | Olemasolev `ApiError` kuju: `message` ja `errorCode` (`INCORRECT_INPUT`). |
| Kasutaja pole sisse loginud | 401 Unauthorized | Olemasoleva `SecurityConfig` järgi tühi body. |
| Otsing ei leia sobivaid tööriistu | 200 OK | `{"tools":[]}` |
| Otsingu käigus tekib ootamatu serveriviga | 500 Internal Server Error | Olemasolev `ApiError` kuju; tehnilisi detaile kliendile ei tagastata. |

## Vastuvõtu kriteeriumid

- [ ] Request on `POST /api/ai/ask` JSON kujul `{"question":"..."}`.
- [ ] `question` on kohustuslik, mitte tühi ja maksimaalselt 500 märki.
- [ ] Edukas vastus on JSON objekt kujul `{"tools":[...]}`.
- [ ] Iga tööriista kirje sisaldab täpselt lepingus määratud välju `toolId`, `name`, `category` ja `location`.
- [ ] `toolId` väärtus on olemasoleva tööriista ID ja sobib `/tools/{toolId}` detailmarsruudi moodustamiseks.
- [ ] `name` tähistab tööriista nime, mitte kasutaja nime.
- [ ] Olemasoleva asukoha korral sisaldab `location` linnaosa nime, mitte tänava-aadressi ega koordinaate.
- [ ] Vaste puudumise korral tagastatakse HTTP 200 ja `tools: []`.
- [ ] Vastuses puuduvad vabatekstiline `answer`, omaniku/rentniku nimed, e-postid, telefonid ja `google_sub`.
- [ ] Päringu valideerimise, sisselogimise ja ootamatu vea staatused vastavad kirjeldatud lepingule.

## Avatud küsimused

- Kui tööriista omanikul puudub profiil või asukoht, kas tulemus jääb loendisse `location: null` väärtusega või jäetakse asukohata tööriist välja? Skeem lubab profiili puududa, seega tuleb see käitumine enne lepingu lõplikku kinnitamist valida.
- Kas `location` jääb ainult linnaosa nimeks ka siis, kui sama linnaosa nimi esineb mitmes linnas? Antud kasutajanäide kuvab ainult `Kristiine`; linna lisamist lepingusse pole praegu määratud.
