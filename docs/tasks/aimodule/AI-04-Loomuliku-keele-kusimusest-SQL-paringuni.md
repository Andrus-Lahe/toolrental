# AI-04 — Loomuliku keele küsimusest SQL-päringuni

**Sõltuvad taskid:** [AI-02 — Piiratud otsinguandmevaade](./AI-02-Piiratud-otsinguandmevaade.md), [AI-03 — Praeguse saadavuse reegel](./AI-03-Praeguse-saadavuse-reegel.md)

## Eesmärk

Luua eraldiseisev NL→SQL generaator. LLM saab kasutaja küsimuse ning AI-02 vaate lubatud skeemi kirjelduse ja koostab SQL päringu, mis filtreerib AI-03 reegli järgi praegu saadaval tööriistad. Päringu valideerimine ja käivitamine ning generaatori ühendamine otsingu käitusvooga kuuluvad AI-05-sse, et valideerimata SQL ei jõuaks andmebaasi.

LLM ei saa andmebaasiühendust ega isikuandmeid. Ta väljastab päringuteksti; päringu kontroll ja käivitamine kuuluvad AI-05 taski.

## Sisend mudelile

- Kasutaja `question` väärtus olemasolevast `POST /api/ai/ask` request'ist.
- Ainult AI-02 piiratud vaate nimi ja lubatud veergude skeem.
- Saadavuse tähendus AI-03 järgi: `is_available_now = true`.

Prompt ei sisalda rakenduse teiste tabelite skeemi, kasutajate nimesid/e-poste ega andmebaasi päringutulemusi.

## LLM-i väljund

Üks SQL `SELECT` päring AI-02 piiratud vaate vastu, ilma Markdowni või selgitava tekstita. Küsimusele, millele lubatud vaade vastata ei saa, väljastab mudel täpse markeri `CANNOT_ANSWER`; AI-05 käsitleb markerit eraldi ega käivita seda SQL-ina. API veastaatuse ja kasutajale kuvatava teate määrab AI-08.

## Vastuvõtu kriteeriumid

- [ ] `ToolSearchSqlGenerator` koostab kasutaja küsimusest SQL-päringu; selle ühendamine API käitusvooga toimub AI-05-s koos validaatori ja käivitajaga.
- [ ] Mudeli kontekst kirjeldab ainult AI-02 piiratud vaadet ja selle lubatud veerge.
- [ ] Prompt ütleb, et otsing peab näitama ainult AI-03 järgi praegu saadaval olevaid tööriistu.
- [ ] LLM-i vastus on päringutekst, mitte loomuliku keele lõppvastus ega Markdown-koodiplokk.
- [ ] Küsimus edastatakse mudelile päringu `question` väljast; sisend jääb AI-01 500 märgi lepingu piiresse.
- [ ] Prompti ega LLM-i konteksti ei lisata kasutaja isikuandmeid ega piiratud vaateväliseid tabeleid.
- [ ] LLM-i vead ja mittetõlgendatav väljund liiguvad AI-05/AI-08 kokkulepitud veakäsitlusse.

## Veaolukorrad

Mudeliteenuse tõrge või tühi vastus ei anna andmebaasipäringut. Selle API staatus ja kasutajale kuvatav teade määratakse AI-08 taskis.

## Teostuse piir

AI-04 loob ja testib SQL-i generaatori. Olemasolev intent-põhine endpoint jääb käesolevas etapis käigus; AI-05 vahetab selle valideeritud NL→SQL käitusvoo vastu. AI-04 ei käivita mudeli vastust andmebaasis ega tagasta SQL-i kasutajale.
