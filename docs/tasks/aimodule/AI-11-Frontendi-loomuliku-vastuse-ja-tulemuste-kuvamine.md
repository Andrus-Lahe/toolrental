# AI-11 — Frontendi loomuliku vastuse ja tulemuste kuvamine

**Vaade:** `AiSearchView.vue`, route `/ai-search`

**Sõltuvad taskid:** [AI-07 — Frontendi lingitud tulemuste kuvamine](./AI-07-Frontendi-lingitud-tulemuste-kuvamine.md), [AI-08 — Tühja tulemuse ja vigade kuvamine](./AI-08-Tuhja-tulemuse-ja-vigade-kuvamine.md), [AI-10 — Loomuliku keele vastus ja JSON-leping](./AI-10-Loomuliku-keele-vastus-ja-JSON-leping.md)

## Kasutajavoog

Kasutaja sisestab küsimuse AI otsinguvaatesse ja esitab selle. Eduka päringu järel kuvatakse backendist saadud loomuliku keele vastus tööriistaloendi kohal ning iga leitud tööriist kuvatakse nime, kategooria ja asukohaga. Tööriista nimi jääb klikitavaks ja avab olemasoleva detailvaate. Kui tööriistu pole saadaval, kuvatakse vastuse tekst, kuid tühja loendi asemele ei lisata vana või sellega vastuolus olevat sõnumit.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Küsimuse sisestus | `textarea` | Olemasolev kohustuslik küsimuse väli; maksimaalselt 500 märki. |
| Küsi nupp | `button` | Esitab päringu; päringu ajal kuvatakse „Otsin…” ja nupp on keelatud. |
| Vastuse tekst | tekstiplokk | Kuvab API `answer` välja tööriistaloendi kohal. Vue peab teksti tavapäraselt escape'ima; vastust ei kuvata HTML-ina. |
| Tööriistade loend | lingitud loend | Kuvab `tools` massiivi kirjed kujul `Nimi · Kategooria · Asukoht`; nimi avab `toolDetailRoute` marsruudi `toolId` alusel. Puuduva asukoha korral jäetakse asukoha osa ära. |
| Tühja loendi olek | tühi loend | Kui `tools` on tühi, kuvatakse `answer`, aga tööriistalinkide loendit ei renderdata. Eraldi hardcoded tühitulemuse sõnumit ei lisata, sest LLM-i vastus kirjeldab olukorda. |
| Päringuvea teade | alert | Säilitab AI-08 järgi sisendvea, autentimata kasutaja, mittetoetatava küsimuse ja üldise vea eristuse. |

## Käitumine ja valideerimine

1. Päringu saatmisel puhastatakse eelmine `answer`, `tools` ja veateade ning seatakse vaade hõivatuks.
2. Frontend saadab senise päringu `POST /api/ai/ask` kujul `{"question":"..."}`.
3. Eduka `200` vastuse korral kontrollitakse, et `answer` on mitte-tühi string ning `tools` on massiiv, mille iga kirje vastab AI-06/AI-07 tööriista lepingu väljadele.
4. Kehtiva vastuse korral kuvatakse `answer` teksti ja `tools` loendit. Tühja `tools` massiivi korral kuvatakse ainult `answer`.
5. `location: null` korral kuvatakse nime ja kategooriat ning asukohateksti ei näidata.
6. Vigase või mittetäieliku edukas-vastuse payload'i korral ei looda linke; vaade kuvab üldise päringuvea.
7. HTTP 400, 401, 422, 500 ja võrgutõrge säilitavad AI-08-s määratud kasutajale nähtava käitumise.
8. Päringu lõppedes taastatakse hõivatuse olek ka vea korral.

## API kutsed

### `POST /api/ai/ask`

**Backend allikas:** `backend/src/main/java/ee/toolrental/aimodule/ToolAskController.java`, mille edukat vastuselepingut laiendab [AI-10 task](./AI-10-Loomuliku-keele-vastus-ja-JSON-leping.md). AI-10 implementatsioon peab enne selle frontendi muudatuse käivitamist olema valmis.

Request body:

```json
{
  "question": "Mitu tööriista on Kristiines?"
}
```

Response (200):

```json
{
  "answer": "Kristiines leidub kokku 20 tööriista. Hetkel saadaval on 8:",
  "tools": [
    {
      "toolId": 12,
      "name": "Akutrell",
      "category": "Ehitustööd",
      "location": "Kristiine"
    }
  ]
}
```

Kui vastavaid praegu saadaval tööriistu pole, on `tools` tühi massiiv; `answer` selgitab, kas asukohas ei leitud tööriistu või leitud tööriistadest ükski pole praegu saadaval.

**Veateated:**

| Status code | `errorCode` | Backend sõnum | Frontendi käitumine |
|---:|---|---|---|
| 400 | `INCORRECT_INPUT` | Olemasolev ohutu sisendi veateade | Kuvab küsimuse sisestamise juhise. |
| 401 | — | AI päringu auth-entrypoint võib tagastada tühja body | Kuvab sisselogimise juhise ja lingi. |
| 422 | `AI_UNSUPPORTED_QUESTION` | `Saan vastata ainult saadaval tööriistade otsimise küsimustele.` | Kuvab mittetoetatava küsimuse teate. |
| 500 | `INTERNAL_SERVER_ERROR` | Üldine ohutu serveriveateade | Kuvab üldise uuesti proovimise juhise; ei kuva tehnilisi detaile. |
| Võrgutõrge | — | HTTP vastust pole | Kuvab üldise uuesti proovimise juhise. |

## Komponendid ja failistruktuur

- `frontend/src/views/AiSearchView.vue` — olemasolev vaade; lisab API `answer` kuvamise ja uuendab response valideerimist.
- `frontend/src/router/index.js` — olemasolev `/ai-search` (`aiSearchRoute`) ja `/tools/:toolId` (`toolDetailRoute`) marsruut; muudatust pole vaja.
- `frontend/tests/ai-search.test.js` — olemasolev vaate API body, vastuse valideerimise, lingi ja veakäitumise test.

## Vastuvõtu kriteeriumid

- [ ] Kehtiva API vastuse `answer` kuvatakse enne tööriistade loendit.
- [ ] Vastuse tekst kuvatakse turvalise tekstina, mitte HTML-ina.
- [ ] Iga `tools` kirje kuvab nime, kategooria ja asukoha kokkulepitud järjekorras; puuduva asukoha korral asukohta ei kuvata.
- [ ] Tööriista nimi kasutab olemasolevat `/tools/{toolId}` detailmarsruuti.
- [ ] `tools: []` korral kuvatakse vastuse tekst ilma vana või dubleeriva tühitulemuse sõnumita.
- [ ] Puuduva või vale tüübi/kujuga `answer` või `tools` korral käsitletakse vastust veana ega looda vigaseid linke.
- [ ] Uue päringu alustamisel eemaldatakse eelmine vastus ja loend; veateate korral neid ei jäeta eksitavalt nähtavale.
- [ ] AI-08 400, 401, 422, 500 ja võrgutõrke käitumine säilib.
- [ ] Päringu hõivatuse olek taastub alati.
- [ ] Frontendi testid katavad vastusega tulemuse, tühja loendi, vigase vastuse ja veateed.

## Teostuse piir

See task kuvab AI-10 backendist saabunud `answer` välja ega muuda selle teksti. Vastuse sõnastamise faktid ja backend JSON lepingu loomine kuuluvad AI-09/AI-10 alla.
