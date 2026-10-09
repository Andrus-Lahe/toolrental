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
| Küsimus ei kuulu tööriistade saadavuse otsingu ulatusse | `422`, `errorCode: AI_UNSUPPORTED_QUESTION`, ohutu kasutajateade `ApiError` kujul | Näidatakse, millistele küsimustele AI saab vastata; seda ei kuvata tühitulemusena. |
| LLM, SQL valideerimine, andmebaas või võrk ebaõnnestub | Backend järgib kontrollitud üldist vealepingut; tehnilisi detaile ei väljasta | Näidatakse üldist vea- ja uuesti proovimise teadet; tühitulemuse teadet ei kuvata. |

Vigase AI koostatud SQL-i, mudeliteenuse, andmebaasi või võrgu vea vastus on `500`, `errorCode: INTERNAL_SERVER_ERROR` ning üldine ohutu `ApiError.message`. AI koostatud SQL, algne tehniline veasõnum ja stack trace ei kuulu kliendivastusesse.

## Vastuvõtu kriteeriumid

- [x] `200` ja `tools: []` kuvab tühitulemuse teate, mitte serverivea teadet.
- [x] Eelmise otsingu tulemused eemaldatakse enne uut päringut ega jää vea korral eksitavalt nähtavale.
- [x] `401` kuvab sisselogimise juhise/linki.
- [x] `400 INCORRECT_INPUT` kuvab kasutajale sobiva sisenditeate; backend vastab olemasoleva `ApiError` kujuga.
- [x] AI/SQL/DB/network tõrge kuvab üldise veateate ega kuva SQL-i, stack trace'i, isikuandmeid ega tehnilist andmebaasisõnumit.
- [x] Mittetoetatav küsimus annab `422 AI_UNSUPPORTED_QUESTION`; tehnilist või mittetoetatavat päringut ei kuvata tühitulemusena.
- [x] Uuesti proovimine on võimalik uut küsimust esitades; vaate hõivatuse olek taastub ka vea korral.

## Seotud failid

- `frontend/src/views/AiSearchView.vue` — praegune `401` ja üldise vea kuvamine.
- `backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` — olemasolev `ApiError` üldvea ja sisendvea kuju.
- `backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java` — sisselogimata päringu `401` käitumine.

## Otsustatud staatused

- Mittetoetatav kasutajaküsimus: `422 AI_UNSUPPORTED_QUESTION` koos selgitava `ApiError`-iga.
- Vigane LLM-i SQL, LLM-i/DB/võrgu tõrge: `500 INTERNAL_SERVER_ERROR` koos üldise `ApiError`-iga.
- Asukohata tööriist jääb tulemusse `location: null` väärtusega vastavalt AI-01 lepingule.
