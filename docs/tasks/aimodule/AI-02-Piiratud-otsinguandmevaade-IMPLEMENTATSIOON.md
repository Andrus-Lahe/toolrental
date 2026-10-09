# AI-02 — Piiratud otsinguandmevaade — implementatsiooni plaan

**Seotud task:** [AI-02-Piiratud-otsinguandmevaade.md](./AI-02-Piiratud-otsinguandmevaade.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` — määrab PostgreSQL tabelid `tool`, `category`, `booking`, `profile`, `location` ja `district`.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/3_import.sql` — sisaldab näidisandmeid, mille abil asukoha ja kategooria seoseid kontrollida.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableToolRepository.java` — kasutab praegu baastabeleid otse; päring seob tool → profile → location → district ning category, kuid ei arvuta broneeringu saadavust.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableTool.java` — praegune Java tulemus sisaldab `toolId`, `name`, `category`, `city` ja `district`.

Test-kaustu ei uuritud, järgides kasutaja juhist.

## Puuduv/muudetav

- Andmebaasi DDL-is puudub AI-le piiratud vaade.
- Praegune otsingurepositoorium küsib baastabeleid otse; hilisem NL→SQL päring peab kasutama vaadet.
- Vaate availability veerg sõltub AI-03 reeglist ning asukohata tööriista käsitlus AI-01-s lahendamata küsimusest.

## Sammud

1. **Kinnita vaate veerud ja eeltingimused** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-02-Piiratud-otsinguandmevaade.md`
   - Joonda veerud AI-01 response lepinguga: tool ID, tööriista nimi, kategooria, linnaosa ja AI-03 järgi arvutatud saadavus.
   - Lahenda AI-01 asukoha puudumise valik enne DDL-i lõplikku kinnitamist.

2. **Lisa vaate DDL skeemi allikasse** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql`
   - Lisa üks rida tööriista kohta tagastav `tool_rental.ai_tool_search` vaade.
   - Seo `tool` kategooriaga ning omaniku asukoht ID-põhiselt `profile`, `location` ja `district` tabelite kaudu.
   - Ära väljasta vaate kaudu kasutaja isikuandmeid, täielikku aadressi, koordinaate, `owner_id`-d ega `renter_id`-d.
   - Arvuta `is_available_now` AI-03 kinnitatud reegli järgi; ära lisa broneeringu join'i viisil, mis tekitaks tööriista kohta mitu rida.

3. **Joonda AI päringukiht vaate lepinguga** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableToolRepository.java`
   - Hilisema AI-04/AI-05 teostuse ettevalmistamisel asenda mudelile nähtav tabeliskeem vaate skeemiga; olemasoleva filtriloogika muutmine kuulub vastavasse teostustaski.

## Veakäsitlus

Runtime päringu vead kuuluvad AI-08 üldise API veakäsitluse alla. DDL-i süntaksi- või seoseviga peab katkestama skeemi loomise; seda ei tohi maskeerida tühja otsingutulemusena.

## Testid

Skeemi loomise järel kontrolli, et vaade tagastab õige kategooria ja linnaosa, ühe rea tööriista kohta ning ainult lubatud veerud. Kontrolli saadavuse arvutust AI-03 juhtumitega. Teste ei käivitatud ega lisatud selle plaani koostamise käigus.

## Avatud küsimused

- AI-01 asukohata tulemuse valik tuleb enne DDL-i lukku panna.
- Kui projekt toetab skeemi loomist väljaspool `docs/database/2_create.sql`, tuleb vaate DDL lisada ka vastavasse lähtekohta.
