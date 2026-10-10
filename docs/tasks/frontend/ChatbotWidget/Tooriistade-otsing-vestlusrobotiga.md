# Tööriistade otsing vestlusrobotiga

**Vaade:** `ChatbotWidget.vue`, ühine komponent. Oma route puudub, kuvatakse kõigi lehtede peal paremas alanurgas.

**Roll:** Customer, Admin (külastajale vestlusakent ei kuvata).

**Vaste balsamic mockupis:** mockupi pilti ega PDF-i lehte selle vaate kohta pole.

Alus: [ChatbotWidget märkmed](../../../balsamic/notes/ChatbotWidget-markmed.md) ja [vestlusroboti BE-task](../../backend/ChatbotWidget/Tooriistade-otsing-vestlusrobotiga.md). Laenuka robot on **„Abimees Pets“**. Kirjaklambri ikooni ei ole.

## Kasutajavoog

Sisse logitud kasutaja näeb igal lehel paremas alanurgas ujuvat vestlusnuppu. Nupu vajutamisel avaneb vestlusaken, kus Abimees Pets tervitab kasutajat. Kasutaja kirjutab küsimuse (nt „Millised tööriistad on saadaval Kristiines?“) ja saadab selle. Robot vastab tekstiga ja selle all kuvatakse leitud tööriistad. Tööriista nimele vajutades avaneb tööriista detailvaade `/tools/{toolId}`. Vestlus on ainult frontendi mälus ja kaob lehe värskendamisel.

Praegune eraldi leht `AiSearchView.vue` (`/ai-search`) ja päise link „AI otsing“ asendatakse selle vestlusaknaga (märkmete staatus: „praegune eraldi leht asendatakse sellega“).

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Vestlusnupp | Ujuv nupp (paremal all, `position: fixed`) | Nähtav ainult siis, kui `session.status === 'authenticated'`. Ikoon, nt `PhChatCircleDots` (`@phosphor-icons/vue`). `aria-label="Ava vestlus Abimees Petsiga"`. Kui aken on avatud, nuppu ei kuvata või see sulgeb akna. |
| Vestlusakna päis | Päis | Vestluse ikoon, pealkiri **„Abimees Pets“** ja sulgemisnupp „X“ (`aria-label="Sulge vestlus"`). |
| Sõnumite ala | Keritav loend | Roboti ja kasutaja sõnumid eraldi mullidena: roboti sõnumid vasakul, kasutaja omad paremal. `aria-live="polite"`, et ekraanilugeja loeks uue vastuse ette. |
| Tervitussõnum | Roboti sõnum | Akna avamisel on esimene sõnum alati **„Tere! Mina olen Abimees Pets. Kuidas saan sind täna aidata?“** Seda backendist ei küsita. |
| Kasutaja sõnum | Mull | Saadetud küsimus täpselt nii, nagu kasutaja selle kirjutas. |
| Roboti vastus | Mull | `answer` tekst. Kuvatakse `{{ }}` kaudu, **mitte `v-html`'iga** (AI tekst on usaldamatu). |
| Tööriistade loend | Lingid roboti vastuse all | Kuvatakse ainult siis, kui `tools` pole tühi. Iga rida on kujul `name` · `category` (· `district`, kui see pole `null`). Rida on klikitav link tööriista detailvaatele. |
| Ootamise olek | Roboti mull | Päringu ajal kuvatakse „Pets mõtleb…“ (või kolm punkti). |
| Veateade | Roboti mull (punane) | Backendi `message` või üldine viga (vt API kutsed). |
| Allanoole nupp | Ümmargune nupp sõnumite ala peal | Nähtav siis, kui kasutaja on üles kerinud ja viimane sõnum pole nähtav. Vajutus kerib viimase sõnumini. |
| Sisestusväli | `textarea` (1 rida, kasvab) | Placeholder **„Kirjuta siia...“**, `maxlength="500"`, `v-model.trim`. Enter saadab, Shift+Enter teeb reavahetuse. |
| Saatmisnupp | Nupp (paberlennuki ikoon, nt `PhPaperPlaneRight`) | `aria-label="Saada"`. Keelatud, kui väli on tühi või päring käib. |

Kirjaklambri (faili lisamise) ikooni **ei ole**, sest backend võtab vastu ainult teksti.

## Käitumine ja valideerimine

