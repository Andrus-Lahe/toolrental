# AI-06 — Backendi JSON-vastus — implementatsiooni plaan

**Seotud task:** [AI-06-Backendi-JSON-vastus.md](./AI-06-Backendi-JSON-vastus.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskResponse.java` — response record sisaldab nüüd ainult `List<AvailableTool> tools`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableTool.java` — sisaldab täpselt `toolId`, `name`, `category`, `location`; `location` on nullitav.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — annab AI-05 päringutulemused DTO-na edasi ega kasuta vastuse koostamiseks teist LLM kutset.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskController.java` — tagastab service response objekti otse Springi JSON serialiseerimiseks.
- AI-01 fikseerib uue API lepingu; AI-02 kuni AI-05 annavad päringutulemused.

AI mooduli testide kasutamine on kasutaja poolt lubatud.

## AI-06 tulemus

- Response'is puudub `answer`; tööriista kirjes on üks `location` väli linnaosa nime või `null` väärtusega.
- `toolId` ja ülejäänud kuvamisväljad tulevad AI-02 vaate tulemusest; LLM-i tekst ei muuda neid.
- Controller tagastab uue DTO ning route ja päringu kuju jäid samaks.

## Sammud

1. **Uuenda tööriista response DTO-d** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableTool.java` või AI-01 lepingule vastava nimega DTO.
   - Hoia väljad `toolId`, `name`, `category`, `location` täpselt AI-01 lepingus.
   - `toolId` peab pärinema andmebaasi rea `tool_id` väärtusest.
   - Kinnita AI-01 järgi `location` nullitavus enne DTO tüübi/serialiseerimise valikut.

2. **Uuenda API response DTO-d** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskResponse.java`.
   - Tagasta root JSON väljana `tools`.
   - Eemalda `answer`, sest uus kasutajavoog kuvab struktureeritud loendit.

3. **Ühenda service päringutulemusega** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java`.
   - Tagasta AI-05 käivitatud päringu read response DTO-na.
   - Eemalda teise LLM kutse kasutus vastuse proosaks koostamisel; AI-04 jääb SQL-i loomise ülesandeks.

4. **Hoia controlleri HTTP leping** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskController.java`.
   - Säilita `POST /api/ai/ask`, `@Valid` ning olemasolev request DTO.
   - Controller jätkab service DTO tagastamist; serialiseerimise kuju tuleb uue response record'iga.

## Veakäsitlus

Selle taski puhul ei muudeta erindite staatuseid. Tühja päringutulemuse korral tagastab service tühja `tools` loendi. AI/DB/SQL vead liiguvad AI-08 lepingusse ning vastus ei tohi sisaldada genereeritud SQL-i ega isikuandmeid.

## Testid

`ToolAskControllerTest` kontrollib vastuse võtmeid, `toolId` väärtust, `location` väärtust ja `null` serialiseerimist, `answer`/vanade location väljade puudumist, tühja `tools` massiivi ning sisselogimise nõuet. `ToolAskServiceTest` ja `ToolSearchQueryRepositoryTest` kontrollivad vastuse andmete päritolu ja kaardistust.

Kontrolli tulemus: AI mooduli lõplikus viie sihttestiklassi käivituses läbisid 18 testi päris PostgreSQL-ühendusega.

## Teostuse piir

AI-01 otsus on `location: null`; `AvailableTool` kasutuskohtade kontroll näitas, et AI otsingust väljaspool see DTO kasutusel ei ole. DTO nime ei muudeta, et hoida käesolev muudatus väikese ja sihitud ulatusega. Frontendi üleminek AI-06 lepingule kuulub AI-07-sse.
