# AI mooduli sessiooni jätkamislogi

**Kuupäev:** 2026-10-09

**Eesmärk:** jätkata AI tööriistaotsingu arutelu ja taskide koostamist samast kohast.

## Töövoo kokkulepe

- Esmalt arutame käitumise ja piirangud läbi.
- Seejärel loome taskid ning nende implementatsiooniplaanid.
- Koodi muutmisega alustame alles siis, kui kasutaja seda ütleb.
- Ära tee commit'i ega push'i ilma kasutaja eraldi juhiseta.
- Kasutaja soovis, et uue teema juurde minnes küsitaks enne kinnitust.

## Senine AI otsingu piirangute kokkulepe

- Otsingu tulemus peab sisaldama ainult tegelikke, AI-03 reegli järgi praegu saadaval tööriistu.
- Isikuandmeid, nagu nimi, e-post, telefon, `google_sub` ja kasutajaga seostatavad väärtused, LLM-ile ei saadeta. ID kasutamine on lubatud seal, kus see on otsinguks vajalik; omaniku/rentniku ID-d pole otsingukontekstis vajalikud.
- LLM-i SQL päring kasutab ainult piiratud AI otsinguandmevaadet; Java valideerib päringu enne andmebaasi käivitamist.
- Tööriista kirjeldust võib sobiva tööriista leidmiseks kasutada, kui mudelile saadetavast tekstist eemaldatakse isikuandmed. Kirjeldus ei kuulu avalikku tööriistaloendi vastusesse.

## Saadavuse ja arvude reeglid

- AI-03 saadavus: `tool.status = 'A'` ja puudub tänast `Europe/Tallinn` kuupäeva kattev `C` booking.
- `P` ja `R` booking saadavust ei blokeeri; `U` tööriista ei kuvata.
- Koguarv hõlmab kõiki otsingule vastavaid avalikult nähtavaid `A` tööriistu, ka neid, millel on täna kattuv `C` booking.
- Saadaval arv on selle koguhulga AI-03 järgi saadaval alamhulk.
- Arvud peavad olema täpsed ega tohi sõltuda tööriistade loendi praegusest 20 kirje piirangust.

## Vastuse käitumine

- Eesmärk on tagastada backendist loomulik eestikeelne vastus koos struktureeritud tööriistaloendiga, näiteks:

```json
{
  "answer": "Kristiines leidub kokku 20 tööriista. Hetkel saadaval on 8:",
  "tools": [
    {
      "toolId": 12,
      "name": "Akutrell",
      "category": "Ehitustööd",
      "location": "Kristiine"
    }
  ]
}
```

- Backend teeb pärast otsingut teise LLM kutse vastuse `answer` koostamiseks. LLM saab kasutada kasutaja küsimust ja backendis arvutatud ohutuid otsingufakte/arve.
- `tools` ja `toolId` väärtused tulevad andmebaasist; LLM neid ei loo ega muuda.
- Frontend kuvab `answer` teksti tööriistade lingitud loendi kohal. Tühja `tools` massiivi korral kuvatakse `answer`, ilma teise vastuolulise tühitulemuse sõnumita.
- Kehtetu küsimuse sisend on `400`, mittetoetatav küsimus `422 AI_UNSUPPORTED_QUESTION`, tehniline viga üldine `500`, sisselogimata kasutaja `401`. Tehnilisi SQL/DB detaile kliendile ei näidata.

## Märksõna- ja kasutusotstarbe otsing

Uus probleem: üksiksõna `labidas` kuvab praegu üldise päringuvea. Kasutaja soovib, et LLM mõistaks ka märksõnu ja kasutusotstarvet, näiteks „millega ma saan kaevata?” → sobiv andmebaasis olemasolev labidas.

Kokkulepped:

