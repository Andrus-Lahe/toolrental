# AI-07 — Frontendi lingitud tulemuste kuvamine

**Sõltuvad taskid:** [AI-01 — Otsingu päringu ja vastuse andmeleping](./AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md), [AI-06 — Backend JSON-vastus](./AI-06-Backendi-JSON-vastus.md)

## Eesmärk

Kuvada AI otsingu API-st saadud tööriistad loendina, kus iga rida sisaldab tööriista nime, kategooriat ja asukohta. Tööriista nimi on link, mis avab selle olemasoleva detailvaate `toolId` alusel.

## Kuvamise kuju

Näidisrida: `Akutrell · Ehitustööd · Kristiine`.

Tööriista nimi navigeerib Vue Routeri marsruudile `/tools/{toolId}`. API-st ei küsita ega koostata eraldi detail-URL-i; link kasutab olemasolevat route nime `toolDetailRoute` ja vastuse `toolId` väärtust.

## Vastuvõtu kriteeriumid

- [x] AI otsinguvaade loeb AI-06 response JSON-ist `tools` massiivi.
- [x] Iga tulemus kuvab nime, kategooria ja olemasoleva asukoha kokkulepitud järjekorras.
- [x] Tööriista nimi on klaviatuuriga kasutatav link.
- [x] Lingile vajutamine avab `/tools/{toolId}` olemasoleva tööriistadetaili marsruudi.
- [x] Link kasutab API `toolId` väärtust, mitte loendipositsiooni ega nime.
- [x] Vaade ei sõltu enam API vabatekstilisest `answer` väljast.
- [x] Loading- ja päringu tegemise käitumine säilib.
- [x] Tühitulemuse ning 401/üldise API vea tekstid on eristatud; AI-08 täpsustab mittetoetatava küsimuse veakäsitluse.

## Seotud failid

- `frontend/src/views/AiSearchView.vue` — olemasolev AI otsingu vorm ja tulemuste loend.
- `frontend/src/router/index.js` — olemasolev `/tools/:toolId` (`toolDetailRoute`) marsruut.

## Teostuse piir

AI-01 järgi on puuduva asukoha väärtus `null`; sellisel juhul jätab vaade asukoha ja selle ees oleva eraldaja kuvamata. AI-05 tagastab praegu `CANNOT_ANSWER` korral tühja `tools` massiivi, mistõttu see näib ajutiselt tühitulemusena; AI-08 peab selle eraldi veana esitama.
