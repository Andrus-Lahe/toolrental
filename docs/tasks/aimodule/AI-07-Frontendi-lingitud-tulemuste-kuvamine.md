# AI-07 — Frontendi lingitud tulemuste kuvamine

**Sõltuvad taskid:** [AI-01 — Otsingu päringu ja vastuse andmeleping](./AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md), [AI-06 — Backend JSON-vastus](./AI-06-Backendi-JSON-vastus.md)

## Eesmärk

Kuvada AI otsingu API-st saadud tööriistad loendina, kus iga rida sisaldab tööriista nime, kategooriat ja asukohta. Tööriista nimi on link, mis avab selle olemasoleva detailvaate `toolId` alusel.

## Kuvamise kuju

Näidisrida: `Akutrell · Ehitustööd · Kristiine`.

Tööriista nimi navigeerib Vue Routeri marsruudile `/tools/{toolId}`. API-st ei küsita ega koostata eraldi detail-URL-i; link kasutab olemasolevat route nime `toolDetailRoute` ja vastuse `toolId` väärtust.

## Vastuvõtu kriteeriumid

- [ ] AI otsinguvaade loeb AI-06 response JSON-ist `tools` massiivi.
- [ ] Iga tulemus kuvab nime, kategooria ja asukoha kokkulepitud järjekorras.
- [ ] Tööriista nimi on klaviatuuriga kasutatav link.
- [ ] Lingile vajutamine avab `/tools/{toolId}` olemasoleva tööriistadetaili marsruudi.
- [ ] Link kasutab API `toolId` väärtust, mitte loendipositsiooni ega nime.
- [ ] Vaade ei sõltu enam API vabatekstilisest `answer` väljast.
- [ ] Loading- ja päringu tegemise käitumine säilib.
- [ ] Tühja tulemuse ja API vea tekstid vastavad AI-08 taskile.

## Seotud failid

- `frontend/src/views/AiSearchView.vue` — olemasolev AI otsingu vorm ja tulemuste loend.
- `frontend/src/router/index.js` — olemasolev `/tools/:toolId` (`toolDetailRoute`) marsruut.

## Avatud küsimused

- Tühja või puuduva asukoha visuaalne kuvamine sõltub AI-01 otsusest ja määratakse AI-08/teostuse käigus.
