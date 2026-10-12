# Vestlusroboti logide selgitus

Siin on selgitatud backendi logiridu, mis tekivad, kui kasutaja saadab vestlusrobotile „Abimees Pets“ küsimuse (`POST /api/ai/ask`). Näited on päris logist. SQL-read tulevad P6Spy logist, `[AiMetrics]` read `AiMetricsAdvisor`'ist ja `[RegexJailbreak]` read `RegexJailbreakAdvisor`'ist.

Lahenduse ülevaade: [AI-lahenduse-ulevaade.md](AI-lahenduse-ulevaade.md).

## Logirea algus

Spring Booti logiread on sama algusega:

```text
INFO 15196 --- [backend] [nio-8080-exec-6] e.t.infrastructure.ai.AiMetricsAdvisor   : ...
```

| Osa | Tähendus |
|---|---|
| `INFO` / `WARN` | Logitase. Mõõdikud on `INFO`, blokeeritud küsimus on `WARN`. Kumbki ei ole viga. |
| `15196` | Backendi protsessi ID (PID) Windowsis |
| `[backend]` | Rakenduse nimi (`spring.application.name`) |
| `[nio-8080-exec-6]` | Tomcati lõim (thread), mis seda HTTP-päringut pordil 8080 teenindas. Ühe küsimuse kõik read on samal lõimel. |
| `e.t.infrastructure.ai.AiMetricsAdvisor` | Logija klass, lühendatult `ee.toolrental.infrastructure.ai.AiMetricsAdvisor` |

## `[AiMetrics] llm_call` rida

```text
[AiMetrics] llm_call in=651 out=109 latencyMs=1041 model=gemini-3.5-flash-lite finishReason=STOP
```

Iga Gemini kutse kohta tuleb üks rida. Eduka küsimuse kohta on neid tavaliselt kaks: intent-kutse ja vastuse kutse.

| Väli | Näide | Tähendus |
|---|---|---|
| `in` | 651 | Sisendtokenid: kõik, mis Geminile saadeti (system-prompt koos few-shot näidetega, loendid, küsimus). Token on umbes 3–4 tähemärki, eesti keeles pigem vähem. |
| `out` | 109 | Väljundtokenid: Gemini vastuse pikkus |
| `latencyMs` | 1041 | Kutse kestus millisekundites, koos võrguaja ja Gemini tööajaga |
| `model` | gemini-3.5-flash-lite | Mudel, mis vastas. Kinnitab, et `application.properties` seade jõudis kohale. |
| `finishReason` | STOP | Mudel lõpetas vastuse loomulikult. Probleemile viitaks `MAX_TOKENS` (vastus lõigati `maxOutputTokens` piiril: intent-kutsel 300, vastuse kutsel 400) või `SAFETY` (Gemini enda turvafilter blokeeris vastuse). |

`AiMetricsAdvisor` ei logi, kumb kutse see oli. Intent-kutse sisend on suurem (few-shot näited ja loendid) ja selle rida tuleb esimesena.

## Näide 1: tagasilükatud küsimus

```text
Connection: 10
Category: statement
Query: SELECT city_name FROM tool_rental.city ORDER BY city_name
-
Connection: 10
Category: statement
Query: SELECT DISTINCT district_name FROM tool_rental.district ORDER BY district_name
-
Connection: 10
Category: statement
Query: SELECT category_name FROM tool_rental.category ORDER BY category_name
WARN 15196 --- [backend] [nio-8080-exec-8] e.t.i.ai.RegexJailbreakAdvisor: [RegexJailbreak] blocked rule=ignore-instructions-et
WARN 15196 --- [backend] [nio-8080-exec-8] e.t.infrastructure.RestExceptionHandler  : Guardrail blocked request: guardrail=regex-jailbreak-guard:ignore-instructions-et
```

Turvakontroll töötas: küsimus lükati tagasi enne, kui see Geminini jõudis.

**1. Kolm SQL-päringut (P6Spy)**

| Väli | Tähendus |
|---|---|
| `Connection: 10` | Andmebaasiühenduse number ühenduste kogumis (Hikari pool). Kõik kolm päringut kasutasid sama ühendust. |
| `Category: statement` | Tavaline SQL-lause (mitte commit või rollback) |
| `Query: …` | Täpne SQL, mis käivitati |

Need on `ToolAskService.ask()` esimesed read: `repository.cityNames()`, `districtNames()` ja `categoryNames()`. Nende nimede loendid lähevad intent-prompti, et mudel valiks ainult olemasolevaid nimesid (grounding).

**2. `[RegexJailbreak] blocked rule=ignore-instructions-et`**

