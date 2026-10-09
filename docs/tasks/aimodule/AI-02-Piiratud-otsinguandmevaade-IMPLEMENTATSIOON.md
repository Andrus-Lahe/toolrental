# AI-02 — Piiratud otsinguandmevaade — implementatsiooni plaan

**Seotud task:** [AI-02-Piiratud-otsinguandmevaade.md](./AI-02-Piiratud-otsinguandmevaade.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` — määrab PostgreSQL tabelid `tool`, `category`, `booking`, `profile`, `location` ja `district`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/3_import.sql` — sisaldab näidisandmeid, mille abil asukoha ja kategooria seoseid kontrollida.
- Varasem `AvailableToolRepository` kasutas baastabeleid otse; see eemaldati AI-05-s aktiivse otsingutee lihtsustamisel.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchQueryRepository.java` — aktiivne AI otsing loeb nüüd ainult piiratud vaadet.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableTool.java` — praegune Java tulemus sisaldab `toolId`, `name`, `category`, `city` ja `district`.

AI-02 teostuse ajal ei uuritud teste. Kasutaja lubas testide kasutamise hiljem ning AI-05 lisas vaate vastu PostgreSQL-i integratsioonitestid.

## Puuduv/muudetav

- Piiratud vaate DDL oli skeemis puudu; see lisati `docs/database/2_create.sql` faili.
- Intent-põhine baastabelipõhine Java otsing eemaldati AI-05-s; mudeli päringut piirab nüüd AI-04 prompt ja AI-05 validaator.
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
   - Jäta prompti muudatus AI-04 ja SQL-i käivitamise kontroll AI-05 teostada.

## Veakäsitlus

Runtime päringu vead kuuluvad AI-08 üldise API veakäsitluse alla. DDL-i süntaksi- või seoseviga peab katkestama skeemi loomise; seda ei tohi maskeerida tühja otsingutulemusena.

## Testid

Vaate tulemusi, lubatud välju ja AI-03 saadavust kontrollib PostgreSQL-i integratsioonitest `ToolSearchQueryRepositoryTest`, mis lisati AI-05 käigus.

## Teostuse piir

Vaade on loodud `docs/database/2_create.sql` skeemi allikasse. AI-04 prompt ja AI-05 validaator ning repository piiravad aktiivse NL→SQL päringu samale vaatele.
