# AI-05 — SQL-i kontroll ja käivitamine

**Sõltuvad taskid:** [AI-02 — Piiratud otsinguandmevaade](./AI-02-Piiratud-otsinguandmevaade.md), [AI-04 — Loomuliku keele küsimusest SQL-päringuni](./AI-04-Loomuliku-keele-kusimusest-SQL-paringuni.md)

## Eesmärk

Kontrollida AI-04 väljundit enne andmebaasis käivitamist ning käivitada ainult praeguse AI otsingu kokkulepitud piiridesse jääv päring. LLM annab rakendusele päringuteksti, mitte õiguse ise andmebaasi ühenduda.

## Lubatud päringu piir

- Päring peab olema üksnes lugemiseks mõeldud `SELECT`.
- Päring võib lugeda ainult AI-02 piiratud otsinguvaadet.
- Päring peab tagastama AI-01 lepingus vajaliku tööriista ID, nime, kategooria ja asukoha.
- Päringu tulemused annavad backendile andmed AI-06 JSON vastuse koostamiseks.

Muud SQL-i piirangud määratakse ainult siis, kui need on olemasolevate kokkulepetega kooskõlas; selle taski ulatus ei laienda lubatud andmestikku ega anna ligipääsu algtabelitele.

Teostus lubab täpselt nõutud neli väljundveergu kindlas järjekorras, ühe `FROM tool_rental.ai_tool_search`, kohustusliku `is_available_now = TRUE`, `ORDER BY tool_id` ning `LIMIT 1..20`. Filtrid kasutavad ainult tööriista ID-d, nime, kirjeldust, kategooriat ja asukohta. Tavalised filtrid seotakse `AND`-iga; sulgudes võib kasutada lihtsat `OR`-rühma. JOIN-id, alampäringud, funktsioonid, kommentaarid ja muud SQL konstruktsioonid lükatakse tagasi.

## Vastuvõtu kriteeriumid

- [x] LLM-i väljundit ei käivitata enne selle kontrolli läbimist.
- [x] Ainult `SELECT` päring piiratud vaate vastu jõuab andmebaasi.
- [x] Päring, mis pole `SELECT`, viitab lubamata tabelile või ei vasta piiratud vaate lepingule, lükatakse enne käivitamist tagasi.
- [x] SQL-i käivitab backend Spring JDBC kaudu; LLM-il pole andmebaasi ühenduse parameetreid.
- [ ] Valideerija lubab `tool_description` filtrit ja sulgudes OR-rühmi ainult lubatud veergudel.
- [ ] Esimene valideerimistõrge lubab ühe parandamiskatse fikseeritud ohutu tagasisidega; teine tõrge lõpetab üldise serveriveaga.
- [x] Tulemusi kasutatakse ainult tööriistaloendi koostamiseks; piiratud vaateväliseid või isikuandmeid vastusele ei lisata.
- [x] Valideerimis- ja andmebaasivea korral annab olemasolev üldine handler `ApiError` kujul üldise 500 vastuse; SQL-i ega tehnilisi detaile kliendile ei tagastata.

## Veaolukorrad

| Olukord | Käitumine |
|---|---|
| Mudeli väljund ei ole lubatud päring | Validaator viskab rakenduse erindi enne repository väljakutset; üldine API handler tagastab üldise 500 `ApiError` vastuse. |
| Mudeli väljund on `CANNOT_ANSWER` | Teenus ei valideeri ega käivita markerit; viskab mittetoetatud küsimuse erindi, mille AI-08 handler teisendab HTTP 422 vastuseks koodiga `AI_UNSUPPORTED_QUESTION`. |
| Andmebaasipäring ebaõnnestub | Otsing lõpeb serveriveaga; SQL-i ega stack trace'i vastuses ei avaldata. |
| Päring annab null rida | Tagastatakse tühi tööriistaloend, mitte serveriviga. |

## Teostuse piir

SQL-i parserit ei lisatud. Validaator aktsepteerib ainult promptiga kooskõlas olevat kitsast päringukuju ning väljastab repositoryle eraldi `ValidatedSql` väärtuse. `CANNOT_ANSWER` käsitletakse enne valideerimist ja see tagastatakse AI-08 lepingu järgi HTTP 422 vastusena koodiga `AI_UNSUPPORTED_QUESTION`. AI-06 viis edukad vastused JSON lepingule.
