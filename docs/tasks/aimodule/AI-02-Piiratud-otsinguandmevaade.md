# AI-02 — Piiratud otsinguandmevaade

**Sõltuv task:** [AI-01 — Otsingu päringu ja vastuse andmeleping](./AI-01-Otsingu-paringu-ja-vastuse-andmeleping.md)

## Eesmärk

Luua AI tööriistaotsingule kitsas andmebaasivaade, mille kaudu LLM-i koostatud päring saab otsinguks vajalikud tööriista, kategooria, saadavuse ja linnaosa andmed. LLM-i päringutel on lubatud kasutada seda vaadet; need ei tohi pärida otse rakenduse muid tabeleid.

Vaade võib andmeid kokku tuua mitmest baastabelist, kuid LLM-ile avaldatakse ainult otsinguks vajalikud väljad. Kasutajate nimed, e-postid, telefonid, `google_sub`, omaniku ja rentniku ID-d, täpne aadress ning koordinaadid ei kuulu vaate väljundisse. Tööriista ID on lubatud ja vajalik UI detaililingi moodustamiseks.

## Andmevaate leping

Vaate esialgne nimi on `tool_rental.ai_tool_search`. Väljundveerud:

| Veerg | Tüüp | Tähendus |
|---|---|---|
| `tool_id` | integer | Tööriista ID; API teisendab selle väljaks `toolId`. |
| `tool_name` | varchar | Tööriista nimi. |
| `category_name` | varchar | Kategooria nimi. |
| `location_name` | varchar või NULL | Omaniku asukoha linnaosa nimi; puuduva profiili või asukoha korral `NULL`. Täpset aadressi ei avaldata. |
| `is_available_now` | boolean | `true`, kui tööriista staatus on `A` ja puudub tänast Tallinna kuupäeva kattev `C` broneering; `P`, `R` ega `U` tulemus ei ole saadaval. |

Vaade on üks rida tööriista kohta. Ootel või tagasi lükatud broneeringud ei tohi tööriista dubleerida. Vaade ei väljasta toor-broneeringuid ega muud kasutajaga seotud infot.

## Seotud andmebaasi tabelid

Vaate lähteandmed asuvad tabelites `tool`, `category`, `booking`, `profile`, `location` ja `district`. Omaniku asukoha seos leitakse ID-de kaudu: `tool.owner_id` → `profile.user_id` → `profile.location_id` → `location.district_id` → `district.id`. `app_user` tabelit ega kasutaja nime/e-posti välju vaate koostamiseks vaja ei ole. LLM kasutab ainult vaadet, mitte neid baastabeleid.

Vt [2_create.sql](../../database/2_create.sql).

Mudeli päringukiht tohib kasutada ainult vaadet `tool_rental.ai_tool_search`. Vaate sees olevad baastabelite ID-seosed on serveripoolsed; mudelile ei avaldata nende skeemi. Prompti piirang kuulub AI-04-sse ning päringu tehniline kontroll AI-05-sse.

## Vastuvõtu kriteeriumid

- [ ] Andmebaasis on AI otsingule mõeldud piiratud vaade `tool_rental.ai_tool_search` (või selle nime muutus on taskis põhjendatud).
- [ ] Vaates on ainult lepingus nimetatud veerud ning üks rida iga tööriista kohta.
- [ ] Tööriista ja kategooria nimed ning linnaosa nimi vastavad baastabelite väärtustele.
- [ ] `is_available_now` järgib AI-03 taskis kinnitatud saadavuse reeglit.
- [ ] Puuduva profiili või asukohaga tööriista käsitlus vastab AI-01 lepingus kinnitatud valikule.
- [ ] Vaate väljundis puuduvad isikuandmed ja täpne aadress; kasutaja nime, e-posti, telefoni, `google_sub`, `owner_id` ega `renter_id` ei ole.
- [ ] AI-04 prompti leping nimetab mudeli lubatud andmeallikana ainult selle vaate; päringu käivitamise piirangu lisab AI-05.
- [ ] Vaate loomise DDL lisatakse projekti andmebaasi skeemi allikasse nii, et tavapärane andmebaasi lähtestamine loob selle.

## Veaolukorrad

Vaate loomise DDL-i viga takistab skeemi lähtestamist ning peab olema nähtav arenduse/deploy protsessis. Otsingupäringu runtime vead tagastatakse AI endpointi olemasoleva üldise 500 `ApiError` lepinguga; kasutajale ei kuvata SQL-i ega andmebaasi tehnilisi detaile.
