# Community Tool Rental — andmebaasi dokumentatsioon

Dokumentatsiooni aluseks ja esmaseks tõeallikaks on [create_toolrental_database.sql.sql](create_toolrental_database.sql.sql). Allpool on kirjeldatud selle faili praegune sisu.

## Ülevaade

SQL-failis on määratletud 10 tabelit:

- `app_user`
- `profile`
- `role`
- `city`
- `district`
- `location`
- `category`
- `tool`
- `tool_image`
- `booking`

Kõik tabelid kasutavad primaarvõtmena välja `id` tüübiga `serial`.

Skript sisaldab tabelite, piirangute ja ühe osalise unikaalse indeksi loomist. See ei sisalda `CREATE DATABASE`, `CREATE SCHEMA`, `SET search_path` ega algandmete lisamist. Tabelinimede ees ei ole skeema nime, seega sõltub sihtskeema ühenduse `search_path` seadistusest.

## Tabelid

### `app_user`

Hoiab kasutaja süsteemseid ja autentimisega seotud andmeid.

| Väli | Tüüp | Reegel |
|---|---|---|
| `id` | `serial` | PK |
| `first_name` | `varchar(100)` | NOT NULL |
| `last_name` | `varchar(100)` | NOT NULL |
| `google_sub` | `varchar(255)` | NOT NULL, UNIQUE |
| `role_id` | `int` | NOT NULL, FK → `role.id` |
| `status` | `char(1)` | NOT NULL, default `'A'` |

Lubatud `status` väärtused SQL-i CHECK järgi: `A` = ACTIVE, `B` = BLOCKED.

### `profile`

Hoiab kasutaja kontakt- ja asukohaandmeid.

| Väli | Tüüp | Reegel |
|---|---|---|
| `id` | `serial` | PK |
| `location_id` | `int` | NOT NULL, FK → `location.id` |
| `user_id` | `int` | NOT NULL, UNIQUE, FK → `app_user.id` |
| `email` | `varchar(254)` | NOT NULL, UNIQUE |
| `phone` | `varchar(32)` | NOT NULL |
| `created_at` | `timestamp` | NOT NULL, default `current_timestamp` |
| `updated_at` | `timestamp` | NOT NULL, default `current_timestamp` |

`profile.user_id` on UNIQUE, seega saab ühe `app_user` kirje kohta olla maksimaalselt üks profiil.

### `role`

Hoiab kasutaja süsteemset rolli.

| Väli | Tüüp | Reegel |
|---|---|---|
| `id` | `serial` | PK |
| `role_name` | `varchar(20)` | NOT NULL, UNIQUE |



### `city`

Linnade loend.

| Väli | Tüüp | Reegel |
|---|---|---|
| `id` | `serial` | PK |
| `city_name` | `varchar(100)` | NOT NULL, UNIQUE |

### `district`

Linnaosade/piirkondade loend.

| Väli | Tüüp | Reegel |
|---|---|---|
| `id` | `serial` | PK |
| `city_id` | `int` | NOT NULL, FK → `city.id` |
| `district_name` | `varchar(100)` | NOT NULL |

Unikaalsus: `UNIQUE (city_id, district_name)`.

### `location`

Hoiab kasutaja aadressi.

| Väli | Tüüp | Reegel |
|---|---|---|
| `id` | `serial` | PK |
| `district_id` | `int` | NOT NULL, FK → `district.id` |
| `street_name` | `varchar(150)` | NOT NULL |
| `house_number` | `varchar(20)` | NOT NULL |
| `apartment_number` | `varchar(20)` | NULL |
| `lng` | `decimal(10,7)` | NULL |
| `lat` | `decimal(10,7)` | NULL |

Linn leitakse seose kaudu: `location → district → city`.

### `category`

Tööriistade kategooriad.

| Väli | Tüüp | Reegel |
|---|---|---|
| `id` | `serial` | PK |
| `category_name` | `varchar(100)` | NOT NULL, UNIQUE |

### `tool`

Hoiab tööriista põhiandmeid.

| Väli | Tüüp | Reegel |
|---|---|---|
| `id` | `serial` | PK |
| `owner_id` | `int` | NOT NULL, FK → `app_user.id` |
| `category_id` | `int` | NOT NULL, FK → `category.id` |
| `name` | `varchar(150)` | NOT NULL |
| `description` | `varchar(2000)` | NULL |
| `status` | `char(1)` | NOT NULL, default `'A'` |
| `created_at` | `timestamp` | NOT NULL, default `current_timestamp` |
| `updated_at` | `timestamp` | NOT NULL, default `current_timestamp` |

Lubatud `status` väärtused SQL-i CHECK järgi: `A` = AVAILABLE, `U` = UNAVAILABLE.

Tööriistal ei ole eraldi asukohta. Omaniku asukoht leitakse läbi `profile` tabeli.

### `tool_image`

