# Kasutajate haldamine

**Vaade:** `AdminView.vue`, route `/admin`

**Roll:** Admin

**Vaste balsamic mockupis:** AdminView.vue, lehekülg 13/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Kasutajate-haldamine.png`).

![Mockup](./Kasutajate-haldamine.png)

Allikad: [AdminView märkmed](../../../balsamic/notes/AdminView-markmed.md) ja allpool viidatud backend taskid. Märkmetes dokumenteeritud täpsustused (eemaldatud „Lisa kasutaja“, kategooria modaalid ilma pildihalduseta) täiendavad algset wireframe'i.

## Kasutajavoog

Admin avab „Haldus“ ja näeb kasutajate tabelit koos olekute ning registreerimiskuupäevadega. Ta saab kasutaja blokeerida või küsitud kinnituse järel kustutada; õnnestumisel laaditakse kasutajate loend uuesti. Keelatud tegevuse korral jäävad andmed nähtavale ja kuvatakse backend message. Kategooriate jaotis valmib [eraldi FE taskis](./Kategooriate-haldamine.md); kasutaja lisamist ega blokeeringu eemaldamise nuppu selles versioonis ei ole.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Logo, Avaleht, Otsi tööriistu, Minu tööriistad, Sõnumid, Profiil, Logi välja | Ühine päis | Mockupi olemasolev navigatsioon; teiste vaadete teostus on eraldi. |
| Haldus | Navigatsioonilink ja pealkiri | Link ainult adminile, suund `/admin`. |
| Kasutajad | Jaotise pealkiri | Kasutajate tabeli kohal. |
| Nimi | Tabeliveerg | firstName ja lastName koos; puuduv/tühi perekonnanimi ei lisa üleliigset tühikut. |
| E-post | Tabeliveerg | email; null korral „—“. |
| Roll | Tabeliveerg | admin → „admin“, customer → „kasutaja“. |
| Registreeritud | Tabeliveerg | registeredAt kuvatakse DD.MM.YYYY, null korral „—“; ära nihuta kuupäeva ajavööndi teisendusega. |
| Olek | Tabeliveerg | A → „Aktiivne“, B → „Mitteaktiivne“. |
| Tegevus: Blokeeri | Rea nupp | Saadab selle rea userId ja status B. |
| Tegevus: Kustuta | Rea nupp | Avab valitud kasutaja nimega kinnituse. |
| Kustutamise kinnitus | Modaal | Kinnita / Tühista; sulgemine või Tühista ei saada DELETE päringut. Täpne tekst on UI teostuse täpsustus. |
| AlertDanger | Veateade | Näitab backend message'i tekstina. |
| Laadimine / tühi loend | Olek | Eristatavad päringu veast; tühja 200 massiivi korral „Kasutajaid ei ole“. |

„Lisa kasutaja“ on algsel pildil nähtav, kuid eemaldatakse vastavalt AdminView märkmetele. Mockupi e-postid ja Malle Haab on kohatäitjad; kasuta vastuse tegelikke ridu.

## Käitumine ja valideerimine

1. Oota ühise autentimisoleku selgumist. Admini korral käivita kasutajate laadimine `beforeMount` kaudu; kategoorialoend laaditakse teisest taskist sõltumatult, et ühe jaotise tõrge ei peidaks teist.
2. Hoia API järjestus (userId kasvavalt), säilita blokeeritud ja profiilita kasutajad. Tabelirea võti on userId. Lehekülgjaotust ega otsingufiltreid see task ei lisa.
3. Blokeeri saadab `{"status":"B"}` valitud userId jaoks. Ka B oleku korduv saatmine annab backend lepingu järgi 200; eraldi aktiveerimisnuppu ei lisata, kuigi API toetab A väärtust.
4. Kustuta salvestab kinnituseks rea ID ja nime. Ainult kinnitamisel saada DELETE; kinnituse tühistamine ei muuda andmeid. USER_HAS_DATA puhul ära käivita blokeerimist automaatselt.
5. Mutatsiooni ajal keela sama rea korduvad tegevused. Eduka 200 tühja body järel lae kasutajate tabel uuesti; ära proovi parsida olematut DTO-d. Kui uuesti laadimine ebaõnnestub, erista edukat muutmist värskendamise veast ning korda üksnes GET päringut.
6. Vea korral näita täpset backend message'i AlertDanger komponendiga. Erista 403 äriviga veakoodi järgi tühja body'ga 403 ligipääsuveast. Oma konto blokeerimise/kustutamise vead peavad olema käsitletud ka siis, kui UI lisab ennetava kontrolli.
7. Võrguvea korral ei pruugi error.response olemas olla. Kuva üldine eestikeelne teade ja vabasta laadimis-/reaolek finally kaudu. Muutvat päringut ei korrata automaatselt: kadunud vastuse korral kontrolli kõigepealt loendit.

## API kutsed

### `GET /api/admin/users`

**Backend task:** [Kasutajate-nimekirja-paring](../../backend/AdminView/Kasutajate-nimekirja-paring.md).

**Sisend:**

Teenusel puuduvad sisendid: path variable'id, query parameetrid ja request body puuduvad.

Teenus on ainult adminile. Kokkuleppe järgi (vt [googlega_login.md](../../../balsamic/notes/googlega_login.md)) kaitseb Spring Security kõiki `/api/admin/**` aadresse reegliga `hasRole("admin")`.

**Väljund:**

**Response (200 OK):** `List<AdminUserDto>`, kõik süsteemi kasutajad, sh blokeeritud. Näide impordiandmete põhjal:

```json
[
  {
    "userId": 1,
    "firstName": "Marko",
    "lastName": "Tamm",
    "email": "email@Gmail.com",
    "roleName": "admin",
    "registeredAt": "2026-09-18",
    "status": "A"
  },
  {
    "userId": 3,
    "firstName": "Liis",
    "lastName": "Kask",
    "email": "liis.kask@example.com",
    "roleName": "customer",
    "registeredAt": "2026-09-18",
    "status": "A"
  }
]
```

| Väli | Java tüüp | Allikas | Selgitus |
|---|---|---|---|
| `userId` | `Integer` | `app_user.id` | |
| `firstName` | `String` | `app_user.first_name` | |
| `lastName` | `String` | `app_user.last_name` | |
| `email` | `String` | `profile.email` | `null`, kui kasutajal pole profiili |
| `roleName` | `String` | `role.role_name` | `admin` või `customer`; mockupis kuvatakse `customer` rolli tekstina „kasutaja“ |
| `registeredAt` | `LocalDate` | `profile.created_at` kuupäevaosa | `null`, kui kasutajal pole profiili |
| `status` | `String` | `app_user.status` | `A` = Aktiivne, `B` = Mitteaktiivne (blokeeritud) |

Mockupi näiteread (Liis Kask `liis@kask.ee`, Malle Haab) on visuaalsed kohatäitjad. JSON näide kasutab faili [3_import.sql](../../../database/3_import.sql) andmeid.

Profiil on valikuline, seega tuleb `profile` tabel liita LEFT JOIN-iga: ka profiilita kasutaja peab loendis olema. Järjestus on `app_user.id ASC` (taski tehniline täpsustus, mockup järjestust ei määra). Lehekülgjaotust ei ole.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | — | Tühi body | Käivita ühine sisselogimise käsitlus ja peata admini tegevused. |
| 403 | — | Tühi body | Kuva ligipääsu puudumine ja peata admini tegevused. |
| 500 | INTERNAL_SERVER_ERROR | Kasutajate laadimine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |

Veavastuste JSON näited (sõnasõnalt backend taski lepingust):

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Kasutajate laadimine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

### `PATCH /api/admin/users/{userId}/status`

**Backend task:** [Kasutaja-blokeerimine](../../backend/AdminView/Kasutaja-blokeerimine.md).

**Sisend:**

| Parameeter | Asukoht | Java tüüp | Kohustuslik | Tähendus |
|---|---|---|---|---|
| `userId` | Path variable | `Integer` | Jah | Muudetava kasutaja `app_user.id`. |
| `status` | Request body | `String` | Jah | Uus olek: `B` (blokeeri) või `A` (aktiivne). |

Request body (`UserStatusRequestDto.java`), näide `PATCH /api/admin/users/3/status`:

```json
{
  "status": "B"
}
```

Mockupi nupp „Blokeeri“ saadab alati `"B"`. Teenus lubab ka väärtust `"A"`, sest andmebaas lubab ainult olekuid `A` ja `B`. Muu väärtus on valideerimisviga.

Teenus on ainult adminile (`/api/admin/**`, `hasRole("admin")`).

**Väljund:**

**Response (200 OK):** tühi body (`Response (200): NONE`).

Teenus seab `app_user.status` väärtuseks request'i `status`. Kui kasutajal on juba sama olek, vastatakse samuti 200 (operatsioon on idempotentne). Blokeeritud kasutaja ei saa enam sisse logida: `CustomOAuth2UserService` kontrollib `status = 'B'` (vt [googlega_login.md](../../../balsamic/notes/googlega_login.md)). Vaates kuvatakse olekut `B` tekstina „Mitteaktiivne“.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | — | Tühi body | Käivita ühine sisselogimise käsitlus ja peata admini tegevused. |
| 403 | — | Tühi body | Kuva ligipääsu puudumine ja peata admini tegevused. |
| 404 | PRIMARY_KEY_NOT_FOUND | Ei leidnud primary keyd 'userId' väärtusega: 123 | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |
| 403 | SELF_BLOCK_NOT_ALLOWED | Iseennast ei saa blokeerida | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |
| 400 | INCORRECT_INPUT | status: <valideerimise teade> | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |
| 500 | INTERNAL_SERVER_ERROR | Kasutaja oleku muutmine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |

Veavastuste JSON näited (sõnasõnalt backend taski lepingust):

```json
{
  "errorCode": "PRIMARY_KEY_NOT_FOUND",
  "message": "Ei leidnud primary keyd 'userId' väärtusega: 123"
}
```

```json
{
  "errorCode": "SELF_BLOCK_NOT_ALLOWED",
  "message": "Iseennast ei saa blokeerida"
}
```

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "status: <valideerimise teade>"
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Kasutaja oleku muutmine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

### `DELETE /api/admin/users/{userId}`

**Backend task:** [Kasutaja-kustutamine](../../backend/AdminView/Kasutaja-kustutamine.md).

**Sisend:**

| Parameeter | Asukoht | Java tüüp | Kohustuslik | Tähendus |
|---|---|---|---|---|
| `userId` | Path variable | `Integer` | Jah | Kustutatava kasutaja `app_user.id`. |

Query parameetrid ja request body puuduvad. Näide: `DELETE /api/admin/users/3`.

Teenus on ainult adminile (`/api/admin/**`, `hasRole("admin")`).

**Väljund:**

**Response (200 OK):** tühi body (`Response (200): NONE`).

Kasutaja kustutatakse füüsiliselt ühes transaktsioonis (`@Transactional`):

1. Kustutatakse kasutaja `profile` rida, kui see on olemas.
2. Kustutatakse `app_user` rida.

Kustutamine on lubatud ainult siis, kui kasutajal pole ühtegi tööriista (`tool.owner_id`) ega broneeringut (`booking.renter_id`). Muul juhul tuleb kasutaja blokeerida (`PATCH /api/admin/users/{userId}/status`). Nii jäävad `tool` ja `booking` välisvõtmed terveks ja teiste kasutajate broneeringute ajalugu ei kao.

Profiili `location` rida jääb alles. AdminView märkmed selle kustutamist ei nõua.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | — | Tühi body | Käivita ühine sisselogimise käsitlus ja peata admini tegevused. |
| 403 | — | Tühi body | Kuva ligipääsu puudumine ja peata admini tegevused. |
| 404 | PRIMARY_KEY_NOT_FOUND | Ei leidnud primary keyd 'userId' väärtusega: 123 | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |
| 403 | SELF_DELETE_NOT_ALLOWED | Iseennast ei saa kustutada | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |
| 403 | USER_HAS_DATA | Kasutajat ei saa kustutada, sest tal on tööriistu või broneeringuid | Kuva message; kasutaja jääb tabelisse. Blokeerimine on eraldi käsitsi valitav tegevus. |
| 500 | INTERNAL_SERVER_ERROR | Kasutaja kustutamine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message AlertDanger kaudu; säilita andmed ja ära näita õnnestumist. |

Veavastuste JSON näited (sõnasõnalt backend taski lepingust):

```json
{
  "errorCode": "PRIMARY_KEY_NOT_FOUND",
  "message": "Ei leidnud primary keyd 'userId' väärtusega: 123"
}
```

```json
{
  "errorCode": "SELF_DELETE_NOT_ALLOWED",
  "message": "Iseennast ei saa kustutada"
}
```

```json
{
  "errorCode": "USER_HAS_DATA",
  "message": "Kasutajat ei saa kustutada, sest tal on tööriistu või broneeringuid"
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Kasutaja kustutamine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

## Komponendid ja failistruktuur

Vaade ja `/admin` route praegu puuduvad; olemas on ainult HomeView ja TestView. Backendis puuduvad selle vaate Controller/DTO realisatsioonid, seega API leping pärineb kasutaja antud backend taskidest. Need kirjeldavad soovitud käitumist, mitte juba töötavat API-t. Järgi `docs/frontend/projekti-struktuur.md` ning `docs/frontend/vue-komponendi-struktuur.md`: Options API, `beforeMount` andmete laadimiseks, `.then()` / `.catch()` / `.finally()` ja eraldi `handle...` meetodid; sündmustel `event-` eesliide.

Mõlemad AdminView taskid täiendavad ühte vaadet, mitte ei loo kahte `/admin` route'i. Ühine päis, rollikaitse, veakomponent ja kinnitamise modaal teostatakse üks kord. `/admin` ligipääs ja „Haldus“ link lubatakse ainult `roleName === 'admin'` korral; serveri rollikaitse jääb määravaks. Autentimine ja väljalogimine kasutavad olemasolevat ühise autentimise ülesannet. Muutvad päringud peavad järgima selle sessiooni/CSRF lepingut.

| Fail | Vastutus |
|---|---|
| `frontend/src/views/AdminView.vue` | Ühine adminivaade, kasutajate päringud ja tegevuste vastused. |
| `frontend/src/components/tables/AdminUsersTable.vue` | Kasutajaread, props loendile ja tegevuse olekule, event-block-user / event-delete-user. |
| `frontend/src/components/modals/ConfirmDeleteModal.vue` | Kinnituse objekt ja event-confirm / event-cancel; ühine kategooriatega. |
| `frontend/src/components/common/AlertDanger.vue` | Märkmetes nimetatud veakomponent, praegu puudub. |
| `frontend/src/api-services/AdminUserService.js` | Kolm kasutajate API päringut. |
| `frontend/src/router/index.js` | `/admin` ja admini rollikaitse. |
| `frontend/src/navigation/` | Haldus link ning ühised navigeerimised. |

Need on kavandatud failid, mitte olemasolev teostus. Backend taski viide CustomOAuth2UserService nimele erineb Google juhendi AppUserOidcService nimest; FE ei sõltu klassinimest ega eelda, et blokeerimine lõpetaks automaatselt juba avatud sessioone.

## Vastuvõtu kriteeriumid

- [ ] Haldus link ja `/admin` on kasutatavad ainult adminile; 401 ja tühja body'ga 403 on käsitletud.
- [ ] Kõik kuus tabeliveergu ja mõlemad rea tegevused on olemas, „Lisa kasutaja“ puudub.
- [ ] Kuvatakse õige nime-, rolli-, kuupäeva- ja olekuvorming; profiilita ja blokeeritud kasutajad säilivad loendis.
- [ ] Blokeerimine saadab rea ID ja B; 200 korral värskendatakse ainult vajalik loend.
- [ ] Kustutamine nõuab kinnitust; tühistamine ei saada päringut.
- [ ] SELF_BLOCK_NOT_ALLOWED, SELF_DELETE_NOT_ALLOWED, USER_HAS_DATA, 400, 404 ja 500 kuvatakse kirjeldatud message'iga.
- [ ] USER_HAS_DATA ei kustuta rida UI-st ega blokeeri kasutajat automaatselt.
- [ ] Tühi vastus, võrguviga, korduv vajutus ja eduka muutmise järgne GET tõrge on kontrollitud.
- [ ] Eduka kustutamise kontroll kasutab tööriistade/broneeringuteta testkasutajat: kumbki impordikasutaja pole kustutatav.
