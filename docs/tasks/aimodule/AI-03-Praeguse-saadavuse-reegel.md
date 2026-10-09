# AI-03 — Praeguse saadavuse reegel

**Seotud taskid:** [AI-01 — Otsingu päringu ja vastuse andmeleping](./AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md), [AI-02 — Piiratud otsinguandmevaade](./AI-02-Piiratud-otsinguandmevaade.md)

## Eesmärk

Fikseerida üheselt, milliseid tööriistu AI otsing tohib vastuses näidata, kui kasutaja küsib, millised tööriistad on praegu saadaval. Selles otsingus tähendab saadaval olemine „pole täna rendis”; tulevaste kuupäevade broneeritavus ei kuulu selle taski ulatusse.

## Reegel

Tööriist kuvatakse ainult siis, kui mõlemad tingimused kehtivad:

1. `tool.status = 'A'`.
2. Puudub `booking` kirje, kus sama `tool_id`-l on `status = 'C'` ja tänane kuupäev jääb broneeringu `start_date` ja `end_date` vahele, mõlemad piirid kaasa arvatud.

`booking.status = 'P'` ja `booking.status = 'R'` ei tähenda praegu rendis olemist ega blokeeri AI otsingut. `tool.status = 'U'` tööriista ei tagastata. Tulevane või minevikus olnud `C` broneering ei blokeeri tänast saadavust, kui selle kuupäevavahemik tänast päeva ei kata.

Kuupäeva „täna” arvutamisel kasutatakse projekti senise booking-äriloogikaga kooskõlas ajavööndit `Europe/Tallinn`. Broneeringute kinnitamise ja kattuvate `P` taotluste käitumise muutmine ei kuulu sellesse taski.

## Seotud andmebaasi tabelid

- `tool`: `id`, `status`.
- `booking`: `tool_id`, `status`, `start_date`, `end_date`.

Vt [2_create.sql](../../database/2_create.sql). AI-02 piiratud vaade peab selle reegli esitama ohutu saadavusväljana.

## Vastuvõtu kriteeriumid

- [ ] `A` tööriist ilma tänase päevaga kattuva `C` booking'uta on AI otsingus saadaval.
- [ ] `A` tööriist tänast päeva katva `C` booking'uga ei ole AI otsingus saadaval.
- [ ] `P` ja `R` booking üksi ei muuda `A` tööriista tänast saadavust.
- [ ] `U` tööriista ei tagastata ka siis, kui booking'u kirjeid pole.
- [ ] Broneeringu algus- ja lõppkuupäev on saadavuse võrdluses kaasavad.
- [ ] Ainult tänase kuupäevaga kattuv `C` booking blokeerib; muu perioodi `C` booking ei blokeeri tänast tulemust.
- [ ] Tänase kuupäeva arvutus kasutab `Europe/Tallinn` ajavööndit.
- [ ] Reegel on dokumenteeritud AI-02 andmevaate `is_available_now` välja alusena.
- [ ] See task ei muuda broneeringu loomise ega omaniku otsuse reegleid.

## Veaolukorrad

Saadavuse arvutamise andmebaasitõrge on serveriviga ning käsitletakse AI-08 taski järgi. Tühja tulemust ei käsitleta veana.

## Avatud küsimused

- Projekti andmebaasi sessiooni ajavööndit ei fikseeri `2_create.sql`; teostusel tuleb kontrollida, et `Europe/Tallinn` tänane kuupäev jõuaks vaatesse üheselt. Kui vaade kasutab andmebaasi `CURRENT_DATE` väärtust, peab selle ajavööndi leping olema tagatud.