Hoiab tööriista pildid otse andmebaasis.

| Väli | Tüüp | Reegel |
|---|---|---|
| `id` | `serial` | PK |
| `tool_id` | `int` | NOT NULL, FK → `tool.id` |
| `image_data` | `bytea` | NOT NULL |
| `is_main` | `boolean` | NOT NULL, default `false` |

Reeglid:

- `(tool_id, image_data)` on UNIQUE;
- ühe tööriista kohta saab olla maksimaalselt üks `is_main = true`;
- tööriista kustutamisel kustutatakse pildid `ON DELETE CASCADE` abil.

### `booking`

Hoiab tööriista renditaotlusi ja broneeringuid.

| Väli | Tüüp | Reegel |
|---|---|---|
| `id` | `serial` | PK |
| `tool_id` | `int` | NOT NULL, FK → `tool.id` |
| `renter_id` | `int` | NOT NULL, FK → `app_user.id` |
| `start_date` | `date` | NOT NULL |
| `end_date` | `date` | NOT NULL |
| `status` | `char(1)` | NOT NULL, default `'P'` |
| `owner_message` | `varchar(500)` | NULL |
| `google_event_id` | `varchar(255)` | NULL, UNIQUE |
| `created_at` | `timestamp` | NOT NULL, default `current_timestamp` |
| `updated_at` | `timestamp` | NOT NULL, default `current_timestamp` |

Lubatud `status` väärtused SQL-i CHECK järgi: `P` = PENDING, `C` = CONFIRMED.

Piirang `booking_period_check` nõuab `start_date < end_date`: lõppkuupäev peab olema alguskuupäevast hilisem.

`owner_message` on valikuline sõnumiväli. `google_event_id` on valikuline unikaalne sündmuse ID väli; SQL ei seo selle täitmist kindla broneeringu staatusega.

Broneeringute kattuvuse kontroll ei ole selles SQL-failis andmebaasi piiranguna realiseeritud.

## Seosed

```text
role       1 ── N app_user
app_user   1 ── 0..1 profile

city       1 ── N district
district   1 ── N location
location   1 ── N profile

app_user   1 ── N tool
category   1 ── N tool
tool       1 ── N tool_image

tool       1 ── N booking
app_user   1 ── N booking
```

`N` tähendab nulli või rohkem seotud kirjeid. Igal profiilil on täpselt üks kasutaja, kuid SQL ei nõua, et igal kasutajal oleks profiil.

## SQL-i ja rakenduse käitumise piir

SQL määratleb andmestruktuuri ja piirangud. See ei rakenda e-kirjade saatmist, Google Calendar sündmuste loomist ega broneeringu staatuste üleminekuid. Neid tegevusi ei saa selle SQL-faili põhjal kinnitada.

Väljade `created_at` ja `updated_at` vaikeväärtus on `current_timestamp`. Skriptis ei ole triggerit, mis muudaks `updated_at` väärtust kirje uuendamisel automaatselt.

## Kustutamise reeglid

`RESTRICT` (viitav tabel → viidatud tabel; viidatud kirjet ei saa kustutada, kui sellele viidatakse):

- `app_user → role`
- `booking → app_user`
- `booking → tool`
- `district → city`
- `location → district`
- `profile → location`
- `profile → app_user`
- `tool → category`
- `tool → app_user`

`CASCADE` (kustutatav vanem → automaatselt kustutatavad seotud kirjed):

- `tool → tool_image`

## Automaatne seadistamine Windowsis

Projekti juurkaustast käivita PowerShellis:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/database/setup-database.ps1
```

Skript kasutab PostgreSQL-i aadressil `localhost:5432` ja andmebaasi nimega `database`. Puuduv andmebaas luuakse. Tühja `public` skeema korral käivitatakse `create_toolrental_database.sql.sql` muutmata kujul ühe transaktsioonina; SQL-i vea korral tabelite loomine tühistatakse.

Kasutajanimi ja parool loetakse `backend/src/main/resources/application.properties` failist. Parooli saab asendada keskkonnamuutujaga `PGPASSWORD`. PostgreSQL-i klient leitakse PATH-ist või tavalisest Windowsi paigalduskaustast; vajadusel anna `-PsqlPath 'C:\Program Files\PostgreSQL\17\bin\psql.exe'`.

Korduskäivitus jätab olemasolevad tabelid ja andmed alles ning kontrollib tabelite nimede vastavust SQL-failile. Skript ei uuenda olemasolevate tabelite veerge ega piiranguid. Osalise või erineva tabeliloendi korral peatub skript veaga.

Backend on seadistatud ühendusele `jdbc:p6spy:postgresql://localhost:5432/database`. IntelliJ Database ühenduses kasuta samuti andmebaasi nime `database` ja skeemat `public`. Vanad näidisprojekti SQL-failid ei kuulu sellesse seadistusse.
