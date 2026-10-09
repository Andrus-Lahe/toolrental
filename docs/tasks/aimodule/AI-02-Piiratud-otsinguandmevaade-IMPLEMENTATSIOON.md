# AI-02 — Piiratud otsinguandmevaade — implementatsiooni plaan

**Seotud task:** [AI-02-Piiratud-otsinguandmevaade.md](./AI-02-Piiratud-otsinguandmevaade.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` — määrab PostgreSQL tabelid `tool`, `category`, `booking`, `profile`, `location` ja `district`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/3_import.sql` — sisaldab näidisandmeid, mille abil asukoha ja kategooria seoseid kontrollida.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableToolRepository.java` — kasutab praegu baastabeleid otse; päring seob tool → profile → location → district ning category, kuid ei arvuta broneeringu saadavust.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableTool.java` — praegune Java tulemus sisaldab `toolId`, `name`, `category`, `city` ja `district`.

Test-kaustu ei uuritud, järgides kasutaja juhist.

## Puuduv/muudetav

- Piiratud vaate DDL oli skeemis puudu; see lisati `docs/database/2_create.sql` faili.
- Praegune intent-põhine Java otsingurepositoorium kasutab baastabeleid serveripoolses koodis; mudelile nähtava SQL-i prompt ning selle käivitamise piirangud tehakse hiljem AI-04/AI-05-s.
- AI-01 fikseerib puuduva profiili/asukoha korral `location_name = NULL`, jättes tööriista vaatesse.
- AI-03 fikseerib saadavuse reegli ning `Europe/Tallinn` kuupäeva kasutamise.

## Sammud

1. **Fikseeri vaate veerud ja eeltingimused** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-02-Piiratud-otsinguandmevaade.md`
   - Joonda veerud AI-01 response lepinguga: tööriista ID ja nimi, kategooria, linnaosa ning AI-03 järgi arvutatud saadavus.
   - Puuduva profiili/asukoha korral jääb tööriist tulemusse `location_name = NULL` väärtusega.

2. **Lisa vaate DDL skeemi allikasse** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql`
   - Lisa üks rida tööriista kohta tagastav `tool_rental.ai_tool_search` vaade.
   - Seo `tool` kategooriaga ning omaniku asukoht ID-põhiselt `profile`, `location` ja `district` tabelite kaudu.
   - Ära väljasta vaate kaudu kasutaja isikuandmeid, täielikku aadressi, koordinaate, `owner_id`-d ega `renter_id`-d.
   - Arvuta tänane kuupäev selgesõnaliselt ajavööndis `Europe/Tallinn`; ära sõltu DB sessiooni vaikeajavööndist.
   - Arvuta `is_available_now` AI-03 kinnitatud reegli järgi `NOT EXISTS` kontrolliga, et broneeringud ei dubleeriks tööriista ridu.

3. **Dokumenteeri mudeli andmeallika piir** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-02-Piiratud-otsinguandmevaade.md`
   - Määra see vaade ainsaks mudelile lubatud otsinguandmeallikaks.
   - Jäta prompti muudatus AI-04 ja SQL-i käivitamise kontroll AI-05 teostada; AI-02 ei muuda intent-põhist Java otsingurepositooriumi.

## Veakäsitlus

Runtime päringu vead kuuluvad AI-08 üldise API veakäsitluse alla. DDL-i süntaksi- või seoseviga peab katkestama skeemi loomise; seda ei tohi maskeerida tühja otsingutulemusena.

## Testid

Skeemi loomise järel kontrolli, et vaade tagastab õige kategooria ja linnaosa, ühe rea tööriista kohta ning ainult lubatud veerud. Kontrolli saadavuse arvutust AI-03 juhtumitega. Teste ei käivitatud ega lisatud selle plaani koostamise käigus.

## Teostuse piir

Vaade on loodud `docs/database/2_create.sql` skeemi allikasse. AI-04 lisab mudeli prompti, mis nimetab lubatud allikana ainult seda vaadet; AI-05 peab piirama päringu käivitamise samale vaatele. Praeguse intent-põhise otsingu Java päringukiht ei käivita mudeli koostatud SQL-i ning selle ümbertegemine ei kuulu AI-02-sse.