- Päringu tähenduse tõlgendamine ja õige SQL-i koostamine jääb LLM-ile; Java koodis ei tehta ette kirjutatud eestikeelsete sünonüümide tabelit.
- LLM saab otsida piiratud vaate tööriista nime, kategooria, asukoha, saadavuse ja tööriista kirjelduse alusel.
- SQL peab jääma piiratud vaate ja lubatud veergude piiresse. Validaatorit võib semantilise otsingu jaoks vajalikus ulatuses laiendada.
- Kui esimene genereeritud SQL validaatorist läbi ei lähe, saab LLM ühe uue katse sama küsimuse ja fikseeritud ohutu validaatoritagasisidega. Parandatud SQL valideeritakse uuesti enne käivitamist.
- Andmebaasi-, võrgu- või mudeliteenuse viga ei käivita SQL-i parandamise retry't. Teise valideerimistõrke korral kasutatakse üldist `500`; `CANNOT_ANSWER` annab `422` ega käivita parandust.
- Praegune logi kinnitab, et valideerija lükkas genereeritud SQL-i tagasi enne andmebaasi jõudmist. Logis pole SQL-i ennast, seega tagasilükkamise täpne süntaksiline põhjus pole veel teada.

## Taskide seis

AI-01 kuni AI-08 on varasemates tööetappides teostatud; AI-08 veakäsitluse muudatused ja testid olid selles sessioonis üle vaadatud ning frontendi build läbis.

AI-09–AI-12 taskide ja implementatsiooniplaanide failid:

- [AI-09 — Otsingutulemuste arv ja LLM-i kontekst](./AI-09-Otsingutulemuste-arv-ja-LLM-kontekst.md)
- [AI-10 — Loomuliku keele vastus ja JSON-leping](./AI-10-Loomuliku-keele-vastus-ja-JSON-leping.md)
- [AI-11 — Frontendi loomuliku vastuse ja tulemuste kuvamine](./AI-11-Frontendi-loomuliku-vastuse-ja-tulemuste-kuvamine.md)
- [AI-12 — LLM-i märksõna- ja kasutusotstarbe otsing](./AI-12-LLM-marksonaja-ja-kasutusotstarbe-otsing.md)

Iga taski implementatsiooniplaan asub samas kaustas nimega, mille lõpus on `-IMPLEMENTATSIOON.md`.

## Koodi jätkutöö seis

- AI-11: `AiSearchView.vue` kuvab nüüd backendist saadud `answer` teksti enne tööriistaloendit ning tühja loendi korral ei lisa eraldi tühitulemuse teadet. Frontendi `npm run build` õnnestus; selle taski teste ei muudetud ega käivitatud.
- AI-12: SQL generaatori prompt toetab märksõna/kasutusotstarbe otsingut ja `tool_description` filtrit; piiratud vaatesse lisati kirjeldus ainult andmebaasi tingimuste jaoks; validaator lubab piiratud OR-rühmi ning teenus teeb ühe parandamiskatse ainult esimese valideerimisvea järel. Backendi `compileJava` õnnestus; teste ei muudetud ega käivitatud.
- AI-09/AI-10 backend on taastatud: valideeritud filtritest koostatakse parameetritud loenduspäringud, `is_visible` abil eristatakse koguarvu saadavusest ning `ToolAskResponse(answer, tools)` vastab AI-11 frontendi lepingule. Vastuse generaator saab küsimuse, täpsed arvud ja ainult üheselt tuvastatud asukoha/kategooria; tööriistakirjeid, ID-sid, SQL-i ega isikuandmeid talle ei anta. `compileJava` läbis; teste pole käivitatud.

## Soovitatud jätk

AI-09/AI-10 olemasolevad testid on viidud uue käitusvoo ja vastuse lepingu järgi kooskõlla: teenuse, kontrolleri, validaatori, vastusegeneraatori ja PostgreSQL repository kontrollid on täiendatud. Repository testid katavad `C`, `P`/`R`, `U`, kirjeldus-OR/asukoha filtrid ja 20 tulemuse piiri. Teste ei ole käivitatud; käivitamine jääb eraldi verifitseerimissammuks. AI-11 frontend ja AI-12 otsing on varasemalt teostatud.

## Tööpuu märkus

Repositooriumi tööpuus oli juba palju olemasolevaid muudatusi teistes backend-, frontend- ja dokumentatsioonifailides. Selles taskide koostamise etapis loodi ainult AI-09–AI-12 taski- ja plaanifailid ning käesolev logi; muudatusi ei commititud. Jätkamisel säilita teised tööpuu muudatused.
