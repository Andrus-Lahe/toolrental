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

- [ ] `POST /api/ai/ask` tagastab HTTP 200 korral AI-01 lepinguga JSON objekti.
- [ ] `tools` on massiiv ning kirje väljad on `toolId`, `name`, `category`, `location`.
- [ ] `toolId` on andmebaasi tegelik `tool.id`, mitte genereeritud või LLM-i pakutud ID.
- [ ] `name`, `category` ja `location` vastavad andmevaate väärtustele.
- [ ] Tühja tulemuse korral tagastatakse HTTP 200 ja `tools: []`.
- [ ] JSON ei sisalda vabatekstilist `answer` välja, omaniku/rentniku isikuandmeid ega toor-SQL-i.
- [ ] HTTP valideerimis-, autentimis- ja serverivigade leping jääb kirjeldatuks AI-01 ning AI-08 järgi.

## Seotud komponendid

Praegune backend kasutab `ToolAskResponse(answer, tools)` ja `AvailableTool(toolId, name, category, city, district)`. Uus response peab järgima AI-01 ühe `location` väljaga kuju. Endpointi marsruuti ega päringu `question` välja see task ei muuda.

## Veaolukorrad

Kasutaja päringu valideerimine jääb HTTP 400; sisselogimata päring HTTP 401; ootamatu backend/AI/DB tõrge HTTP 500 olemasoleva `ApiError` kujul. Tühitulemus on edukas HTTP 200 vastus tühja massiiviga.

## Avatud küsimused

- `location: null` ja asukohata tööriista loendis hoidmise valik sõltub AI-01 avatud küsimusest.
