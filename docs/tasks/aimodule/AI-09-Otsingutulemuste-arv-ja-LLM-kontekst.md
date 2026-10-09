# AI-09 — Otsingutulemuste arv ja LLM-i kontekst

**Seotud taskid:** [AI-02 — Piiratud otsinguandmevaade](./AI-02-Piiratud-otsinguandmevaade.md), [AI-03 — Praeguse saadavuse reegel](./AI-03-Praeguse-saadavuse-reegel.md), [AI-05 — SQL-i kontroll ja käivitamine](./AI-05-SQL-i-kontroll-ja-kaivitamine.md)

## Eesmärk

Valmistada backendis ette täpsed arvud ja ohutud otsingufaktid, mida järgmine task saab kasutada LLM-ilt loomuliku keele vastuse küsimiseks. Näiteks peab otsing Kristiine kohta eristama kõiki seal avalikult nähtavaid tööriistu nendest, mis on AI-03 reegli järgi täna saadaval.

See task ei lisa veel LLM-i loomuliku keele vastuse kutset ega muuda avalikku API vastusekuju või kasutajaliidest. Need kuuluvad järgmistesse taskidesse.

## Loenduse reeglid

- **Koguarv (`totalToolCount`)** hõlmab kõiki kasutajatele nähtavaid ehk `tool.status = 'A'` tööriistu, mis vastavad kasutaja otsingu tingimustele. Koguarvu kuuluvad ka tööriistad, millel on täna kattuv `C` olekus broneering.
- **Saadaval arv (`availableToolCount`)** hõlmab koguarvu alamhulka, mis vastab AI-03 saadavuse reeglile: `A` olekus tööriist, millel pole tänase Tallinna kuupäevaga kattuvat `C` broneeringut. `P` ja `R` olekus broneering saadavust ei blokeeri.
- `U` olekus tööriistu ei loeta koguarvu ega saadaval tööriistade hulka.
- Mõlemad arvud kasutavad samu kasutaja otsingust tuletatud tingimusi, näiteks asukohta ja kategooriat. Saadavuse tingimus rakendub ainult saadaval arvule.
- Loendused on täpsed ega sõltu kuvatava `tools` massiivi 20 tulemuse piirangust.
- Kuupäevareegel jääb AI-03 järgi `Europe/Tallinn` ajavööndisse.

## LLM-i jaoks ettevalmistatav kontekst

Backend valmistab järgneva vastuse genereerimise jaoks ette ainult küsimusele vastamiseks vajalikud otsingufaktid:

- otsingus kasutatud asukoht ja muud sobivad mitte-isikulised filtrid;
- `totalToolCount`;
- `availableToolCount`.

Need väärtused arvutab backend piiratud otsinguandmevaate põhjal. LLM-ile ei anta kasutajate nimesid, e-posti aadresse, telefoninumbreid, `google_sub` väärtusi, omaniku/rentniku ID-sid, täpseid aadresse ega koordinaate. Tööriistade linkide loendit ei genereerita nende faktide põhjal; selle allikas jääb AI-05 valideeritud andmebaasipäringuks.

## Näide

Kui Kristiines leidub kokku 20 nähtavat tööriista ja neist 8 on AI-03 järgi saadaval, valmistab backend ette faktid:

```json
{
  "location": "Kristiine",
  "totalToolCount": 20,
  "availableToolCount": 8
}
```

Järgmine vastuse genereerimise task saab nende faktide põhjal koostada dünaamilise teksti, näiteks „Kristiines leidub kokku 20 tööriista. Hetkel saadaval on 8:”.

## Vastuvõtu kriteeriumid

- [ ] Backend arvutab otsingutingimustele vastavate nähtavate tööriistade täpse koguarvu, sh täna `C` broneeringuga tööriistad.
- [ ] `U` olekus tööriistu koguarvu ei arvestata.
- [ ] Backend arvutab sama otsingu täpse saadaval tööriistade arvu AI-03 reegli järgi.
- [ ] Koguarv ja saadaval arv kasutavad samu sisendist tuletatud asukoha, kategooria ja muid toetatud otsingufiltreid; saadavuse tingimus mõjutab ainult saadaval arvu.
- [ ] Koguarv ja saadaval arv on täpsed ka siis, kui vastuse `tools` massiivis kuvatakse maksimaalselt 20 kirjet.
- [ ] Järgmise LLM-vastuse jaoks ettevalmistatud kontekst sisaldab täpseid arve ja vajalikku mitte-isikulist asukoha/filtri kirjeldust.
- [ ] Loenduse andmeallikas ei avalda mudelile ega API vastusele keelatud isikuandmeid, täpset aadressi ega koordinaate.
- [ ] Tööriistade `tools` loendi kirjed jäävad andmebaasitulemustest pärinevaks; LLM ei saa muuta nende ID-sid ega välju.
- [ ] Null tulemuse korral on mõlemad arvud `0` ning backend ei käsitle olukorda veana.
- [ ] Loendusvea korral kasutatakse AI-08 üldist serverivea käsitlust; tehnilisi andmebaasi detaile kliendile ei tagastata.

## Veaolukorrad

| Olukord | Käitumine |
|---|---|
| Otsingufiltritele vastavaid nähtavaid tööriistu pole | `totalToolCount = 0`, `availableToolCount = 0`; otsing jääb edukaks. |
| Tööriistu leidub, kuid ükski pole AI-03 järgi saadaval | `totalToolCount` on positiivne, `availableToolCount = 0`; saadaval tööriistade loend on tühi. |
| Andmebaasi loendus ebaõnnestub | Päring käsitletakse AI-08 järgi üldise HTTP 500 veana; tehnilisi detaile ei avaldata. |
| Küsimus ei kuulu AI otsingu ulatusse | Säilib AI-08 `422 AI_UNSUPPORTED_QUESTION` käitumine. |

## Teostuse piir

See task valmistab ette backendis kasutatava otsingu kokkuvõtte ega muuda veel `ToolAskResponse` avalikku JSON lepingut. LLM-i loomuliku keele vastuse prompt, teine mudelikutse ja `answer` välja kuvamine kuuluvad järgmiste AI taskide alla.
