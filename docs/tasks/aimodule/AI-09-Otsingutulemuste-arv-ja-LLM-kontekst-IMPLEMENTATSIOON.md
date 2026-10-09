# AI-09 — Otsingutulemuste arv ja LLM-i kontekst — implementatsiooni plaan

**Seotud task:** [AI-09-Otsingutulemuste-arv-ja-LLM-kontekst.md](./AI-09-Otsingutulemuste-arv-ja-LLM-kontekst.md)

## Teostatud

- `ValidatedSql` kannab parseri eraldatud filtrirühmi ning `ToolSearchQueryRepository.searchWithCounts` koostab neist nimetatud parameetritega loendused.
- AI vaate `is_visible` loendab ainult `A` olekus tööriistu; `is_available_now` järgib AI-03 reeglit. Tulemuste loend jääb valideeritud SQL-i `LIMIT 1..20` piiresse.
- `ToolSearchSummary` koondab tööriistad, täpsed arvud ja ainult üheselt tuvastatud asukoha/kategooria.
- Teste ei käivitatud. `backend` mooduli `compileJava` läbis.

## Algne hetkeseis

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — juhib küsimuse SQL-iks genereerimist, valideerimist ja andmebaasist saadaval tööriistade otsimist.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlValidator.java` — lubab ainult üht kitsast päringukuju piiratud `tool_rental.ai_tool_search` vaate vastu. `ValidatedSql` hoiab praegu ainult valideeritud SQL stringi; parser nõuab `is_available_now = TRUE` tingimust ja `LIMIT 1..20`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchQueryRepository.java` — käivitab valideeritud päringu JDBC kaudu ja kaardistab tulemused `AvailableTool` kirjeteks.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableTool.java` — sisaldab API loendi jaoks `toolId`, `name`, `category` ja `location` väärtusi.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskResponse.java` — AI-06 lepingu järgi sisaldab praegu ainult `tools` massiivi.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` — piiratud vaade annab tööriista, kategooria, linnaosa ja `is_available_now` välja. Saadavus on AI-03 reegli järgi; vaade ei anna eraldi ohutut nähtavuse lippu, mis eristaks `U` olekus tööriista praegu rendis olevast `A` tööriistast.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-03-Praeguse-saadavuse-reegel.md` — määrab saadaval tööriista reegli ning kinnitab, et `U` olekus tööriista ei tagastata.

## Puuduv/muudetav

- Valideeritud otsingukriteeriumid tuleb eraldada AI genereeritud SQL tekstist kujule, mida backend saab ohutult kasutada nii tulemuste päringuks kui ka loenduseks.
- Piiratud andmevaade või selle serveripoolne loendus peab eristama nähtavat tööriista (`A`) praegu saadaval olemisest. Toorest `tool.status` väärtust ei ole vaja mudelile avaldada; piisab minimaalsest ohutust tähisest.
- JDBC repository peab andma tagasi täpse `totalToolCount` ja `availableToolCount` väärtuse samade otsingufiltrite alusel ning jätma saadaval tööriistade loendi 20 kirje piirangu jõusse.
- Service vajab sisemist kokkuvõtte mudelit, mis sisaldab arvusid ja vastuse genereerimiseks vajalikke ohutuid otsingufakte. Avalik `ToolAskResponse` ei muutu selles taskis.
- Loenduse ja filtrite rakendamist tuleb testida päris andmebaasi vaate vastu või olemasoleva repository testimise mustriga.

## Sammud

1. **Lisa valideeritud otsingu tingimuste mudel** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlValidator.java`.
   - Muuda parserit nii, et see tagastaks lisaks käivitamiseks lubatud päringu kujule ka struktureeritud lubatud filtrid (`toolId`, tööriista nimi, kategooria, asukoht).
   - Säilita praegused turvapiirid: lubatud vaade ja veerud, lihtsad tingimused, ainult `AND`, kohustuslik saadavuse tingimus mudeli loendi päringus ning limiit vahemikus 1..20.
   - Loenduspäringud koosta backendis sellest struktureeritud filtrite mudelist parameetritud päringuna; ära eemalda ega asenda SQL tekstis tingimusi stringioperatsioonidega.

2. **Erista vaates nähtavus ja praegune saadavus** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` ning sellega kooskõlas olev AI-02 task.
   - Lisa piiratud vaatesse minimaalne ohutu boolean väli, mille järgi saab loendada nähtavaid `A` olekus tööriistu ka siis, kui nende `is_available_now` väärtus on `false` tänase `C` broneeringu tõttu.
   - Hoia `U` olekus tööriistad koguarvust väljas.
   - Ära lisa vaatesse kasutajanimesid, e-posti, `google_sub`, omaniku/rentniku ID-sid, aadressi ega koordinaate.
   - Uuenda AI-02 vaate dokumenteeritud veerge ja skeemi loomise teste, kui need testid vaadet kontrollivad.

