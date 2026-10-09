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

## Vastuvõtu kriteeriumid

- [ ] LLM-i väljundit ei käivitata enne selle kontrolli läbimist.
- [ ] Ainult `SELECT` päring piiratud vaate vastu jõuab andmebaasi.
- [ ] Päring, mis pole `SELECT`, viitab lubamata tabelile või ei vasta piiratud vaate lepingule, lükatakse enne käivitamist tagasi.
- [ ] SQL-i käivitab backend Spring JDBC kaudu; LLM-il pole andmebaasi ühenduse parameetreid.
- [ ] Tulemusi kasutatakse ainult tööriistaloendi koostamiseks; piiratud vaateväliseid või isikuandmeid vastusele ei lisata.
- [ ] Päringu valideerimise ja andmebaasi vea vastus järgib AI-08 lepingut ning ei avalda SQL-i ega tehnilisi detaile kasutajale.

## Veaolukorrad

| Olukord | Käitumine |
|---|---|
| Mudeli väljund ei ole lubatud päring | Päringut ei käivitata; viga antakse AI-08 käsitlusse. |
| Andmebaasipäring ebaõnnestub | Otsing lõpeb serveriveaga; SQL-i ega stack trace'i vastuses ei avaldata. |
| Päring annab null rida | Tagastatakse tühi tööriistaloend, mitte serveriviga. |

## Avatud küsimused

- AI-02 vaate täpne nimi/veerud peavad enne teostust olema lukus.
- AI-04 mittetoetatava küsimuse marker ja AI-08 vastuse staatus tuleb ühtlustada.
