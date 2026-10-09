# AI-11 — Frontendi loomuliku vastuse ja tulemuste kuvamine — implementatsiooni plaan

**Seotud task:** [AI-11-Frontendi-loomuliku-vastuse-ja-tulemuste-kuvamine.md](./AI-11-Frontendi-loomuliku-vastuse-ja-tulemuste-kuvamine.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/AiSearchView.vue` — sisaldab küsimuse vormi, API kutset, valideerib `tools` massiivi ja kuvab detailvaate lingid; response'ist ei loeta veel `answer` välja.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/router/index.js` — registreerib `/ai-search` (`aiSearchRoute`) ja `/tools/:toolId` (`toolDetailRoute`) marsruudid.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/tests/ai-search.test.js` — kontrollib API body't, tööriista välju ja route'i, tühja tulemust, vigast response'i ning 400/401/422/500/võrgutõrkeid.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-10-Loomuliku-keele-vastus-ja-JSON-leping.md` — määrab tulevase edukas-vastuse kuju `{ "answer": "...", "tools": [...] }`.
- Frontend kasutab selles vaates Options API stiili; `frontend/CLAUDE.md` kirjeldab rakenduse üldist tehnoloogiat ja `docs/frontend/vue-komponendi-struktuur.md` projekti olemasolevat Vue komponendi mustrit.

## Puuduv/muudetav

- `AiSearchView.vue` peab salvestama ja näitama response `answer` välja.
- Response valideerimine peab nõudma mitte-tühja `answer` stringi ning säilitama olemasoleva `tools`/kirjete valideerimise.
- Tühja loendi korral tuleb vältida vastuolulise hardcoded teate kuvamist lisaks LLM-i vastusele.
- `ai-search.test.js` peab katma uue vastuse ja regressioonid.

## Sammud

1. **Lisa `answer` vaate olekusse** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/AiSearchView.vue`.
   - Lisa `answer: ''` `data()` algolekusse.
   - Puhasta see koos `tools` ja veaga uue päringu alguses ning päringu ebaõnnestumisel.
   - Salvesta edukast vastusest nii `answer` kui ka `tools`.

2. **Uuenda vastuse payload'i valideerimist** — sama fail.
   - Muuda `isValidToolSearchResponse(data)` kontrollima, et `data.answer` on string ega ole tühi või ainult tühikutest.
   - Säilita `tools` massiivi ja iga `toolId`, `name`, `category`, `location` väärtuse praegused kontrollid.
   - Vigane `answer` või tööriistakirje peab minema olemasolevasse üldise päringuvea käsitlusse.

3. **Kuva vastus tulemuste kohal** — sama fail.
   - Näita `answer` teksti `hasSearched` olekus enne loendit ja seosta uuendatud vastuse piirkond `aria-live="polite"` semantikaga.
   - Kasuta Vue tavalist interpolatsiooni `{{ answer }}`; ära kasuta `v-html`.
   - Kui `tools` massiiv on tühi, jäta linkide loend kuvamata ning ära kuva vana „tööriistu ei leitud” hardcoded sõnumit, sest vastuse generaator kirjeldab olukorda.
   - Tulemuste olemasolul säilita `RouterLink`, `toolDetailRoute` ja iga tööriista tegelik `toolId`.

4. **Säilita vead ja hõivatuse olek** — sama fail.
   - Säilita AI-08 400 sisenditeade, 401 sisselogimislink, 422 mittetoetatava küsimuse teade ja üldine serveri/võrguvea teade.
   - API vea korral puhasta `answer` koos vana `tools` loendiga.
   - Hoia `busy = false` taastamine `finally` harus.

5. **Uuenda AI otsingu vaate teste** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/tests/ai-search.test.js`.
   - Positiivse vastuse test kontrollib `answer` teksti ning et see paikneb template'is enne tulemuste loendit.
   - Tühja `tools` loendi test kontrollib, et vastus säilib ja tühja loendi teade ei dubleeri seda.
   - Vigase payload'i juhtumid katavad puuduva/tühja `answer` ning varasemad vigased `tools` kirjed.
   - Veateed kontrollivad, et vana vastus ja vana loend on eemaldatud ning `busy` taastub.

## Veakäsitlus

AI-08 määratud käitumist ei muudeta. Vigane HTTP 200 payload ja ootamatu võrgutõrge kuvavad üldise päringuvea. HTTP 400, 401, 422 ja 500 jäävad eraldi käsitletuks. Uue päringu alguses ja iga vea korral puhastatakse vastuse tekst ning tööriistaloend.

## Testid

Täienda olemasolevat `frontend/tests/ai-search.test.js` faili. Selle repo praegune `frontend/package.json` ei defineeri `test:ai-search` scripti, seega kasuta käsitsi käivituseks `node tests/ai-search.test.js` kataloogis `frontend/`. Kontrolli lisaks `npm run build` käsuga, et Vue template ja komponendi kompileerimine õnnestub. `npm run lint` käivitab fix-lippudega lintimise, seega ära kasuta seda selle taski kontrollina.

## Avatud küsimused

Kasutajaliidese käitumine on olemasolevate kokkulepete põhjal määratud: `answer` kuvatakse tekstina loendi kohal, `tools` list jääb AI-07 lingireegli järgi ning tühja loendi korral eraldi vastuolulist tühitulemuse teksti ei lisata. AI-10 backend peab enne integratsiooni vastama dokumenteeritud `answer` + `tools` lepingule.