3. **Lisa täpne arvutus repository kihti** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchQueryRepository.java`.
   - Kasuta samu valideeritud kasutajafiltreid mõlema arvu jaoks.
   - `totalToolCount` loendab nähtavaid vasteid sõltumata `is_available_now` väärtusest.
   - `availableToolCount` loendab nende vastete alamhulka, millel `is_available_now = TRUE`.
   - Tagasta endiselt kuni 20 järjestatud `AvailableTool` kirjet; ära arvuta koguarvu selle piiratud loendi pikkusest.
   - Kasuta nimetatud SQL parameetreid ja piiratud vaadet; ära käivita LLM-i SQL-i muutmata kujul loenduspäringuna.

4. **Koosta service kihi otsingukokkuvõte** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` ja uus sisemine kokkuvõtte klass `ToolSearchSummary.java`.
   - Koonda repository arvud, saadaval tööriistad ning ainult vastuse sõnastamiseks vajalikud otsingufaktid.
   - Lisa asukoht või muu kasutatud filter kokkuvõttesse ainult siis, kui see päringu tingimustest üheselt selgub.
   - Ära saada selle kokkuvõtte osana isikuandmeid ega loo veel teist LLM kutset.
   - Hoia avalik response DTO selles taskis muutmata; selle uuendamine kuulub hilisemasse API lepingu taski.

5. **Lisa loenduse ja kokkuvõtte testid** — backend testid `ee.toolrental.aimodule` paketis.
   - Kontrolli, et tänase `C` broneeringuga `A` tööriist kuulub koguarvu, kuid mitte saadaval arvu ega tööriistaloendisse.
   - Kontrolli, et saadaval olev `A` tööriist kuulub mõlemasse arvu.
   - Kontrolli, et `U` olekus tööriist ei kuulu kumbagi arvu.
   - Kontrolli, et `P` ja `R` broneeringud ei muuda AI-03 järgi saadavust.
   - Kontrolli, et 20 tulemuse kuvamispiir ei kärbi koguarvu ega saadaval arvu ning sama asukoha/kategooria filtrid rakenduvad mõlemale loendusele.
   - Kontrolli null vasteid ning et keeldutud/invaliidne SQL ei jõua andmebaasi.

## Veakäsitlus

Valideerimata filtrid ei tohi jõuda loenduspäringusse. Loenduse või tulemuste JDBC tõrge liigub olemasolevasse üldisesse handlerisse ja tagastub AI-08 järgi `500 INTERNAL_SERVER_ERROR` üldise sõnumiga. Mittetoetatav küsimus säilitab AI-08 `422 AI_UNSUPPORTED_QUESTION` käitumise. Null tulemust tagastab nullarvudega eduka otsingu, mitte vea.

## Testid

Täienda `ToolSearchSqlValidatorTest`-i valideeritud filtrite eraldamise kontrollidega ning `ToolSearchQueryRepositoryTest`-i loendusjuhtudega piiratud AI vaate vastu. Täienda `ToolAskServiceTest`-i kontrollima, et teenus koondab õiged loendusarvud ja ohutud faktid, kuid ei muuda vastuseskeemi ega käivita loenduse jaoks LLM-i.

Kasuta päris PostgreSQL-i olemasoleva repository integratsioonitesti mustri järgi, et kontrollida vaate `is_available_now` ja uue nähtavuse välja kooskõla AI-03 reegliga.

## Avatud küsimused

Taski käitumisreeglid on arutelus kokku lepitud: koguarv hõlmab nähtavaid `A` tööriistu, kaasa arvatud praegu rendis olevad; saadaval arv järgib AI-03 reeglit. Avaliku API ja kasutajaliidese muudatused on eraldi järgmiste taskide ulatus.
