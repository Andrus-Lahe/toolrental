# AI-08 — Tühja tulemuse ja vigade kuvamine — implementatsiooni plaan

**Seotud task:** [AI-08-Tuhja-tulemuse-ja-vigade-kuvamine.md](./AI-08-Tuhja-tulemuse-ja-vigade-kuvamine.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/AiSearchView.vue` — kuvab AI-06 JSON-i, lingitud tööriistad, tühitulemuse ning eristab 400, 401, 422 ja üldist viga.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` — valideerimisvead saavad `400 INCORRECT_INPUT`; ootamatu viga saab `500 INTERNAL_SERVER_ERROR` olemasoleva `ApiError` kujul.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java` — autentimata päring saab `401`; AI POST puhul body't ei kirjutata.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — tagastab tühja `tools` massiivi ainult eduka tühitulemuse korral; mittetoetatava küsimuse puhul viskab erindi.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` — annab mittetoetatavale küsimusele `422 AI_UNSUPPORTED_QUESTION`; tehnilised erindid jäävad üldiseks `500 INTERNAL_SERVER_ERROR` vastuseks.

Testide lisamine ja kasutamine on kasutaja poolt lubatud.

## AI-08 tulemus

- `200 tools: []`, 401, 400, 422 ja üldine 500 on eristatud.
- LLM-i mittetoetatav küsimus annab `422 AI_UNSUPPORTED_QUESTION`; AI/SQL/DB/võrgu tõrge annab üldise 500.
- Vigade korral eemaldatakse vana tulemus, tehnilisi detaile ei kuvata ja `busy` vabaneb `finally` harus.

## Sammud

1. **Fikseeri backend veastaatuste leping** — AI-04/AI-05 taskide käitusvead saavad AI-08-s lõpliku API kuju.
   - Mittetoetatav kasutajaküsimus on `422 AI_UNSUPPORTED_QUESTION` koos ohutu juhisega.
   - Vigane mudeli SQL ning LLM/DB/võrgu tõrge on `500 INTERNAL_SERVER_ERROR` üldsõnumiga.
   - Ära avalda SQL-i ega algset DB/stack trace detaili.

2. **Käsitle edukat tühitulemust** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/AiSearchView.vue`.
   - Kui `response.data.tools` on tühi massiiv, kuva tühitulemuse teade.
   - Ära kuva samal ajal vanast otsingust jäänud tulemusi.

3. **Käsitle API vigu** — sama fail.
   - Säilita `401` sisselogimisteade/link.
   - `400 INCORRECT_INPUT` ja `422 AI_UNSUPPORTED_QUESTION` puhul kuva vastavale olukorrale sobiv ohutu juhis.
   - Võrgu- ja serverivea puhul kuva üldine uuesti proovimise juhis.
   - Ära käsitle error response'i `tools: []` eduka vastusena.

4. **Taasta päringu olek kõigil harudel** — sama fail.
   - Hoia `busy` vabastamine `finally` harus.
   - Uue päringu alguses puhasta vana tulemus ja veateade.

## Veakäsitlus

Backend kasutab olemasolevat `ApiError` kuju ning ei saada kliendile SQL-i või andmebaasi tehnilisi detaile. Frontend eristab tühja HTTP 200 tulemust staatuskoodiga veast. 401 auth-entrypoint võib jääda tühja body'ga; UI määrab sisselogimise oleku statuskoodi järgi.

## Testid

`ToolAskControllerTest` kontrollib autentimist, request valideerimise 400 vastust, toetatud/unsupported teenuse vastuseid, 422 veakuju ning 500 üldist veakuju ilma SQL detailideta. `ToolAskServiceTest` kontrollib, et `CANNOT_ANSWER` ei jõua andmebaasi ja muudetakse eristatavaks veaks. `frontend/tests/ai-search.test.js` katab tühja 200, 400, 401, 422, 500, võrgutõrke ning `busy` vabastamise.

Kontrolli tulemus: backendis läbisid `ToolAskControllerTest` 6 ja `ToolAskServiceTest` 3 testi; frontendis läbisid kõik 4 AI otsingu testi. AI-08 muudatuste järel läbis ka `npm run build`.

## Teostuse piir

AI-01 `location: null` jääb tööriistakirjesse; AI-07 peidab ainult asukoha kuvateksti. 401 body jääb autentimise konfiguratsiooni järgi tühjaks ning UI tunneb selle ära HTTP staatuse järgi.
