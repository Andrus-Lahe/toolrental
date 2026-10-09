# AI-06 — Backendi JSON-vastus — implementatsiooni plaan

**Seotud task:** [AI-06-Backendi-JSON-vastus.md](./AI-06-Backendi-JSON-vastus.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskResponse.java` — response record on praegu `answer` ja `List<AvailableTool> tools`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableTool.java` — sisaldab `toolId`, `name`, `category`, `city`, `district`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — koostab praegu vastuse teise LLM kutsega ning tagastab tööriistaloendi.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskController.java` — tagastab service response objekti otse Springi JSON serialiseerimiseks.
- AI-01 fikseerib uue API lepingu; AI-02 kuni AI-05 annavad päringutulemused.

Test-kaustu ei uuritud kasutaja juhise järgi.

## Puuduv/muudetav

- Olemasoleva response sees olev `answer` väli ei kuulu uude lepingusse.
- Tööriista asukoht on kahe välja `city` ja `district` asemel üks `location`.
- SQL päringu tulemused tuleb siduda tegeliku `toolId`-ga; LLM-i kokkuvõtte väljundit ei kasutata andmeväljade allikana.

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

Hilisem teostus peab kontrollima JSON-i täpseid võtmeid ja tüüpe, `toolId` päritolu, asukoha väärtust ning tühja massiivi serialiseerimist. Testkaustu ei loetud ega teste käivitatud.

## Avatud küsimused

- AI-01 `location` puuduva väärtuse otsus peab enne response DTO lõplikku kuju olema tehtud.
- Kas olemasolev `AvailableTool` nimetatakse ümber lepingut väljendavaks DTO-ks või muudetakse olemasolevat record'it, sõltub selle kasutusest mujal; enne refactorit tuleb kasutuskohad üle kontrollida.