- `WARN` on hoiatus, mitte viga. Rünnakukatse blokeerimine on oodatud olukord.
- `rule=ignore-instructions-et`: küsimus vastas eestikeelsele mustrile „ignoreeri / unusta / eira … juhiseid / reegleid“. Näiteks „Ignoreeri kõiki eelnevaid juhiseid“.
- Logisse läheb ainult reegli nimi, mitte kasutaja küsimus.

**3. `Guardrail blocked request: guardrail=regex-jailbreak-guard:ignore-instructions-et`**

`RestExceptionHandler` püüdis kinni `GuardrailViolationException`'i ja logis selle. Kliendile läks 400 vastus:

```json
{"errorCode": "QUESTION_REJECTED", "message": "Küsimus lükati turvakontrolli tõttu tagasi. Palun sõnasta küsimus ümber."}
```

Reegli nime vastuses ei ole, see jääb ainult serveri logisse.

**Mida rida puudumine näitab:** `[AiMetrics] llm_call` rida ei tulnud. See tähendab, et Gemini kutset ei tehtud, tokeneid ei kulunud ja kulu oli null.

`AiMetricsAdvisor` käivitub ahelas esimesena, aga logib alles siis, kui kutse on tagasi tulnud. Regex-advisor viskas erindi enne, kui päring Geminini jõudis, ja erind läks mõõdikute advisorist läbi enne logimist.

