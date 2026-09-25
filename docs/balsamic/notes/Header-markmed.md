# Header.vue märkmed

Allikas: `Laenukas2509.pdf`, lehekülg 14 (Header.vue).

Staatus: kinnitatud 2026-09-25 (lingi nimi "Admin"; "Minu tööriistad" ja "Profiil" ainult sisse logitud kasutajale; "Logi välja" suunab avalehele; "Sõnumid" linki pole). Sisselogimise ja väljalogimise lahendus ning `GET /api/me` tulevad failist [googlega_login.md](googlega_login.md). Backendis pole seda koodi veel.

## Erinevus tavalisest vaate sildist

- `Header.vue` on **komponent**, mitte eraldi vaade: tal pole oma rada. See kuvatakse kõigi lehtede kohal (`App.vue` kaudu, mis praegu sisaldab toorikprojekti `<nav>` plokki).
- Kollane märge loetleb lingid: Logo, Avaleht, Otsi tööriistu, Minu tööriistad, Profiil, Logi sisse / Registreeru ning adminile lisaks Admin. Lingide sihtrajad on võetud teiste vaadete siltidelt:

| Link | Rada | Silt |
|---|---|---|
| Logo, Avaleht | `/` | [HomeView-markmed.md](HomeView-markmed.md) |
| Otsi tööriistu | `/tools` | [ToolsView-markmed.md](ToolsView-markmed.md) |
| Minu tööriistad | `/my-tools` | [MyToolsView-markmed.md](MyToolsView-markmed.md) |
| Profiil | `/profile` | [MyProfile-markmed.md](MyProfile-markmed.md) |
| Admin | `/admin` | [AdminView-markmed.md](AdminView-markmed.md) |

- „Logi sisse / Registreeru“ on üks ja sama Google'i sisselogimine: `googlega_login.md` järgi luuakse kasutaja esimesel sisselogimisel ja profiil täidetakse seejärel MyProfile vormis. Eraldi registreerumist pole.
- „Logi välja“ nuppu sellel lehel pole, kuid teiste lehtede mockupitel on see sisse logitud kasutaja päises. Silt kirjeldab mõlemat olekut.
- Teiste lehtede mockupitel on päises ka link „Sõnumid“, sellel lehel ja kollases märkmes seda pole. Silt seda ei lisa.
- Näide kasutab faili `3_import.sql` kasutajat Liis Kask (`userId = 3`).

## Vaate märkmed

```text
Roll: Kõik rollid (külastaja, Customer, Admin — lingid sõltuvad rollist)
Failinimi: Header.vue
Frontend rada: — (komponent, kuvatakse kõigi lehtede päises)

Vaatega seotud lisainfo:
Rakenduse käivitumisel küsitakse GET /api/me. Vastus 401 tähendab, et kasutaja pole sisse logitud: kuvatakse Logo, "Avaleht", "Otsi tööriistu" ja nupp "Logi sisse / Registreeru", mis on tavaline link /oauth2/authorization/google (mitte axios päring).

Sisse logitud kasutajale kuvatakse lisaks "Minu tööriistad" (/my-tools), "Profiil" (/profile) ja nupu "Logi sisse / Registreeru" asemel "Logi välja" (POST /logout, seejärel tühjendatakse frontendi kasutaja olek ja suunatakse avalehele /). Link "Admin" (/admin) on nähtav ainult siis, kui roleName = "admin".

Kui GET /api/me vastab hasProfile: false, suunatakse kasutaja profiili täitmise vaatele /profile.
```

## API märkmed — GET /api/me

```text
API: GET /api/me

CurrentUserDto.java
Response (200):
{
  "userId": 3,
  "firstName": "Liis",
  "lastName": "Kask",
  "roleName": "customer",
  "email": "liis.kask@example.com",
  "hasProfile": true
}

API teenuse lisainfo:
Kasutaja tuvastatakse sessioonist (JSESSIONID küpsis). roleName on "admin" või "customer". email tuleb profiilist või, kui profiili pole, Google'i sessioonist; siis on hasProfile = false. Sisse logimata kasutajale vastab Spring Security 401 ilma ApiError body'ta — see ei ole viga, vaid tähendab külastajat.

Veateated: —
```

## Veakäsitluse seos olemasoleva projektiga

- `GET /api/me` ei viska ühtegi kohandatud erindit (`ForbiddenException`, `DataNotFoundException`, `PrimaryKeyNotFoundException`), seega `Veateated: —`.
- 401 tuleb Spring Security seadistusest `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` (vt `googlega_login.md`), mitte `RestExceptionHandler`-ist.
- Blokeeritud kasutaja (`app_user.status = 'B'`) ei pääse sisselogimisest kaugemale, seega `GET /api/me` teda ei näe.
- `POST /logout` on Spring Security sisseehitatud aadress, mitte meie API. Seepärast pole sellel eraldi API märget.