1. `ChatbotWidget` lisatakse üks kord faili `App.vue` (`RouterView` järele), mitte igasse vaatesse. Nähtavus sõltub ühisest `session` olekust (`@/auth/session.js`). Komponent ei tee oma `/api/me` päringut.
2. Kui `session.status` ei ole `'authenticated'` (`loading`, `guest`, `error`), ei kuvata ei nuppu ega akent. Väljalogimisel (`guest`) suletakse aken ja vestlus tühjendatakse.
3. Akna esimesel avamisel lisatakse sõnumite loendisse tervitussõnum. Sulgemine ja uuesti avamine **säilitab** vestluse sama lehekülje laadimise jooksul. Värskendamisel vestlus kaob.
4. **Frontendi valideerimine enne saatmist.** Tühja või ainult tühikutest koosneva välja korral on saatmisnupp keelatud ja Enter ei saada midagi. `maxlength="500"` takistab pikemat sisestust. Backend kontrollib sama asja ka ise (400 `INCORRECT_INPUT`).
5. Saatmisel:
   - kasutaja sõnum lisatakse loendisse, väli tühjendatakse, `isSending = true`, kuvatakse ootamise olek ja saatmisnupp keelatakse;
   - tehakse päring `POST /api/ai/ask` kujul `{ "question": "<küsimus>" }`;
   - korraga on käimas ainult üks päring.
6. **200 vastus:** ootamise olek asendatakse roboti mulliga (`answer`) ja selle all kuvatakse `tools` loend. Kui `tools` on `[]`, kuvatakse ainult tekst. Ka „Saan aidata ainult saadaval tööriistade otsimisega.“ ja teised 200 erijuhud on tavalised vastused, mitte vead.
7. **Tööriista nimele vajutus:** `NavigationService.navigateToToolDetail(this.$router, tool.toolId)`. Aken jääb avatuks ja vestlus säilib, sest komponent on `App.vue`'s, mitte vaate sees.
8. **Veateated** (vt tabel allpool) kuvatakse punase roboti mullina vestluses. Kasutaja küsimus jääb loendisse. Sisestusväli on kohe uuesti kasutatav, et ta saaks küsimust parandada.
9. **401 vestluse ajal** (sessioon aegus): kuvatakse backendi `message` ja kutsutakse `loadSession()`, mis muudab oleku `guest`'iks. Selle tõttu aken sulgub ja päis näitab sisselogimise nuppu.
10. **Kerimine:** uue sõnumi lisamisel keritakse sõnumite ala alla (`$nextTick`, siis `scrollTop = scrollHeight`). Allanoole nupp kuvatakse ainult siis, kui kasutaja on üles kerinud.
11. **Ligipääsetavus:** Escape sulgeb akna, avamisel liigub fookus sisestusväljale, sulgemisel tagasi vestlusnupule. Kitsal ekraanil (alla 576px) on aken peaaegu täislaiuses ja ei kata sisestusvälja klaviatuuri all.

## API kutsed

### `POST /api/ai/ask`

**Backend task:** [Tööriistade otsing vestlusrobotiga](../../backend/ChatbotWidget/Tooriistade-otsing-vestlusrobotiga.md).

**Backend allikas:** `ToolAskController.java`, `ToolAskRequest.java`, `ToolAskResponse.java`, `AvailableTool.java`, `ToolAskService.java`, `RestExceptionHandler.java`, `SecurityConfig.java`. Endpoint on koodis valmis. `QUESTION_REJECTED` on lisatud backend taski implementatsiooniga.

Päring kasutab sama origin'i sessiooniküpsist. Kasutaja ID-d ega muid välju body's ei saadeta.

`ToolAskRequest.java`, request body:

```json
{
  "question": "Millised tööriistad on saadaval Kristiines?"
}
```

`question`: kohustuslik, mitte tühi, kuni 500 märki.

`ToolAskResponse.java`, response (200):

```json
{
  "answer": "Kristiines on praegu saadaval Akutrell, Muruniiduk, Survepesur ja Matkatelk.",
  "tools": [
    {
      "toolId": 1,
      "name": "Akutrell",
      "category": "Ehitustööd",
      "city": "Tallinn",
      "district": "Kristiine"
    },
    ...
  ]
}
```

- `answer` on Gemini loodud tekst ja selle sõnastus muutub iga kord. Testides ära eelda täpset sõnastust.
- `tools` on kuni 20 tööriista. Tühi tulemus on `[]`, mitte `null`.
- `city` ja `district` võivad olla `null` (omanikul pole asukohta).
- 200 vastused ilma tööriistadeta: `"Saan aidata ainult saadaval tööriistade otsimisega."`, `"Sellist linna, linnaosa või kategooriat ei leitud."` ja `"Sellele otsingule vastavaid saadaval tööriistu ei leitud."`. Kõigil neil on `tools: []`.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | AUTHENTICATION_REQUIRED | Vaate avamiseks logi sisse. | Kuva message vestluses, kutsu `loadSession()`. Aken sulgub, kui olek on `guest`. |
| 400 | INCORRECT_INPUT | question: ei tohi olla tühi | Ei tohiks juhtuda (FE keelab saatmise). Kuva message punase mullina. |
| 400 | INCORRECT_INPUT | question: ei tohi olla pikem kui 500 märki | Ei tohiks juhtuda (`maxlength`). Kuva message punase mullina. |
| 400 | QUESTION_REJECTED | Küsimus lükati turvakontrolli tõttu tagasi. Palun sõnasta küsimus ümber. | Kuva message punase mullina. Kasutaja saab kohe uue küsimuse kirjutada. |
| 500 | INTERNAL_SERVER_ERROR | Toiming ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message punase mullina. |
| — | — | (võrguviga, vastust pole) | Kuva „Päring ebaõnnestus. Palun proovi uuesti.“ |

