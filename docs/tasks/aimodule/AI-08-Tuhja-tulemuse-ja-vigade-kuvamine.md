# AI-08 — Tühja tulemuse ja vigade kuvamine

**Seotud taskid:** [AI-01 — Otsingu päringu ja vastuse andmeleping](./AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md), [AI-05 — SQL-i kontroll ja käivitamine](./AI-05-SQL-i-kontroll-ja-kaivitamine.md), [AI-06 — Backend JSON-vastus](./AI-06-Backendi-JSON-vastus.md), [AI-07 — Frontendi lingitud tulemuste kuvamine](./AI-07-Frontendi-lingitud-tulemuste-kuvamine.md)

## Eesmärk

Eristada edukat otsingut, millel pole vasteid, ja otsingut, mis ebaõnnestus. Kasutaja peab aru saama, kas tööriistu ei leitud või päringut ei saanud tehnilisel põhjusel täita.

## Käitumine

| Olukord | API käitumine | Kasutajaliidese käitumine |
|---|---|---|
| Päring õnnestus ja tööriistu leiti | `200`, `tools` sisaldab tulemusi | Kuvatakse lingitud tööriistaloend. |
| Päring õnnestus, aga vasteid pole | `200`, `{"tools":[]}` | Kuvatakse teade, et sobivaid praegu saadaval tööriistu ei leitud; vana tulemuste loend eemaldatakse. |
| Kasutaja pole sisse loginud | `401` | Näidatakse sisselogimise teadet/linki, nagu praeguses AI otsinguvaates. |
| Küsimuse kuju ei läbi valideerimist | `400` olemasoleva `ApiError` kujul | Näidatakse kasutajale sobivat sisendi veateadet. |
| LLM, SQL valideerimine, andmebaas või võrk ebaõnnestub | Backend järgib kontrollitud üldist vealepingut; tehnilisi detaile ei väljasta | Näidatakse üldist vea- ja uuesti proovimise teadet; tühitulemuse teadet ei kuvata. |

Küsimus, millele piiratud otsinguvaade vastata ei saa, ei tohi paista edukalt tühja tööriistaloendina, kui backend suudab selle eraldi veana tuvastada. Konkreetne status/errorCode peab olema backendis üheselt dokumenteeritud enne UI eristuse lisamist.

## Vastuvõtu kriteeriumid

- [ ] `200` ja `tools: []` kuvab tühitulemuse teate, mitte serverivea teadet.
- [ ] Eelmise otsingu tulemused eemaldatakse enne uut päringut ega jää vea korral eksitavalt nähtavale.
- [ ] `401` kuvab sisselogimise juhise/linki.
- [ ] `400` valideerimisvea sõnum on nähtav ning vastab olemasolevale `ApiError` kujule.
- [ ] AI/SQL/DB/network tõrge kuvab üldise veateate ega kuva SQL-i, stack trace'i, isikuandmeid ega tehnilist andmebaasisõnumit.
- [ ] Tehniline või mittetoetatava päringu viga ei kuvata kui „sobivaid tööriistu ei leitud”.
- [ ] Uuesti proovimine on võimalik uut küsimust esitades; vaate hõivatuse olek taastub ka vea korral.

## Seotud failid

- `frontend/src/views/AiSearchView.vue` — praegune `401` ja üldise vea kuvamine.
- `backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` — olemasolev `ApiError` üldvea ja sisendvea kuju.
- `backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java` — sisselogimata päringu `401` käitumine.

## Avatud küsimused

- Millise konkreetse HTTP staatuse ja `errorCode` saab LLM-i mittetoetatav või vigaselt koostatud päring? AI-04/AI-05 backendileping peab selle enne UI erikäitumist määrama.
- Kas asukohata tööriist kuvatakse `location: null` väärtusega või jäetakse välja (AI-01 avatud otsus)?
