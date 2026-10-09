# AI-04 — Loomuliku keele küsimusest SQL-päringuni

**Sõltuvad taskid:** [AI-02 — Piiratud otsinguandmevaade](./AI-02-Piiratud-otsinguandmevaade.md), [AI-03 — Praeguse saadavuse reegel](./AI-03-Praeguse-saadavuse-reegel.md)

## Eesmärk

Asendada praegune LLM-i intent/filtrite tuvastamise samm NL→SQL sammuga. LLM saab kasutaja küsimuse ning AI-02 vaate lubatud skeemi kirjelduse ja koostab SQL päringu, mis leiab AI-03 reegli järgi praegu saadaval tööriistad.

LLM ei saa andmebaasiühendust ega isikuandmeid. Ta väljastab päringuteksti; päringu kontroll ja käivitamine kuuluvad AI-05 taski.

## Sisend mudelile

- Kasutaja `question` väärtus olemasolevast `POST /api/ai/ask` request'ist.
- Ainult AI-02 piiratud vaate nimi ja lubatud veergude skeem.
- Saadavuse tähendus AI-03 järgi: `is_available_now = true`.

Prompt ei sisalda rakenduse teiste tabelite skeemi, kasutajate nimesid/e-poste ega andmebaasi päringutulemusi.

## LLM-i väljund

Üks SQL `SELECT` päring AI-02 piiratud vaate vastu, ilma Markdowni või selgitava tekstita. Küsimus, millele lubatud vaade vastata ei saa, suunatakse AI-08 taskis määratavale käsitlusele; see task ei otsusta kasutajale kuvatavat veateadet.

## Vastuvõtu kriteeriumid

- [ ] Praegune `ToolSearchIntent`-i loomise samm asendatakse loomuliku keele küsimusest SQL-i koostamisega.
- [ ] Mudeli kontekst kirjeldab ainult AI-02 piiratud vaadet ja selle lubatud veerge.
- [ ] Prompt ütleb, et otsing peab näitama ainult AI-03 järgi praegu saadaval olevaid tööriistu.
- [ ] LLM-i vastus on päringutekst, mitte loomuliku keele lõppvastus ega Markdown-koodiplokk.
- [ ] Küsimus edastatakse mudelile päringu `question` väljast; sisend jääb AI-01 500 märgi lepingu piiresse.
- [ ] Prompti ega LLM-i konteksti ei lisata kasutaja isikuandmeid ega piiratud vaateväliseid tabeleid.
- [ ] LLM-i vead ja mittetõlgendatav väljund liiguvad AI-05/AI-08 kokkulepitud veakäsitlusse.

## Veaolukorrad

Mudeliteenuse tõrge või tühi vastus ei anna andmebaasipäringut. Selle API staatus ja kasutajale kuvatav teade määratakse AI-08 taskis.

## Avatud küsimused

- Kas mittetoetatava või mitmeti mõistetava küsimuse SQL-i koostamise vastus on eraldi marker (näiteks `CANNOT_ANSWER`) või käsitletakse seda AI-05 valideerimisveana? Valik peab sobituma AI-08 UI käitumisega.
- AI-02/AI-03 vaate ja saadavuse leping peavad enne prompti fikseerimist olema kinnitatud.
