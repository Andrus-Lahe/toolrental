# AI-07 — Frontendi lingitud tulemuste kuvamine — implementatsiooni plaan

**Seotud task:** [AI-07-Frontendi-lingitud-tulemuste-kuvamine.md](./AI-07-Frontendi-lingitud-tulemuste-kuvamine.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/AiSearchView.vue` — sisaldab küsimuse vormi, `axios.post('/api/ai/ask', ...)` kutset ja `tools` massiivi loendit; nimi kuvatakse praegu `<strong>` elemendina ning vastuse `answer` tekst kuvatakse eraldi.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/router/index.js` — registreerib `toolDetailRoute` marsruudi `/tools/:toolId`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/ToolDetailView.vue` — olemasolev tööriista detailvaade.
- AI-06 response leping sisaldab `toolId`, `name`, `category`, `location` ning ei sisalda enam `answer` välja.

## Puuduv/muudetav

- Tulemuse nimi pole link.
- Vaade ootab praegu `answer` välja ja kasutab asukoha väljana `district`.
- Tühitulemuse kuvamine tuleb kooskõlastada AI-08 taskiga.

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

Hilisem UI kontroll peaks kinnitama kuvateksti, õiget route nime/`toolId` parameetrit, eemaldatud `answer` sõltuvust ning tühja/vea oleku nähtavust. Testkaustu ei loetud ega teste käivitatud.

## Avatud küsimused

- Asukoha puuduva väärtuse kuvamine tuleb joondada AI-01 kinnitatud lepinguga.
