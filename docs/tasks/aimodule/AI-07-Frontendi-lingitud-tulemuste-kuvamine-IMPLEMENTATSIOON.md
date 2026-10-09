# AI-07 — Frontendi lingitud tulemuste kuvamine — implementatsiooni plaan

**Seotud task:** [AI-07-Frontendi-lingitud-tulemuste-kuvamine.md](./AI-07-Frontendi-lingitud-tulemuste-kuvamine.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/AiSearchView.vue` — sisaldab nüüd küsimuse vormi, API kutset ning AI-06 lepinguga lingitud `tools` loendit.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/router/index.js` — registreerib `toolDetailRoute` marsruudi `/tools/:toolId`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/ToolDetailView.vue` — olemasolev tööriista detailvaade.
- AI-06 response leping sisaldab `toolId`, `name`, `category`, `location` ning ei sisalda enam `answer` välja.

## AI-07 tulemus

- Tulemuse nimi kasutab olemasolevat `toolDetailRoute` marsruuti koos API `toolId` väärtusega.
- Vastuses loetakse `location`; `answer`-i ja vana `district`-välja sõltuvust ei ole.
- Tühi edukas vastus, 401 ning üldine API viga kuvatakse eraldi olekutena.
- Valideerimata või vigase kujuga response loetakse päringuveaks; selle põhjal linke ei looda.

## Sammud

1. **Joonda vaate andmeväljad AI-06 response'iga** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/AiSearchView.vue`.
   - Kasuta `response.data.tools ?? []` ning välju `toolId`, `name`, `category`, `location`.
   - Eemalda `answer`-i sõltuvus ning renderda iga kirje tööriistaandmetest.

2. **Tee tööriista nimest detaililink** — sama fail.
   - Kasuta `<RouterLink>`-i ja olemasolevat nimega route'i:
     `:to="{ name: 'toolDetailRoute', params: { toolId: tool.toolId } }"`.
   - Ära pane `toolId` väärtust kasutaja sisendist ega LLM-i vabatekstist.

3. **Säilita vormi olekud** — sama fail.
   - Hoia `busy`, küsimuse valideerimine ning päringu submit käitumine.
   - Tühja tulemuse ja vea oleku markup/sõnumid võta AI-08 lepingust.

4. **Ära muuda route'i, kui olemasolev detailmarsruut sobib** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/router/index.js`.
   - Kasuta olemasolevat `toolDetailRoute` marsruuti; uut detail-URL-i ega endpointi pole vaja.

## Veaolukorrad

API veakäsitlus jääb AI-08 taski järgi. Link ise ei tohi tekkida puuduva või vigase `toolId` põhjal; vigane payload käsitletakse päringu veana, mitte suunatakse juhuslikule tööriistale.

## Testid

Lisatud `frontend/tests/ai-search.test.js` katab API body, uue väljade kuju, named route ja `toolId` kasutuse, tühja vastuse, vigase response'i ning 401/üldise vea käitumise.

Kontrolli tulemus: kõik 4 AI otsingu testi läbisid; `npm run build` läbis Vite tootmisbuild'i.

## Teostuse piir

Kui `location` on `null`, jäetakse asukoha osa kuvamata. AI-08 käsitleb AI-05 `CANNOT_ANSWER` markerit HTTP 422 vastusena koodiga `AI_UNSUPPORTED_QUESTION`, mille frontend kuvab eraldi teatega.