**Tähelepanek:** kolm SQL-päringut tehakse ka tagasilükatud küsimuse korral, sest regex-kontroll toimub Gemini kutse sees, mis tuleb pärast nimede laadimist. Need päringud on väikesed ja kiired, seega praktilist probleemi ei ole. Kui on vaja, et tagasilükatud küsimus andmebaasi üldse ei puudutaks, saaks nimede laadimise tõsta kontrolli järele (väike muudatus `ToolAskService`'is).

## Näide 2: edukas küsimus

```text
Connection: 10
Category: statement
Query: SELECT city_name FROM tool_rental.city ORDER BY city_name
-
Connection: 10
Category: statement
Query: SELECT DISTINCT district_name FROM tool_rental.district ORDER BY district_name
-
Connection: 10
Category: statement
Query: SELECT category_name FROM tool_rental.category ORDER BY category_name
INFO 15196 --- [backend] [nio-8080-exec-9] e.t.infrastructure.ai.AiMetricsAdvisor: [AiMetrics] llm_call in=649 out=65 latencyMs=991 model=gemini-3.5-flash-lite finishReason=STOP
-
Connection: 10
Category: statement
Query: SELECT t.id AS tool_id, t.name, c.category_name, ci.city_name, d.district_name FROM tool_rental.tool t JOIN tool_rental.category c ON c.id = t.category_id LEFT JOIN tool_rental.profile p ON p.user_id = t.owner_id LEFT JOIN tool_rental.location l ON l.id = p.location_id LEFT JOIN tool_rental.district d ON d.id = l.district_id LEFT JOIN tool_rental.city ci ON ci.id = d.city_id WHERE t.status = 'A'  AND ci.city_name = 'Tallinn' AND d.district_name = 'Kristiine' AND c.category_name = 'Koristamine' ORDER BY t.id LIMIT 20
INFO 15196 --- [backend] [nio-8080-exec-9] e.t.infrastructure.ai.AiMetricsAdvisor   : [AiMetrics] llm_call in=333 out=50 latencyMs=684 model=gemini-3.5-flash-lite finishReason=STOP
```

Logi näitab ühe küsimuse edukat läbimist algusest lõpuni. Küsimus oli umbes „Kas Tallinnas Kristiines on koristamise tööriistu?“.

**1. Nimede laadimine (3 SQL-päringut)**

Sama mis näites 1. Linnade, linnaosade ja kategooriate nimed loetakse andmebaasist, et need intent-prompti panna.

**2. Intent-kutse Geminile: `in=649 out=65 latencyMs=991`**

Gemini sai prompti koos few-shot näidete, loendite ja küsimusega ning tagastas umbes sellise JSON-i:

```json
{"reasoning": "...", "toolSearch": true, "city": "Tallinn", "district": "Kristiine", "category": "Koristamine"}
```

Sisend on suur (649), sest selles on system-prompt ja kõik loendid. Väljund on väike (65), sest see on lühike JSON. Regex-kontroll läks selle kutse eel läbi, sest blokeerimise rida logis ei ole.

**3. Tööriistade otsing (`AvailableToolRepository.findAvailable`)**

Seda päringut teeb SQL, mitte AI. Gemini andis ainult filtrite väärtused ja need kontrolliti enne andmebaasi loendite vastu (`canonicalName()`).

| SQL osa | Tähendus |
|---|---|
| `SELECT t.id AS tool_id, t.name, c.category_name, ci.city_name, d.district_name` | Ainult viis avalikku välja, mis lähevad `AvailableTool`'i. E-posti ega telefoni ei võeta. |
| `FROM tool t JOIN category c` | Iga tööriist koos kategooria nimega |
| `LEFT JOIN profile p ON p.user_id = t.owner_id` | Tööriista omaniku profiil |
| `LEFT JOIN location l` → `district d` → `city ci` | Omaniku aadressist linnaosa ja linn. Tööriista asukoht on omaniku asukoht. |
| `WHERE t.status = 'A'` | Ainult saadaval tööriistad, `U` (kasutusest väljas) jäetakse välja |
| `AND ci.city_name = 'Tallinn' AND d.district_name = 'Kristiine' AND c.category_name = 'Koristamine'` | Gemini tuvastatud filtrid. Iga filter lisatakse ainult siis, kui see leiti. Tööriista nime korral lisandub `AND t.name ILIKE '%Akutrell%'`. |
| `ORDER BY t.id LIMIT 20` | Järjestus ID järgi, kuni 20 tulemust |

**Väärtused on päringus näha, aga SQL-süsti ohtu pole.** Koodis on päring parameetritega (`:city`, `:district`, `:category`, `:toolName`). P6Spy paneb logisse ainult loetavuse huvides tegelikud väärtused. Andmebaasi läheb päring koos eraldi parameetritega, seega kasutaja tekst ei saa SQL-i muuta.

`3_import.sql` andmetel vastab sellele tingimusele üks tööriist: Survepesur (`toolId=6`, Marko Tamm, Kristiine).

**4. Vastuse kutse Geminile: `in=333 out=50 latencyMs=684`**

Gemini sai küsimuse ja leitud tööriistad märgendis `<tools>` ning sõnastas vastuse, näiteks „Kristiines on saadaval Survepesur (Koristamine).“. Sisend on väiksem (333), sest loendeid ega few-shot näiteid siin ei ole, ainult one-shot näide ja leitud tööriistad. `getSafeAnswer()` kontrollis vastust ja see läks läbi muutmata kujul.

**5. Vastus kliendile**

`ToolAskResponse`, milles on `answer` ja `tools` list (andmebaasist, mitte AI-lt). Frontend kuvab need vestlusaknas.

**Kokku:** 4 SQL-päringut ja 2 Gemini kutset, umbes 980 tokenit (649 + 65 + 333 + 50) ja AI-osa kestus umbes 1,7 sekundit (991 + 684 ms). `finishReason=STOP` mõlemal kutsel tähendab, et kumbki vastus ei jäänud tokenipiiri tõttu poolikuks.

## `Tool search intent` rida (debug)

Rida tuleb ainult siis, kui `ToolAskService` logitase on `DEBUG` (`logging.level.ee.toolrental.service.ToolAskService=DEBUG` failis `application.properties`). See näitab, mida Gemini esimene kutse küsimusest tuvastas:

```text
Tool search intent: reasoning=Küsitakse koristamist Kristiines. city="Tallinn" district="Kristiine" category="Koristamine" toolName="null"
```

| Väli | Tähendus |
|---|---|
| `reasoning` | Mudeli põhjendus (chain of thought), kliendile seda ei saadeta |
| `city="Tallinn"` | Jutumärkides on Gemini antud tekst |
| `toolName="null"` või `toolName=""` | Gemini andis puuduva väärtuse tekstina. Kood muudab selle päris `null`'iks (`normalizeIntentValue()`), seega filtrit ei lisata. |
| `toolName=null` (ilma jutumärkideta) | Väli puudus Gemini vastusest täiesti |

Samal tasemel võivad tulla ka read `Tool name not found in question, ignored` ja `Tool name not found in database, ignored`. Need tähendavad, et Gemini pakutud tööriista nime ei olnud küsimuses või andmebaasis ning otsing tehti ilma selleta.

## Kiirülevaade: mida logist otsida

| Logis on | Tähendab |
|---|---|
| 3 SQL-i, 2 × `[AiMetrics]`, nende vahel tööriistade SQL | Edukas otsing, tulemused leiti |
| 3 SQL-i, 1 × `[AiMetrics]`, tööriistade SQL-i pole | Küsimus ei olnud tööriistade kohta või nime ei leitud. Vastus on 200 koos selgitava tekstiga. |
| 3 SQL-i, 1 × `[AiMetrics]`, tööriistade SQL, 2. `[AiMetrics]` puudub | Filtritele vastavaid tööriistu ei leitud. Vastuse kutset ei tehta. |
| 3 SQL-i, `[RegexJailbreak] blocked`, `Guardrail blocked request` | Küsimus lükati tagasi (400 `QUESTION_REJECTED`), Gemini kutset ei tehtud |
| `finishReason=MAX_TOKENS` | Vastus lõigati `maxOutputTokens` piiril |
| `Unsafe AI answer replaced` | `getSafeAnswer()` asendas vastuse, sest see oli liiga pikk või sisaldas e-posti, telefoni või URL-i |
