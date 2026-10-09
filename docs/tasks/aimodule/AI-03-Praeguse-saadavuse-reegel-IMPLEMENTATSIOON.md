# AI-03 — Praeguse saadavuse reegel — implementatsiooni plaan

**Seotud task:** [AI-03-Praeguse-saadavuse-reegel.md](./AI-03-Praeguse-saadavuse-reegel.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` — `tool.status` väärtused on `A` ja `U`; booking'u väärtused on `P`, `C`, `R`; booking'ul on `tool_id`, `start_date`, `end_date`.
- `AvailableToolRepository` oli varasem AI otsingurepository ning eemaldati AI-05 käigus, kui aktiivne AI päring viidi piiratud vaatele.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` — `ai_tool_search.is_available_now` arvutab aktiivse AI otsingu saadavuse reegli.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/ToolRepository.java` — `findAvailableOwnerToolsBy` välistab tänast päeva katva `C` broneeringu omaniku tööriistaloendi tarbeks.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/BookingRepository.java` — kattuvate taotluste kontroll arvestab `P` ja `C` olekuid broneerimise kuupäevavahemikus.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/BookingService.java` — tänane kuupäev saadakse ajavööndiga `Europe/Tallinn`; see task ei muuda broneerimise käitumist.

AI mooduli olemasolevaid teste võib nüüd kasutada; testide lisamine ja käivitamine on kasutaja poolt lubatud.

## Puuduv/muudetav

- AI otsing peab välistama ainult tänast kuupäeva katva `C` broneeringu.
- `Europe/Tallinn` kuupäev saadakse rakenduse `Clock` bean'ist, mitte andmebaasi sessiooni vaikeajavööndist.
- AI-02 vaade arvutab sama reegli SQL-is ajavööndi selgesõnalise teisendusega.

## Sammud

1. **Kinnita saadavuse ärireegel** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-03-Praeguse-saadavuse-reegel.md`
   - Kasuta olekuid `tool A/U` ja `booking P/C/R` täpselt taski reegli järgi.
   - Säilita algus- ja lõppkuupäeva kaasav võrdlus ning ainult tänase hetke saadavus.

2. **Joonda reegel AI-02 vaatega** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-02-Piiratud-otsinguandmevaade.md`
   - Lisa/defineeri `is_available_now` nii, et `A` tööriist on saadaval, kui puudub sama tööriista tänast kuupäeva kattev `C` booking.
   - `P`, `R`, mineviku- ja tuleviku booking ei blokeeri tänast saadavust.

3. **Kasuta selgesõnalist kuupäevaallikat** — AI otsingu aktiivne rakendus kasutab AI-02 vaadet.
   - Arvuta kuupäev vaates `CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Tallinn'` kaudu.
   - Aktiivne AI käitusvoog ei kasuta Java repository-päringut baastabelitele.

4. **Hoia taski piirangud** — booking'u loomise ja omaniku otsuse teenuseid ei muudeta. Kattuvate `P` taotluste lubamine on eraldi hilisem töö.

## Veakäsitlus

Andmebaasi lugemise viga liigub AI-08 veakäsitlusse. Kui saadavuse kontroll ebaõnnestub, ei tohi teenus vaikimisi käsitleda tööriistu saadaval olevana.

## Testid

`AvailableToolRepositoryTest` kontrollis algset Java päringut ja eemaldati AI-05 käigus koos selle asendamisega. Aktiivse vaate saadavust testib `ToolSearchQueryRepositoryTest`: tänast päeva kattev `C` välistab rea ning `P` ja `R` ei välista.

Kontrolli tulemus:

- Varasemas AI-03 käivituses läbisid `AvailableToolRepositoryTest` 6, `ToolAskControllerTest` 2 ja `ToolAskServiceTest` 1 testi päris PostgreSQL ühendusega.
- Aktiivse view-põhise otsingu praegused saadavuse testid on `ToolSearchQueryRepositoryTest`-is ning AI-05 plaanis on kirjas selle taski lõplik testikäivitus.

## Teostuse piir

Booking'u loomise, kattuvuste kontrolli ja omaniku otsuse käitumist ei muudeta. Aktiivne AI otsing kasutab AI-02 vaate `is_available_now` välja, mis arvutab kuupäeva SQL-is `Europe/Tallinn` ajavööndiga.
