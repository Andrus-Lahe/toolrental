# AI-06 — Backendi JSON-vastus

**Sõltuv task:** [AI-01 — Otsingu päringu ja vastuse andmeleping](./AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md)

## Eesmärk

Tagastada `/api/ai/ask` tulemused AI-01 andmelepingu kohase JSON-loendina, et frontend saaks näidata tööriista nime klikitava detaililingina. Backend tagastab andmebaasist leitud tööriistade struktureeritud andmed; eraldi LLM-i loodud vabatekstilist kokkuvõtet selle vastuse jaoks ei kasutata.

## API leping

**Request:** `POST /api/ai/ask`, JSON `{"question":"Millised tööriistad on saadaval Kristiines?"}`.

**Response (200 OK):**

```json
{
  "tools": [
    {
      "toolId": 1,
      "name": "Akutrell",
      "category": "Ehitustööd",
      "location": "Kristiine"
    }
  ]
}
```

Vaste puudumisel tagastatakse `{"tools":[]}`. Iga tööriista kirje väljad ja nende tähendus on määratud [AI-01 taskis](./AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md). Vastuses ei ole `answer` välja ega kasutaja isikuandmeid.

## Vastuvõtu kriteeriumid

- [x] `POST /api/ai/ask` tagastab HTTP 200 korral AI-01 lepinguga JSON objekti.
- [x] `tools` on massiiv ning kirje väljad on `toolId`, `name`, `category`, `location`.
- [x] `toolId` on andmebaasi tegelik `tool.id`, mitte genereeritud või LLM-i pakutud ID.
- [x] `name`, `category` ja `location` vastavad andmevaate väärtustele.
- [x] Tühja tulemuse korral tagastatakse HTTP 200 ja `tools: []`.
- [x] JSON ei sisalda vabatekstilist `answer` välja, omaniku/rentniku isikuandmeid ega toor-SQL-i.
- [x] HTTP valideerimis-, autentimis- ja serverivigade leping jääb kirjeldatuks AI-01 ning AI-08 järgi.

## Seotud komponendid

Backend kasutab `ToolAskResponse(tools)` ja `AvailableTool(toolId, name, category, location)`, mis järgivad AI-01 lepingut. Endpointi marsruut ja päringu `question` väli jäid samaks.

## Veaolukorrad

Kasutaja päringu valideerimine jääb HTTP 400; sisselogimata päring HTTP 401; ootamatu backend/AI/DB tõrge HTTP 500 olemasoleva `ApiError` kujul. Tühitulemus on edukas HTTP 200 vastus tühja massiiviga.

## Teostuse piir

AI-01 määrab, et puuduva asukoha korral jääb tööriist tulemusse ja `location` on JSON-is `null`. AI-06 seda valikut ei muuda. Mittetoetatava küsimuse täpsem HTTP/UI käsitlus jääb AI-08 taski.
