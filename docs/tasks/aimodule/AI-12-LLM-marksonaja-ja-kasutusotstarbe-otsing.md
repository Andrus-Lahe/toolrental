# AI-12 — LLM-i märksõna- ja kasutusotstarbe otsing

**Seotud taskid:** [AI-02 — Piiratud otsinguandmevaade](./AI-02-Piiratud-otsinguandmevaade.md), [AI-03 — Praeguse saadavuse reegel](./AI-03-Praeguse-saadavuse-reegel.md), [AI-04 — Loomuliku keele küsimusest SQL-päringuni](./AI-04-Loomuliku-keele-kusimusest-SQL-paringuni.md), [AI-05 — SQL-i kontroll ja käivitamine](./AI-05-SQL-i-kontroll-ja-kaivitamine.md), [AI-09 — Otsingutulemuste arv ja LLM-i kontekst](./AI-09-Otsingutulemuste-arv-ja-LLM-kontekst.md)

## Eesmärk

Lubada kasutajal otsida nii küsimuse, üksiku märksõna kui ka kirjeldatud vajaduse järgi. LLM peab ise mõistma päringu tähendust ja koostama selle põhjal SQL-i ainult lubatud otsinguandmevaate vastu. Näiteks peab „labidas” leidma tegeliku sobiva tööriista ning „millega ma saan kaevata?” võib leida labida tööriista kirjelduse või nime põhjal.

Otsingu tulemus peab sisaldama ainult andmebaasis olemasolevaid, AI-03 järgi praegu saadaval olevaid tööriistu. LLM ei tohi tööriistu välja mõelda ega SQL-i otse andmebaasiühendusega käivitada; rakendus valideerib iga päringu enne käivitamist.

## LLM-i lubatud otsinguandmed

LLM-i SQL päringu lubatud allikas jääb AI-02 piiratud vaateks `tool_rental.ai_tool_search`. Vaade peab võimaldama päringus kasutada tööriista nime, kategooriat, asukohta, saadavust ning tööriista kirjeldust.

- `tool_description` on tööriista leidmiseks kasutatav tootetekst, mitte kasutaja profiiliandmed.
- Tööriista kirjeldus on kasutaja sisestatud vabatekst. Kui kirjeldust või selle osa saadetakse mudelile väärtusena, tuleb sealt enne eemaldada isikuandmed, sealhulgas nimed, e-posti aadressid, telefoninumbrid, `google_sub`, omaniku/rentniku ID-d, täpsed aadressid ja muud isikut tuvastada võimaldavad väärtused.
- Kirjeldust võib kasutada SQL-i `WHERE` filtris, kuid see ei kuulu tööriista projektsiooniväljundisse ega API `tools` kirjesse.
- LLM-ile ei anta ligipääsu algtabelitele ega teistele vaadetele. Lubatud veerud ei laiene kasutajate või broneerijate isikuandmetele.

## Päringu tõlgendamine

- Üksik sõna või lühike fraas, näiteks `labidas`, on tööriistaotsingu sisend, mitte automaatselt mittetoetatav küsimus.
- Kasutusotstarvet väljendav päring, näiteks `millega ma saan kaevata?`, tuleb tõlgendada sobivate tööriistade otsinguna. LLM valib ise päringus sobivad lubatud väljad ja tingimused, sealhulgas nime, kategooria või kirjelduse.
- Asukoha või kategooria nimetamisel tuleb neid tingimusi koos semantilise otsinguga arvestada.
- Saadavusfilter peab järgima AI-03 reeglit. Tulemus ei tohi soovitada `U` tööriista ega tänase kattuva `C` broneeringuga tööriista.
- LLM-i loogikat ei asendata Java koodis ette kirjutatud eestikeelsete sõnade või sünonüümide tabeliga.

## SQL-i valideerimine ja üks parandamiskatse

Java validaator jääb turvapiiriks. Päring võib kasutada ainult piiratud vaadet ja selles taskis lubatud veerge ning peab jääma otsinguks vajalikku lugemisformaati. Keelatud on algtabelid, kirjutavad päringud, kommentaarid, mitmikpäringud, andmebaasifunktsioonid ja muu valideerimata SQL.

Kui esimene genereeritud SQL ei läbi validaatorit:

1. backend ei käivita seda;
2. backend saadab LLM-ile sama algse küsimuse ja ühe kontrollitud, tehnilisi andmeid mitte sisaldava valideerija tagasiside;
3. LLM saab teha ühe parandatud päringu katse;
4. backend valideerib parandatud päringu uuesti ja käivitab ainult selle, kui kontroll läbitakse.