Veateate kuvamisel kasuta backendi `error.response.data.message` välja. Kui see puudub, kasuta võrguvea teksti. Ära suuna veavaatele: vestlusrobot on lisavõimalus ja selle viga ei tohi kasutaja käimasolevat lehte katkestada.

## Komponendid ja failistruktuur

| Fail | Seis | Tegevus |
|---|---|---|
| `frontend/src/components/common/ChatbotWidget.vue` | Puudub | **Uus.** Options API struktuuris (`name`, `components`, `data`, `computed`, `methods`). `data`: `isOpen`, `isSending`, `question`, `messages: [{ sender: 'bot' \| 'user', text: '', tools: [], isError: false }]`, `session`. API muster: `sendAskRequest()` → `.then(handleAskResponse)` → `.catch(handleAskError)` → `.finally(() => (this.isSending = false))`. |
| `frontend/src/api-services/AiService.js` | Puudub | **Uus.** `sendAskRequest(question) { return axios.post('/api/ai/ask', { question }) }`, samamoodi nagu `CityService.js`. |
| `frontend/src/App.vue` | Olemas | Lisa `<ChatbotWidget />` `RouterView` järele ja `components` loendisse. |
| `frontend/src/navigation/NavigationService.js` | Olemas | `navigateToToolDetail(router, toolId)` on juba olemas, muudatust ei vaja. |
| `frontend/src/views/AiSearchView.vue` | Olemas | **Eemalda** (asendatakse vestlusaknaga). |
| `frontend/src/router/index.js` | Rada `/ai-search` (`aiSearchRoute`) on olemas | **Eemalda** rada ja `AiSearchView` import. |
| `frontend/src/navigation/AppHeader.vue` | Link „AI otsing“ (`/ai-search`) on kõigile nähtav | **Eemalda** link. |

Vestlusakna kujundus (värvid, mullid) järgib projekti olemasolevat teemat.

## Vastuvõtu kriteeriumid

- [ ] Sisse logitud Customer ja Admin näevad igal lehel paremas alanurgas vestlusnuppu. Külastaja ei näe seda, ka siis mitte, kui `session.status` on `loading` või `error`.
- [ ] Aken avaneb pealkirjaga „Abimees Pets“ ja tervitusega „Tere! Mina olen Abimees Pets. Kuidas saan sind täna aidata?“. Sulgemisnupp „X“ ja Escape sulgevad akna.
- [ ] Kirjaklambri ikooni pole.
- [ ] Tühja või ainult tühikutega välja ei saa saata. Üle 500 märgi ei saa sisestada.
- [ ] Saatmisel kuvatakse küsimus, ootamise olek ja saatmisnupp on keelatud. Korraga käib ainult üks päring.
- [ ] Küsimusele „Millised tööriistad on saadaval Kristiines?“ kuvatakse `answer` ning tööriistade loend (3_import.sql andmetel Akutrell, Muruniiduk, Survepesur, Matkatelk).
- [ ] Tööriista nimele vajutus avab `/tools/{toolId}`. Vestlus säilib.
- [ ] `tools: []` korral kuvatakse ainult tekst, tühja loendit ei näidata.
- [ ] `district: null` ei kuva teksti „null“.
- [ ] AI vastus kuvatakse tekstina (`{{ }}`), mitte HTML-ina.
- [ ] 400 (`INCORRECT_INPUT`, `QUESTION_REJECTED`), 500 ja võrguvea korral kuvatakse vestluses punane mull õige tekstiga ning väli on uuesti kasutatav.
- [ ] 401 korral kuvatakse message, sessioon laetakse uuesti ja aken sulgub.
- [ ] Uue sõnumi korral keritakse alla. Allanoole nupp ilmub ainult üles kerimisel.
- [ ] Vestlus säilib akna sulgemisel ja avamisel, kaob lehe värskendamisel ja väljalogimisel.
- [ ] `AiSearchView.vue`, rada `/ai-search` ja päise link „AI otsing“ on eemaldatud.
- [ ] Aken on kasutatav kitsal ekraanil ja klaviatuuriga (fookus, Enter, Shift+Enter, Escape).
