# AI-03 — Praeguse saadavuse reegel — implementatsiooni plaan

**Seotud task:** [AI-03-Praeguse-saadavuse-reegel.md](./AI-03-Praeguse-saadavuse-reegel.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` — `tool.status` väärtused on `A` ja `U`; booking'u väärtused on `P`, `C`, `R`; booking'ul on `tool_id`, `start_date`, `end_date`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableToolRepository.java` — AI päring filtreerib praegu ainult `t.status = 'A'`, booking'u tabelit ei arvesta.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/ToolRepository.java` — `findAvailableOwnerToolsBy` välistab tänast päeva katva `C` broneeringu omaniku tööriistaloendi tarbeks.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/booking/BookingRepository.java` — kattuvate taotluste kontroll arvestab `P` ja `C` olekuid broneerimise kuupäevavahemikus.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/BookingService.java` — tänane kuupäev saadakse ajavööndiga `Europe/Tallinn`; see task ei muuda broneerimise käitumist.

Test-kaustu ei uuritud kasutaja juhise järgi.

## Puuduv/muudetav

- AI otsing ignoreerib praegu kõiki booking'u kirjeid.
- Üks keskne saadavuse väljendus puudub; AI-02 vaade peab selle arvutama AI-03 reegli järgi.
- Andmebaasi `CURRENT_DATE` ajavööndi kooskõla Tallinnaga tuleb enne konkreetse SQL väljendi valikut kontrollida.

## Sammud

1. **Kinnita saadavuse ärireegel** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-03-Praeguse-saadavuse-reegel.md`
   - Kasuta olekuid `tool A/U` ja `booking P/C/R` täpselt taski reegli järgi.
   - Säilita algus- ja lõppkuupäeva kaasav võrdlus ning ainult tänase hetke saadavus.

2. **Joonda reegel AI-02 vaatega** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-02-Piiratud-otsinguandmevaade.md`
   - Lisa/defineeri `is_available_now` nii, et `A` tööriist on saadaval, kui puudub sama tööriista tänast kuupäeva kattev `C` booking.
   - `P`, `R`, mineviku- ja tuleviku booking ei blokeeri tänast saadavust.

3. **Tagada kuupäeva allikas** — võimalikud failid sõltuvad lahendusest: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableToolRepository.java` või DB ajavööndi seadistus.
   - Kasuta `Europe/Tallinn` kuupäeva; ära eelda andmebaasi serveri vaikimisi ajavööndit.
   - Väldi olukorda, kus Java ja PostgreSQL annavad kesköö ümbruses erineva „tänase” kuupäeva.

4. **Hoia taski piirangud** — booking'u loomise ja omaniku otsuse teenuseid ei muudeta. Kattuvate `P` taotluste lubamine on eraldi hilisem töö.

## Veakäsitlus

Andmebaasi lugemise viga liigub AI-08 veakäsitlusse. Kui saadavuse kontroll ebaõnnestub, ei tohi teenus vaikimisi käsitleda tööriistu saadaval olevana.

## Testid

Hilisem teostus peaks kontrollima `A`/`U`, `P`/`C`/`R`, tänase kuupäeva piire ning Tallinnas kuupäeva vahetumise serva. Testfaile ei loetud ega käivitatud.

## Avatud küsimused

- Kuidas tagatakse andmebaasi päringu ajal `Europe/Tallinn` kuupäev: kas rakenduse antud kuupäeva kaudu või andmebaasi sessiooni ajavööndi lepinguga? Valik peab olema kooskõlas AI-02 vaate teostusega.
