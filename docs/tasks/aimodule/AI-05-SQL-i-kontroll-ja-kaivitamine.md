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

Teostus kasutab sellest lepingust kitsamat SQL-i alamhulka: täpselt nõutud neli väljundveergu kindlas järjekorras, üks `FROM tool_rental.ai_tool_search`, lihtsad `AND`-iga seotud võrdlusfiltrid, kohustuslik `is_available_now = TRUE`, `ORDER BY tool_id` ning `LIMIT 1..20`. JOIN-id, alampäringud, funktsioonid, `OR`, kommentaarid ja muud SQL konstruktsioonid lükatakse tagasi. See on generaatori praeguse promptiga kooskõlas ning hoiab valideerimise arusaadava ja konservatiivsena.

## Vastuvõtu kriteeriumid

- [x] LLM-i väljundit ei käivitata enne selle kontrolli läbimist.
- [x] Ainult `SELECT` päring piiratud vaate vastu jõuab andmebaasi.
- [x] Päring, mis pole `SELECT`, viitab lubamata tabelile või ei vasta piiratud vaate lepingule, lükatakse enne käivitamist tagasi.
- [x] SQL-i käivitab backend Spring JDBC kaudu; LLM-il pole andmebaasi ühenduse parameetreid.
- [x] Tulemusi kasutatakse ainult tööriistaloendi koostamiseks; piiratud vaateväliseid või isikuandmeid vastusele ei lisata.
- [x] Valideerimis- ja andmebaasivea korral annab olemasolev üldine handler `ApiError` kujul üldise 500 vastuse; SQL-i ega tehnilisi detaile kliendile ei tagastata. AI-08 võib määrata täpsema kasutajakogemuse.

## Veaolukorrad

| Olukord | Käitumine |
|---|---|
| Mudeli väljund ei ole lubatud päring | Validaator viskab rakenduse erindi enne repository väljakutset; üldine API handler tagastab üldise 500 `ApiError` vastuse. |
| Mudeli väljund on `CANNOT_ANSWER` | Teenus ei valideeri ega käivita markerit; tagastab olemasoleva API kuju järgi abiteksti ja tühja tööriistaloendi. AI-08 võib selle vastusekuju täpsustada. |
| Andmebaasipäring ebaõnnestub | Otsing lõpeb serveriveaga; SQL-i ega stack trace'i vastuses ei avaldata. |
| Päring annab null rida | Tagastatakse tühi tööriistaloend, mitte serveriviga. |

## Teostuse piir

SQL-i parserit ei lisatud. Validaator aktsepteerib ainult promptiga kooskõlas olevat kitsast päringukuju ning väljastab repositoryle eraldi `ValidatedSql` väärtuse. `CANNOT_ANSWER` käsitletakse enne valideerimist. Praegune `ToolAskResponse` jääb AI-06-ni kehtima; AI-08 otsustab hiljem, kas mittetoetatav päring vajab teistsugust API/UI vastust.
