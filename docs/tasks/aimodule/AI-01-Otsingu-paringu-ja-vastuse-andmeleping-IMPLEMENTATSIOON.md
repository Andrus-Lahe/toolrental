# AI-01 — Otsingu päringu ja vastuse andmeleping — implementatsiooni plaan

**Seotud task:** [AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md](./AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskRequest.java` — request record väljal `question`, valideerib `@NotBlank` ja maksimaalselt 500 märki.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskController.java` — olemasolev `POST /api/ai/ask`, kasutab `@Valid` request body't ning kutsub `ToolAskService`-it.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskResponse.java` — praegune vastus sisaldab `answer` stringi ning `AvailableTool` kirjete loendit.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableTool.java` — olemasolevad väljad on `toolId`, `name`, `category`, `city` ja `district`; lepingus soovitud ühe `location` välja asemel on linn ja linnaosa eraldi.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableToolRepository.java` — praegune otsing tagastab tööriistad ning teenus lisab neile AI-ga koostatud vabatekstilise vastuse.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/AiSearchView.vue` — saadab sama küsimusekuju, kuid loeb vastusest `answer` ja `tools`; tööriista nimi pole praegu link.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/router/index.js` — detailmarsruut `/tools/:toolId` on olemas.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java` — AI endpointi katab üldreegel `anyRequest().authenticated()`; sisselogimata päring saab 401 vastuse tühja body'ga.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java` — olemasolev valideerimis- ja üldvea vastuse kuju on `message` + `errorCode`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/3_import.sql` — määravad väljade seosed ning taskis toodud näiteandmed.

Taski AI-01 tulemus on API lepingu fikseerimine. NL→SQL teostus, turvaline andmevaade ja saadavuse filter kuuluvad järgmistesse taskidesse. Frontendi lingi teostus kuulub AI-07 taski.

## Puuduv/muudetav

- Lepingus tuleb fikseerida request `question` väli ja maksimaalne pikkus; praegune Java DTO juba vastab sellele.
- Lepingus tuleb asendada praegune vastuse kirjeldus `answer` + `tools` lepinguga vastusega `tools[]`, kus igal kirjel on `toolId`, `name`, `category` ja `location`.
- Leping peab fikseerima tühja tulemuse, valideerimisvea, autentimise ja serverivea käitumise.
- Lepingus tuleb selgelt öelda, et `toolId` on UI detailmarsruudi jaoks ning isikuandmeid API vastusesse ei lisata.
- Puuduva omaniku profiili või asukoha korral jääb sobiv tööriist tulemusse ja `location` väärtus on `null`.
- Tootmiskoodi ei muudeta selle dokumentatsioonitaski käigus. API DTO ja Vue vaate teostus tehakse neile eraldatud hilisemates taskides.

## Sammud

1. **Fikseeri request leping** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md`
   - Kasuta olemasolevat `POST /api/ai/ask` endpointi ja `question` request välja.
   - Kirjelda kohustuslikkust ning 500 märgi piirangut vastavalt `ToolAskRequest` DTO-le.

2. **Fikseeri response DTO leping** — sama fail.
   - Kirjelda juurtaseme `tools` massiivi ning välju `toolId`, `name`, `category`, `location`.
   - Kasuta JSON näites `docs/database/3_import.sql` tegelikke tööriista-, kategooria- ja asukohaväärtusi.
   - Määra, et vastuse eesmärk on struktureeritud loend; vaba `answer` teksti genereerimine pole selle lepingu osa.
   - Määra puuduva profiili või asukoha korral `location: null`, jättes tööriista tulemusse.

3. **Fikseeri vead ja tühitulemus** — sama fail.
   - Kirjelda `400` valideerimisvea ning olemasoleva `ApiError` kuju.
   - Kirjelda `401` olemasoleva turvakonfiguratsiooni järgi ning `200` tühja loendiga vaste puudumisel.
   - Lisa `500` üldvea olemasolev leping, ilma SQL-i, stack trace'i või isikuandmete avaldamiseta.

4. **Kontrolli lepingu kooskõla olemasoleva süsteemiga** — failid: eelmises jaotises loetletud request/response DTO, controller, frontend vaade, ruuter ja turvaseadistus.
   - Märgi selgelt, millised väljad juba eksisteerivad ja millised vajavad hilisemas teostuses DTO muudatust.
   - Hoia API lepingu dokumentatsioon lahus tulevastest töödest, mis muudavad LLM-i prompti, SQL käivitamist või UI linke.

## Veakäsitlus

AI-01 ei muuda käitusaja veakäsitlust. Dokumenteeritakse olemasolev leping: DTO valideerimise viga tagastab `400` ja `INCORRECT_INPUT`; sisselogimata AI päring tagastab `401` tühja body'ga; ootamatu serveriviga tagastab `500` olemasoleva `ApiError` kujul. Otsingu sisulised vead ning SQL-i kontrolli erindid lahendatakse hilisemates teostustaskides.

## Testid

See task fikseerib API lepingu dokumentatsioonis ega muuda koodi, seega käitusaja teste selle taski käigus ei lisata ega käivitata. Hilisem DTO/controller teostustask peab kontrollima JSON väljade täpset kuju, `toolId` säilimist, `location` väärtust, tühja loendit ja olemasolevaid 400/401/500 lepinguid.

## Avatud küsimused

- Kas `location` jääb linnaosa nimeks (näiteks `Kristiine`) või peab tulevikus sisaldama ka linna, et eristada sama nimega linnaosi eri linnades?
- Backend `ToolAskResponse` ja frontend `AiSearchView.vue` kasutavad praegu `answer` ning `tools` koos `city` ja `district` väljadega. Uus leping muudab seda kuju; muudatuse teostus ja kooskõlastatud väljalase kuuluvad hilisematesse AI taskidesse.
