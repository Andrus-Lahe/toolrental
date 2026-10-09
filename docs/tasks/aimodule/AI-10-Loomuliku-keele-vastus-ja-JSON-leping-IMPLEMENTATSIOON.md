# AI-10 — Loomuliku keele vastus ja JSON-leping — implementatsiooni plaan

**Seotud task:** [AI-10-Loomuliku-keele-vastus-ja-JSON-leping.md](./AI-10-Loomuliku-keele-vastus-ja-JSON-leping.md)

## Teostatud

- Vastuse genereerimine toimub eraldi Spring AI `ChatClient`-iga pärast otsingu loendusi.
- `ToolAskResponse(answer, tools)` järgib AI-01/AI-10 lepingut; tööriistad tulevad JDBC tulemustest.
- Vastuse mudelile antakse küsimus, arvud ning ainult üheselt tuvastatud asukoht/kategooria. Arvude muutmine, SQL, JSON ja kõrvalised faktid on promptis keelatud.
- Viis AI-09/AI-10 sihitud testiklassi läbisid kokku 25 testi, sh PostgreSQL repository testid.

## Algne seis (enne teostust)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — juhib praegu SQL-i genereerimist, valideeritud otsingut ja `ToolAskResponse` loomist.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlGenerator.java` — teeb ühe `ChatClient` kutse ning sisaldab eraldi SQL-i system prompti.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskResponse.java` — sisaldab praegu ainult `List<AvailableTool> tools` välja.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableTool.java` — backendist saadud lingitud tööriista kirje; LLM-i vastuse generaator ei tohi selle ID-sid ega loendit koostada.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/resources/application.properties` — Spring AI kasutab `google-genai` mudelipakkujat, `GEMINI_API_KEY` keskkonnamuutujat ja mudelit `gemini-3.5-flash-lite`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` — Spring AI Google GenAI starter on juba sõltuvusena olemas.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/aimodule/ToolSearchSqlGeneratorTest.java` — ChatClient mockimise muster, mida saab kasutada uue vastuse generaatori testides.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/aimodule/ToolAskServiceTest.java` ja `ToolAskControllerTest.java` — service ja API response testimise olemasolevad eeskujud.
- AI-09 task määrab backendis loodava otsingukokkuvõtte: otsingufiltrid, `totalToolCount` ja `availableToolCount`. AI-09 implementatsioon peab enne AI-10 käitusahela ühendamist valmis olema.

## Puuduv/muudetav

- Vastuse loomise eraldi komponent ja system prompt.
- `ToolAskService`-i teine, pärast otsingut toimuv mudelikutse AI-09 ohutute faktidega.
- Eduka vastuse DTO laiendus `answer` väljaga.
- Controller/service/generaatori testid uue vastuse ja error path'i jaoks.
- AI-01 ja AI-06 API lepingu dokumentatsiooni joondamine.

## Sammud

1. **Lisa vastuse genereerimise komponent** — uus fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchAnswerGenerator.java`.
   - Ehita sama Spring AI `ChatClient.Builder` abil eraldi `ChatClient`, nagu `ToolSearchSqlGenerator` teeb.
   - Hoia vastuse system prompt SQL promptist lahus.
   - Võta generaatori sisendiks kasutaja küsimus ja AI-09 kokkuvõtte minimaalsed faktid: teadaolevad filtrid, koguarv ning saadaval arv.
   - Nõua lühikest eestikeelset proosavastust, mis ei muuda arve ega lisa fakte. Ära luba mudelil tagastada JSON-it, SQL-i ega tööriistade nimekirja.
   - Käsitle kasutaja küsimust andmesisendina, mitte system juhiste asendajana; vastuse süsteemireeglid peavad jääma kehtima ka siis, kui küsimus sisaldab mudelile suunatud käske.
   - Tühja/blank vastuse korral viska üldine rakenduse erind; ära saada raw mudeliväljundit API veana edasi.

2. **Uuenda vastuse DTO-d** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskResponse.java`.
   - Muuda record väljad kujule `String answer` ja `List<AvailableTool> tools`.
   - Järjesta JSON-i väljad lepingus `answer`, seejärel `tools`.
   - Jäta `AvailableTool` DTO muutmata; `toolId` ning teised tööriistaväljad ei tule LLM-i väljundist.

