# AI-12 — LLM-i märksõna- ja kasutusotstarbe otsing — implementatsiooni plaan

**Seotud task:** [AI-12-LLM-marksonaja-ja-kasutusotstarbe-otsing.md](./AI-12-LLM-marksonaja-ja-kasutusotstarbe-otsing.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlGenerator.java` — annab Gemini `ChatClient`-ile kasutaja küsimuse ning kitsendatud SQL system prompti. Prompt ei kirjelda praegu `tool_description` välja ega üksiksõna/kasutusotstarbe näiteid.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlValidator.java` — aktsepteerib üht fikseeritud `SELECT` projektsiooni, üht vaadet, lihtsaid `AND`-tingimusi ja `LIMIT 1..20`. `OR` ja sulud lükatakse tagasi.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — genereerib SQL-i, valideerib selle ja annab selle repositoryle. Validaatori vea korral korduskatset praegu ei tehta.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchQueryRepository.java` — käivitab valideeritud SQL-i ainult `tool_rental.ai_tool_search` vaate vastu.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` — piiratud vaade sisaldab praegu tööriista ID-d/nime, kategooriat, linnaosa ja `is_available_now`; `tool.description` pole vaates.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/InvalidToolSearchSqlException.java` — üldine valideerimise erind; ohutu korduskatse tagasiside tüüpi praegu pole.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/aimodule/ToolSearchSqlGeneratorTest.java`, `ToolSearchSqlValidatorTest.java` ja `ToolAskServiceTest.java` — olemasolevad prompti, grammatika ja käitusjärjestuse testid.
- Isikuandmeid automaatselt eemaldav tööriistakirjelduse sanitizerit `backend/src/main` ega sellega seotud testiotsingus ei leitud.
- AI-03 määrab saadavusreegli; AI-08 määrab 422 mittetoetatava küsimuse ja üldise 500 vea lepingud.

## Puuduv/muudetav

- Piiratud vaate ja AI-04 prompti otsinguväljade hulka tuleb lisada tööriista kirjeldus.
- LLM-ile tuleb õpetada, et üksiksõna ja kasutusotstarve võivad tähendada tööriistaotsingut.
- Validaatori ohutut SQL grammatikat tuleb laiendada ainult selliste kombinatsioonide jaoks, mida semantiline otsing vajab.
- `ToolAskService` peab valideerimise tagasilükkamisel andma LLM-ile ühe ohutu paranduse võimaluse ja tagama, et käivitatakse ainult valideeritud SQL.
- Kirjeldusest mudelile saadetavate väärtuste puhul puudub PII puhastamise komponent ja selle testid.
- AI-02, AI-04, AI-05 ja AI-09 taski tehniline kirjeldus vajab kooskõlla viimist.

## Sammud

1. **Laienda piiratud vaate otsinguvälja** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql`.
   - Lisa vaatesse tööriista kirjeldus `tool.description` alusel, kuid säilita AI-02 privaatsuspiirangud.
   - LLM-i SELECT projektsioon peab jääma API tulemuseks vajalike väljade juurde; kirjeldust kasutatakse ainult otsingutingimuses.
   - Kui mõni kirjeldus saadetakse mudelile väärtusena, lisa eraldi serveripoolne sanitiseerimiskomponent ja ära saada puhastamata kirjeldust. Kui LLM saab ainult skeemi ning kirjeldus filtreeritakse DB sees, ära tagasta kirjeldusväärtust mudelile ega kliendile.
   - Uuenda AI-02 taski vaate skeemi.

2. **Uuenda SQL generaatori prompti** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlGenerator.java`.
   - Kirjelda lubatud `tool_description` otsinguvälja.
   - Lisa lühikese fraasi ning kasutusotstarbe sisendite näited; nõua küsimuse tähenduse järgi sobivate lubatud väljade valimist.
   - Hoia `CANNOT_ANSWER` ainult nende päringute jaoks, mis pole tööriistade otsingud ega ole vaate alusel lahendatavad.
   - Hoia alati saadavusfilter AI-03 reegliga kooskõlas ja väljund üks SQL päringuna.

3. **Laienda validaatorit minimaalse ohutu grammatikaga** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlValidator.java`.
   - Lisa lubatud filtriveergude hulka `tool_description`.
   - Luba ainult dokumenteeritud võrdlusfiltrid ning nende vajalikud rühmad/loogilised ühendused (näiteks kontrollitud `OR`-harud sulgudes), piiramata valikprojektsiooni, tabelit, kohustuslikku saadavustingimust, `ORDER BY tool_id` ega `LIMIT 1..20` reeglit.
   - Jätka kommentaaride, mitmikpäringute, funktsioonide, JOIN-ide, alampäringute, muude tabelite, kirjutavate päringute ja tundmatu süntaksi tagasilükkamist.
   - Väljasta valideerija vea jaoks ohutu fikseeritud tagasisidekood või piiratud kategooria; ära lisa errorisse toor-SQL-i ega andmebaasi teksti.
   - Kontrolli stringliteralide käsitlust ja parameetrite ohutust koos olemasoleva parseri piirangutega.

4. **Lisa üks parandamiskatse teenuse voogu** — failid: `ToolSearchSqlGenerator.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java`.
   - Hoia esimese genereerimise küsimus alles.
   - Ainult `InvalidToolSearchSqlException` korral tee üks uus generaatori kutse, andes kaasa algse küsimuse ja validaatori fikseeritud ohutu tagasiside.
   - Valideeri parandatud päring uuesti; käivita repository ainult teise valideerimise õnnestumisel.
   - Kui paranduskutse tagastab `CANNOT_ANSWER`, lõpeta `422 AI_UNSUPPORTED_QUESTION` vastusega.
   - Lõpliku valideerimistõrke korral lõpeta üldise HTTP 500 käsitlusega. Ära tee lõputut retry't.
   - Ära retry andmebaasi, ühenduse või mudeliteenuse vea korral. Ära kutsu retry'd, kui tulemus on `CANNOT_ANSWER`.

5. **Uuenda AI taskide kokkuleppeid** — failid: `AI-02-Piiratud-otsinguandmevaade.md`, `AI-04-Loomuliku-keele-kusimusest-SQL-paringuni.md`, `AI-05-SQL-i-kontroll-ja-kaivitamine.md` ja `AI-09-Otsingutulemuste-arv-ja-LLM-kontekst.md`.
   - Dokumenteeri kirjeldus otsitava väljana ilma selle lisamiseta API väljundisse.
   - Dokumenteeri märksõna/eesmärgi tõlgendus ning validaatori ühe parandamiskatse piir.
   - Veendu, et AI-09 koguarvu ja saadaval arvu päringud kasutavad samu semantilise otsingu filtreid.

6. **Lisa generaatori, validaatori, teenuse ja andmebaasi testid** — testid `ee.toolrental.aimodule` paketis.
   - ChatClient mockidega kontrolli sõna, lühifraasi ja eesmärgi sisendite prompti ning mudelikutset.
   - Kontrolli kirjeldusfiltri lubamist; keelatud väljade, tabelite ja SQL konstruktsioonide tagasilükkamist.
   - Kontrolli, et esimene vigane SQL ei jõua repositoryni, teise katse valideeritud SQL jõuab, ja maksimaalselt tehakse kaks genereerimiskutset kokku.
   - Kontrolli, et teise katse viga, `CANNOT_ANSWER`, DB viga ja LLM võrgutõrge ei tekita uut ebavajalikku korduskatset.
   - Testi sanitiseerimist isiku nime, e-posti, telefoninumbri, täpse aadressi ja ID tüüpi tundlike väärtuste näidetega, kui kirjeldust saadetakse mudelile.
   - Päris PostgreSQL repository testis kontrolli, et märksõna või kirjelduses oleva kasutusotstarbe järgi leitud vastused on reaalsed ning vastavad AI-03 saadavusele.

## Veakäsitlus

Esimese validaatori tagasilükkamise korral rakendub üks ohutu SQL-i parandamiskatse. Kui ka parandatud päring ei läbi validaatorit, tagastatakse AI-08 järgi üldine `500 INTERNAL_SERVER_ERROR`; kliendile ega teisele mudelikutsule ei anta andmebaasi veateadet ega stack trace'i. Mudeli/DB tõrkeid ei proovita parandada SQL-i uuesti genereerides. `CANNOT_ANSWER` jääb `422 AI_UNSUPPORTED_QUESTION` vastuseks.

## Testid

Täienda `ToolSearchSqlGeneratorTest`, `ToolSearchSqlValidatorTest` ja `ToolAskServiceTest`; lisa repository integratsioonitestid olemasoleva PostgreSQL testi mustri järgi. Kontrolli nii lubatud märksõna- ja kasutusotstarbe otsinguid kui ka keelatud SQL-i, ühekordset retry piiri, `CANNOT_ANSWER` markerit, sanitizerit (kui kirjeldus väärtusena mudelile läheb) ning AI-03 saadavuse säilimist.

## Avatud küsimused

Kasutaja on kinnitanud, et LLM tõlgendab märksõna ja kasutusotstarvet ise ning teeb ühe parandamiskatse validaatori tagasiside põhjal. Tööriista kirjelduse kasutamine on lubatud ainult isikuandmeid eemaldanud kujul, kui kirjeldusväärtus mudelile saadetakse. Täpne grammatika jääb rakenduse valideeritud ja testitud päringukujude alusel piiratuks.
