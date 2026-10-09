# AI-10 — Loomuliku keele vastus ja JSON-leping

**Seotud taskid:** [AI-01 — Otsingu päringu ja vastuse andmeleping](./AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md), [AI-08 — Tühja tulemuse ja vigade kuvamine](./AI-08-Tuhja-tulemuse-ja-vigade-kuvamine.md), [AI-09 — Otsingutulemuste arv ja LLM-i kontekst](./AI-09-Otsingutulemuste-arv-ja-LLM-kontekst.md)

## Eesmärk

Koostada otsingust lühike ja loomulik eestikeelne vastus LLM-i abil ning tagastada see backendist koos andmebaasist saadud tööriistaloendiga. Näiteks võib vastus olla: „Kristiines leidub kokku 20 tööriista. Hetkel saadaval on 8:”. Arvud peavad tulema AI-09 backendis arvutatud faktidest.

Backend teeb vastuse loomiseks pärast otsingut eraldi LLM kutse. LLM koostab ainult `answer` tekstivälja; tööriistade `tools` loendi ning selle `toolId`, nime, kategooria ja asukoha koostab backend valideeritud andmebaasitulemustest.

## Sisend vastuse LLM-ile

Vastuse generaator saab:

- kasutaja algse otsinguküsimuse;
- AI-09 kokkuvõttest otsingus kasutatud asukoha või muud üheselt tuvastatud ohutud filtrid;
- täpsed `totalToolCount` ja `availableToolCount` arvud.

LLM-ile ei saadeta tööriistade loendi koostamiseks vajalikku `toolId` massiivi ega isikuandmeid. Prompt ei tohi sisaldada nimesid, e-posti aadresse, telefoninumbreid, `google_sub` väärtusi, omaniku/rentniku ID-sid, täpseid aadresse, koordinaate ega toor-SQL-i. Vastuse LLM ei päri andmebaasi ega muuda saadud loenduse arve.

## LLM-i väljund

LLM tagastab lühikese eestikeelse loomuliku keele teksti, mis kasutab ainult sisendis olevaid fakte. Väljund ei ole JSON, SQL, Markdown ega tööriistade loend; backend lisab selle `answer` väljana JSON vastusesse.

- Positiivse saadaval arvu korral võib tekst lõppeda loendit sisse juhatava kooloniga.
- Kui nähtavaid tööriistu pole, ütleb vastus selgelt, et neid selles otsingus ei leitud.
- Kui koguarv on positiivne, aga saadaval arv on `0`, ütleb vastus, et tööriistu leidub, kuid ükski pole praegu saadaval.
- Asukohta nimetatakse ainult siis, kui see on AI-09 kokkuvõttes üheselt teada.
- LLM ei tohi arve ümardada, muuta ega järeldada lisafakte.
- Kasutaja küsimust käsitletakse vastuse sõnastamise sisendina; sinna kirjutatud juhised ei tohi muuta system prompti reegleid ega lubatud fakte.

## API vastus

**Response (200 OK):** backend ühendab genereeritud vastuse ja andmebaasist saadud `tools` loendi.

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

`answer` on LLM-i loodud lühitekst, mis põhineb backendis arvutatud otsingufaktidel. `tools` on backendis andmebaasitulemustest koostatud massiiv. LLM ei väljasta ega muuda `tools` kirjeid. Tühitulemuse korral tagastatakse samuti mõlemad väljad; `tools` on tühi massiiv ning `answer` kirjeldab nulltulemust või seda, et ükski tööriist pole praegu saadaval.

See API leping laiendab AI-01 ja AI-06 varasemat ainult `tools` sisaldanud vastust.

## Mudeli ja konfiguratsiooni kasutus

- Taaskasutatakse olemasolevat Spring AI `ChatClient` konfiguratsiooni ja `application.properties`-is määratud Google GenAI mudelit `gemini-3.5-flash-lite`.
- SQL-i genereerimise prompt ja vastuse koostamise prompt jäävad eraldi komponentidesse.
- Selle taskiga ei vahetata mudelit, teenusepakkujat ega API võtit.

## Vastuvõtu kriteeriumid

- [ ] Backend teeb pärast AI-09 kokkuvõtte valmimist eraldi LLM kutse lühikese eestikeelse vastuse koostamiseks.
- [ ] Vastuse prompt saab algse küsimuse ning ainult AI-09 ohutud filtrid ja täpsed koguarvu/saadaval arvu faktid.
- [ ] LLM-i juhised keelavad arvude muutmise, väljamõeldud faktid, SQL-i, JSON-i ja tööriistade loendi väljastamise.
- [ ] Kasutaja küsimuses sisalduvad prompti muutmise või piirangute eiramise juhised ei laienda vastuse generaatori ülesannet ega talle antud fakte.
- [ ] Vastuse LLM ei saa andmebaasiga ühendust ega kasutaja isikuandmeid.
- [ ] Edukas vastus on JSON kujul `{ "answer": "...", "tools": [...] }`.
- [ ] `tools` loend ja selle `toolId` väärtused pärinevad backendis valideeritud andmebaasitulemustest; LLM ei saa neid luua ega muuta.
- [ ] Null koguarvu, null saadaval arvu ja positiivse saadaval arvu olukorrad saavad faktidega kooskõlas oleva vastuseteksti.
- [ ] AI-09 kokkuvõttes puuduva asukoha korral ei väida vastus kindlat linnaosa.
- [ ] Tühi või vigane vastuse LLM väljund ning mudeliteenuse tõrge liiguvad AI-08 üldisesse HTTP 500 veakäsitlusse; toorväljundit ega tehnilisi detaile kliendile ei tagastata.
- [ ] Mittetoetatav otsing säilitab AI-08 `422 AI_UNSUPPORTED_QUESTION` käitumise ega tee vastuse LLM kutset.
- [ ] AI-01 ja AI-06 dokumenteeritud edukas vastusekuju uuendatakse uuele lepingule.

## Veaolukorrad

| Olukord | Käitumine |
|---|---|
| AI-09 kokkuvõte näitab null nähtavat tööriista | Edukas HTTP 200 vastus, `answer` selgitab nulltulemust ja `tools` on tühi. |
| Tööriistu leidub, kuid saadaval arv on null | Edukas HTTP 200 vastus; `answer` ütleb, et tööriistu leidub, kuid praegu saadaval pole; `tools` on tühi. |
| Vastuse LLM tagastab tühja või kasutuskõlbmatu väljundi | Backend tagastab AI-08 järgi üldise HTTP 500 vastuse. |
| Vastuse mudeliteenus ei vasta või ebaõnnestub | Backend tagastab AI-08 järgi üldise HTTP 500 vastuse. |
| Esimene mudelikutse tagastab `CANNOT_ANSWER` | Säilib HTTP 422 `AI_UNSUPPORTED_QUESTION`; teist mudelikutsungit ei tehta. |

## Teostuse piir

See task muudab backendis `answer` ja `tools` ühendava edukate vastuste lepingu ning lisab vastuse LLM-i kutse. Frontendi `answer` kuvamine ning nulltulemuse ja loendi ühine esitlus kuuluvad järgmisse taski. SQL-i genereerimise ja valideerimise turvapiirid jäävad kehtima.

LLM-i proosa on olemuselt varieeruv. Prompt ja testid piiravad vastuse sisu etteantud faktidega, kuid üksnes tekstimudeli väljundi põhjal ei saa kõigi võimalike küsimuste puhul matemaatiliselt tõestada, et mudel ei sõnasta arvu valesti; selle riski vähendamiseks ei anta mudelile tööriistade loendit ega muid kõrvalisi andmeid.