3. **Ühenda teine kutse service töövoogu** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java`.
   - Järjekord on: SQL-i genereerimine → `CANNOT_ANSWER` kontroll → valideerimine → AI-09 otsing ja kokkuvõte → loomuliku keele vastuse genereerimine → `ToolAskResponse(answer, tools)`.
   - Anna vastuse generaatorile AI-09 kokkuvõtte arvud/filtrid ja algne küsimus. Ära saada talle `AvailableTool` kirjete loendit, kui vastuse koostamiseks pole seda vaja.
   - `CANNOT_ANSWER` korral katkesta enne andmebaasi ja enne vastuse generaatorit.
   - Ära muuda ega töötle ümber AI-05 poolt valideeritud SQL-i selle taski käigus.

4. **Uuenda lepingudokumentatsioon** — failid: `docs/tasks/aimodule/AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md` ja `docs/tasks/aimodule/AI-06-Backendi-JSON-vastus.md`.
   - Märgi uus edukas JSON leping `{ "answer": "...", "tools": [...] }` AI-10 laiendusena.
   - Hoia iga `tools` kirje väljade ja privaatsuspiirangute kirjeldus AI-01 järgi.

5. **Lisa generaatori ja service testid** — failid: `backend/src/test/java/ee/toolrental/aimodule/ToolSearchAnswerGeneratorTest.java` ja `ToolAskServiceTest.java`.
   - ChatClient mock'iga kontrolli system prompti reegleid, küsimuse ja AI-09 faktide edastamist, eestikeelse teksti tagastust ning tühja vastuse tagasilükkamist.
   - Kontrolli teenuse kutsete järjestust ja et ainult AI-09 arvud/filtrid jõuavad vastuse generaatorisse.
   - Kontrolli, et `AvailableTool` list ja ID-d võetakse repository vastusest ega asendata mudeli tekstiga.
   - Kontrolli, et `CANNOT_ANSWER` korral teist mudelikutsungit ei toimu ning generaatori runtime-viga liigub üldise 500 käsitlusse.

6. **Uuenda kontrolleritestid** — fail: `backend/src/test/java/ee/toolrental/aimodule/ToolAskControllerTest.java`.
   - Kontrolli HTTP 200 JSON kuju, `answer` teksti ning olemasolevaid `tools` välju koos korrektsete `toolId` väärtustega.
   - Säilita kontrollid 400, 401, 422 ja üldise 500 veakuju kohta.

## Veakäsitlus

Vastuse generaatori tühi väljund või mudeliteenuse tõrge liigub `RestExceptionHandler`-i üldisesse handlerisse ja tagastub AI-08 järgi `500 INTERNAL_SERVER_ERROR` üldise ohutu sõnumiga. Mudeli toorväljundit, SQL-i ega stack trace'i kliendile ei tagastata. SQL generaatori `CANNOT_ANSWER` marker jääb eraldi AI-08 `422 AI_UNSUPPORTED_QUESTION` vastuseks ja väldib teist LLM kutset.

## Testid

Lisa mockitud ChatClientiga `ToolSearchAnswerGeneratorTest`; täienda `ToolAskServiceTest`-i teise mudelikutse järjestuse, faktide ja tulemuste allika kontrollidega; täienda `ToolAskControllerTest`-i uue JSON kuju ja üldise mudelivea kontrollidega. Need testid ei vaja päris Gemini teenust ega API võtit. Andmebaasi loenduse õigsus kuulub AI-09 repository testidesse.

## Avatud küsimused

Käitumisotsused on kokku lepitud varasemates AI taskides: vastus on eesti keeles; faktid ja arvud tulevad AI-09 backend kokkuvõttest; LLM tagastab ainult teksti; `tools` tuleb andmebaasist; vastuse mudelikutse rike käsitletakse AI-08 järgi üldise HTTP 500 veana. Frontendi kuvamine jääb järgmisse taski.