Parandamiskatse ei tohi sisaldada andmebaasi veateadet, stack trace'i ega piirangutest välja jäävat tabeli- või isikuandmestiku infot. Andmebaasi käivitusviga ei põhjusta SQL-i automaatset kordusgenereerimist. Kui teine päring lükatakse tagasi, kasutatakse AI-08 üldist HTTP 500 veakäsitlust. `CANNOT_ANSWER` säilitab AI-08 `422 AI_UNSUPPORTED_QUESTION` käitumise ning selle puhul parandamiskatset ei tehta.

## Vastuvõtu kriteeriumid

- [ ] Lühike märksõna või fraas saab tööriistaotsingu päringuna töödelda.
- [ ] LLM suudab tõlgendada loomulikus keeles kirjeldatud tööriista kasutusotstarvet ja koostab selle põhjal SQL-i lubatud vaate vastu.
- [ ] SQL generaatori system prompt kirjeldab `tool_description` välja otsinguks lubatud veeruna ning sisaldab märksõna ja kasutusotstarbe näiteid.
- [ ] Tööriista kirjeldust võib kasutada filtrina, kuid seda ei lisata `SELECT` väljundisse ega API `tools` kirjetele.
- [ ] Kui tööriista kirjeldus saadetakse mudelile väärtusena, eemaldatakse sellest enne saatmist ülesandes nimetatud isikuandmed; testiandmed tõendavad puhastamist.
- [ ] SQL validaator toetab semantilise otsingu jaoks vajalikke komposiitfiltreid lubatud veergudel, säilitades piiratud vaate, saadavuse, limiidi ja lugemisõiguse piirangud.
- [ ] Esimest valideerimata päringut ei käivitata.
- [ ] Valideerija tagasilükkamise järel lubatakse maksimaalselt üks parandatud SQL-i genereerimine; parandatud päring valideeritakse enne käivitamist uuesti.
- [ ] Valideerija tagasiside on fikseeritud ohutu info ega sisalda SQL-i käitusviga, stack trace'i või isikuandmeid.
- [ ] Andmebaasi- või võrgutõrge ei käivita SQL-i parandamise korduskatset.
- [ ] `CANNOT_ANSWER` korral säilib HTTP 422 ning SQL-i ega vastuse parandamiskutset ei tehta.
- [ ] Kui paranduskutse tagastab `CANNOT_ANSWER`, säilib samuti HTTP 422 ja päringut andmebaasi ei saadeta.
- [ ] Lõpptulemus sisaldab ainult andmebaasist leitud ja AI-03 järgi praegu saadaval tööriistu; LLM-i oletatud tööriista ei kuvata.
- [ ] Kui parandamiskatse ei anna valideeritavat päringut, kasutatakse AI-08 üldist 500 veavastust ilma tehniliste detailideta.

## Testitavad näited

| Kasutaja sisend | Oodatav käitumine |
|---|---|
| `labidas` | LLM koostab nime/kirjelduse järgi sobiva päringu; vastuseks tulevad ainult tegelikud saadaval tööriistad. |
| `millega ma saan kaevata?` | LLM tõlgendab kasutusotstarbe ja võib leida sobiva labida kirjelduse või nime järgi. |
| `labidas Kristiines` | LLM koostatud päring arvestab nii tööriista sobivust kui ka asukohta. |
| päring, mis toodab esimesel katsel keelatud SQL-i | SQL-i ei käivitata; LLM saab ühe ohutu tagasiside ja parandab päringu. |
| parandatud SQL lükatakse samuti tagasi | Üldine HTTP 500, detailset SQL-i kasutajale ei näidata. |

## Veaolukorrad

| Olukord | Käitumine |
|---|---|
| Tööriistaotsingu märksõna ei leia sobivat tööriista | Edukas otsing tühja tulemusega, mitte SQL-i valideerimisviga. |
| LLM väljastab esimesel korral lubamatu SQL-i | Päringut ei käivitata; tehakse üks parandamiskatse ohutu valideerija tagasisidega. |
| Teine SQL ei läbi valideerimist | AI-08 üldine HTTP 500 vastus. |
| Paranduskutse tagastab `CANNOT_ANSWER` | HTTP 422 `AI_UNSUPPORTED_QUESTION`; SQL-i ei käivitata. |
| LLM ei saa vaate põhjal küsimusele vastata | `CANNOT_ANSWER` → HTTP 422 `AI_UNSUPPORTED_QUESTION`; parandamiskatset ei tehta. |
| Mudeliteenus või andmebaas ebaõnnestub | AI-08 üldine HTTP 500 vastus; DB veateadet mudelile SQL-i parandamiseks ei saadeta. |

## Seotud taskide uuendused

Implementatsioon peab joondama AI-02 piiratud vaate veerud, AI-04 prompti ja AI-05 SQL-i grammatika selle taski lubatud otsinguulatusega. AI-09 loenduste filter peab kasutama samu valideeritud kasutaja tingimusi, kuid koguarvu arvutusest eemaldatakse ainult saadavustingimus.
