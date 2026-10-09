# AI-05 — SQL-i kontroll ja käivitamine — implementatsiooni plaan

**Seotud task:** [AI-05-SQL-i-kontroll-ja-kaivitamine.md](./AI-05-SQL-i-kontroll-ja-kaivitamine.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — juhib nüüd NL→SQL generaatori, validaatori ja piiratud repository järjestust.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlGenerator.java` — AI-04 generaator annab mudeli väljundi ja `CANNOT_ANSWER` markeri.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchQueryRepository.java` — käivitab ainult validaatori loodud `ValidatedSql` väärtuse Spring JDBC kaudu ning loeb ainult vaadet.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskController.java` — endpoint delegeerib töö service kihile.
- AI-02 vaade `tool_rental.ai_tool_search` ja AI-04 generaatori leping on olemas.
- Vana `AvailableToolRepository` ning `ToolSearchIntent`-i käitusvoog olid enne AI-05 ühendust kasutuses; nende baastabelipõhine otsing eemaldatakse.

Testide lisamine ja kasutamine on kasutaja poolt lubatud.

## AI-05 tulemus

- Validaator lubab ainult generaatori praeguse piiratud päringukuju.
- AI-06 viis `AvailableTool` kaardistuse AI-01 JSON lepingule vastavaks DTO-ks.
- Vana intent-põhine repository ja DTO eemaldati; paralleelset AI otsinguteed ei ole.

## Sammud

1. **Eralda SQL-i kontroll komponendiks** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlValidator.java`
   - Aktsepteeri ainult täpne projektsioon, üks lubatud vaade, lihtsad lubatud filtrid, saadavuse tingimus ning `ORDER BY tool_id LIMIT 1..20`.
   - Lükka tagasi kommentaarid, mitmikpäringud, muu tabel, JOIN, alampäring, funktsioon, `OR` ja tundmatu süntaks.
   - Eduka kontrolli tulemus on eraldi `ValidatedSql` tüüp; tagasilükatud väljund ei jõua repositoryni.

2. **Lisa piiratud päringu käivitamine** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchQueryRepository.java`.
   - Kasuta Spring JDBC olemasolevat konfiguratsiooni ja kuva vaadet.
   - Map'i ainult ID, tööriista nimi, kategooria ja linnaosa; isikuandmete välju ei loeta.

3. **Ühenda pipeline teenuses** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java`.
   - Järjekord: AI-04 genereerib → `CANNOT_ANSWER` kontroll → validator kontrollib → repository käivitab → service tagastab tulemused.
   - `CANNOT_ANSWER` ei lähe validaatorisse ega andmebaasi.
   - Ära lisa teist LLM-i kutset vastuse proosaks vormistamiseks; praeguse API ajutine vastusetekst on backendis deterministlik.

4. **Kasuta olemasolevat API veakäsitlust** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`.
   - Valideerimis- ja JDBC erindid lähevad olemasolevasse üldisesse handlerisse, mis logib serveris ja annab kliendile üldise 500 `ApiError` vastuse.
   - SQL-i, DB sõnumit ega stack trace'i ei tagastata. Mittetoetatud küsimus saab AI-08 järgi eraldi 422 vastuse.

## Veakäsitlus

Kontrolli tagasilükkamine ei tohi langeda tühja otsingutulemusena. DB vea puhul kasutab endpoint olemasolevat üldist serverivastust. Kasutajale ei tagastata mudeli SQL-i ega andmebaasi tehnilist sõnumit.

## Testid

`ToolSearchSqlValidatorTest` katab lubatud SQL-i, mitte-SELECTi, mitmikpäringu, vale vaate, alampäringu, puuduva saadavusfiltri, vale projektsiooni, väljaspool sulge kasutatud `OR`-i, kommentaari, funktsiooni ja üle 20 limiidi tagasilükkamise. AI-12 lisab lubatud veergude sulgudes OR-rühmade kontrolli. `ToolAskServiceTest` kontrollib genereeri→käivita järjestust ning et `CANNOT_ANSWER` ja invaliidne SQL repositoryni ei jõua. `ToolSearchQueryRepositoryTest` kasutab päris PostgreSQL-i ja kontrollib vaatepäringu tulemuskaardistust.

Kontrolli tulemus: viis AI mooduli sihttestiklassi läbisid PostgreSQL-ühendusega kokku 17 testi. Pärast generaatori prompti täpsustamist läbis `ToolSearchSqlGeneratorTest` uuesti kõik 4 testi. Saadavuse `C` ning `P`/`R` reegleid kontrollitakse päris vaate kaudu `ToolSearchQueryRepositoryTest`-is.

## Teostuse piir

Validaator kasutab väikest tokenizer/parserit, mitte SQL-i märksõnade regex-otsingut ega üldotstarbelist SQL-parseri teeki. See lubab otsingufiltreid ning ainult sulgudes OR-rühmi lubatud veergudel, jättes ülejäänud SQL süntaksi keelatuks. `CANNOT_ANSWER` tagastatakse HTTP 422 vastusena koodiga `AI_UNSUPPORTED_QUESTION`; valideerimis-, mudeliteenuse ja andmebaasivead tagastatakse üldise HTTP 500 veana. AI-06 asendas `answer` + `AvailableTool` vastuse JSON lepinguga.
