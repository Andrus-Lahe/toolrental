# Minu tööriistade ülevaate päring

**Teenus:** `GET /api/users/me/tools`

**Vaste balsamic mockupis:** MyToolsView.vue, lehekülg 10/17 failis [Laenukas 2509.pdf](../../../balsamic/notes/Laenukas%202509.pdf).

![Mockup](./Minu-tooriistade-ulevaade.png)

**Kasutaja juhis:** lähtuda kujundusest; kollane silt on aegunud. Seetõttu on aluseks viis kujunduses nähtavat rühma, mitte vana märkme kolm loendit. Ka sinise sildi kolme loendiga vastust tuleb nende rühmade jaoks laiendada. Allolev viie loendiga DTO on taski tehniline ettepanek, mitte olemasolev realisatsioon. [MyToolsView märkmed](../../../balsamic/notes/MyToolsView-markmed.md) jäävad ajalooliseks allikaks; neid selles töös ei muudeta.

## Sisend ja väljund

### `GET /api/users/me/tools`

Path/query parameetrid ja request body puuduvad. Kasutaja määratakse serveri sessioonist; ka Admin näeb enda andmeid, mitte kogu süsteemi loendit. Endpoint'i aadress säilib siniselt sildilt.

**Response 200:** `MyToolsResponseDto`:

```json
{
  "userId": 3,
  "firstName": "Liis",
  "lastName": "Kask",
  "email": "liis.kask@example.com",
  "phone": "55501002",
  "myRentals": [],
  "incomingRequests": [],
  "outgoingRequests": [
    {
      "toolId": 1,
      "toolName": "Akutrell",
      "toolStatus": "A",
      "imageData": "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNDAiIGhlaWdodD0iMjQwIiB2aWV3Qm94PSIwIDAgMjQwIDI0MCI+PHJlY3Qgd2lkdGg9IjI0MCIgaGVpZ2h0PSIyNDAiIHJ4PSIxNiIgZmlsbD0iI2YxZjVmOSIvPjxwYXRoIGZpbGw9IiMyNTYzZWIiIGQ9Ik01MCA2MGgxMDV2NDVINTB6IE04NSAxMDVoMzV2NjBIODV6XCIvPjxwYXRoIGZpbGw9IiMzMzQxNTUiIGQ9Ik0xNTUgNzBoMjV2MjVoLTI1eiBNMTgwIDc4aDMwdjloLTMweiBNNzAgMTU1aDY1djE1SDcwelwiLz48dGV4dCB4PSIxMjAiIHk9IjIxNSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9InNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMjAiIGZpbGw9IiMwZjE3MmEiPkFrdXRyZWxsPC90ZXh0Pjwvc3ZnPg==",
      "bookingId": 1,
      "startDate": "2026-10-02",
      "endDate": "2026-10-04",
      "bookingStatus": "P"
    }
  ],
  "availableTools": [
    {
      "toolId": 8,
      "toolName": "Projektor",
      "toolStatus": "A",
      "imageData": "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNDAiIGhlaWdodD0iMjQwIiB2aWV3Qm94PSIwIDAgMjQwIDI0MCI+PHJlY3Qgd2lkdGg9IjI0MCIgaGVpZ2h0PSIyNDAiIHJ4PSIxNiIgZmlsbD0iI2YxZjVmOSIvPjx0ZXh0IHg9IjEyMCIgeT0iMTI1IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIyMCIgZmlsbD0iIzBmMTcyYSI+UHJvamVrdG9yPC90ZXh0Pjwvc3ZnPg=="
    },
    {
      "toolId": 4,
      "toolName": "Hekikäärid",
      "toolStatus": "A",
      "imageData": "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNDAiIGhlaWdodD0iMjQwIiB2aWV3Qm94PSIwIDAgMjQwIDI0MCI+PHJlY3Qgd2lkdGg9IjI0MCIgaGVpZ2h0PSIyNDAiIHJ4PSIxNiIgZmlsbD0iI2YxZjVmOSIvPjxnIGZpbGw9Im5vbmUiIHN0cm9rZS13aWR0aD0iMTAiPjxwYXRoIHN0cm9rZT0iIzY0NzQ4YiIgZD0iTTY1IDM1bDEwNSAxMzUgTTE3NSAzNUw3MCAxNzBcIi8+PHBhdGggc3Ryb2tlPSIjZWE1ODBjIiBkPSJNMTQ1IDE0MGwyNSAzMCBNOTUgMTQwbC0yNSAzMFwiLz48L2c+PGNpcmNsZSBjeD0iMTIwIiBjeT0iMTA1IiByPSI5IiBmaWxsPSIjMzM0MTU1XCIvPjx0ZXh0IHg9IjEyMCIgeT0iMjE1IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIyMCIgZmlsbD0iIzBmMTcyYSI+SGVraWvDpMOkcmlkPC90ZXh0Pjwvc3ZnPg=="
    },
    {
      "toolId": 3,
      "toolName": "Tolmuimeja",
      "toolStatus": "A",
      "imageData": "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNDAiIGhlaWdodD0iMjQwIiB2aWV3Qm94PSIwIDAgMjQwIDI0MCI+PHJlY3Qgd2lkdGg9IjI0MCIgaGVpZ2h0PSIyNDAiIHJ4PSIxNiIgZmlsbD0iI2YxZjVmOSIvPjxwYXRoIGZpbGw9IiMwZDk0ODgiIGQ9Ik02NSA5MGgxMDB2NjBINjV6XCIvPjxwYXRoIGZpbGw9Im5vbmUiIHN0cm9rZT0iIzBkOTQ4OCIgc3Ryb2tlLXdpZHRoPSIxNSIgZD0iTTg1IDkwVjU1aDU1djM1XCIvPjxwYXRoIHN0cm9rZT0iIzMzNDE1NSIgc3Ryb2tlLXdpZHRoPSI4IiBkPSJNNTUgMTU1aDEyNSBNMTUwIDEzMHY0NVwiLz48dGV4dCB4PSIxMjAiIHk9IjIxNSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9InNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMjAiIGZpbGw9IiMwZjE3MmEiPlRvbG11aW1lamE8L3RleHQ+PC9zdmc+"
    }
  ],
  "rentedOutTools": []
}
```

Näide on [3_import.sql](../../../database/3_import.sql) Liis Kask (`userId=3`) fikseeritud testkuupäeval **25.09.2026**, mitte dünaamiline tänase päeva ootus. Broneering 1 Akutrellile on P; Redeli broneering 2 lõppes 21.09; Hekikääride broneering 3 on R. Seetõttu on näites ainult üks outgoingRequests kirje ja kolm vaba oma tööriista. Pildistringid on kodeeritud otse impordi SVG baitidest, nende sisu parandamata.

`MyToolsResponseDto`: Integer userId; String firstName, lastName, email, phone; neli List<MyToolBookingDto> loendit (myRentals, incomingRequests, outgoingRequests, rentedOutTools) ning List<MyToolCardDto> availableTools.

`MyToolCardDto`: Integer toolId; String toolName, toolStatus, imageData. `MyToolBookingDto` sisaldab samu välju ning lisaks Integer bookingId, LocalDate startDate/endDate ja String bookingStatus. imageData on ainult is_main=true pildi Base64 ilma data-URL prefiksita; puuduva peapildi korral null. Lisapilti ei valita automaatselt asemele. Telefon lisatakse profile.phone kaudu, sest see on kujunduses nähtav, kuid vanas API näites puudub.

## Eesmärk ja valikureeglid

Teenus täidab kasutaja profiilikaardi ja kujunduse viis rühma ühe vastusega. See on lugemispäring: ei kinnita taotlusi, muuda tööriista staatust ega saada e-kirju.

| UI rühm | DTO loend | Valikureegel |
|---|---|---|
| Minu laenutused | myRentals | booking.renter_id = sessiooni kasutaja, tool.owner_id != kasutaja, booking.status=C, start_date <= täna <= end_date. |
| Kinnituse ootel → Palun kinnita | incomingRequests | tool.owner_id = kasutaja, booking.renter_id != kasutaja, booking.status=P, end_date >= täna. |
| Kinnituse ootel → Ootab omaniku kinnitust | outgoingRequests | booking.renter_id = kasutaja, tool.owner_id != kasutaja, booking.status=P, end_date >= täna. |
| Minu tööriistad → Vabad | availableTools | tool.owner_id = kasutaja, tool.status=A ning puudub praegu kehtiv C broneering. |
| Minu tööriistad → Välja laenatud | rentedOutTools | tool.owner_id = kasutaja, teise kasutaja booking.status=C, start_date <= täna <= end_date. Tool.status ei asenda broneeringu kontrolli. |

Kuupäeva- ja olekureeglid on kujunduse põhjal tehtud tehnilised täpsustused: PDF näitab rühmade nimetusi, kuid ei määra piirpäevi. „Täna“ võetakse üks kord päringu alguses serveri Europe/Tallinn ajavööndis. Algus- ja lõpppäev loetakse perioodi sisse.

Esimeses, teises, kolmandas ja viiendas loendis on üks kaart broneeringu kohta (`bookingId`); neljandas üks kaart tööriista kohta (`toolId`). Sama tööriista mitu taotlust ei tohi kaduda grupeerimise tõttu. Vaba tööriist võib olla ka ootel taotluste loendis: P ei ole kinnitatud laenutus. Ükski praegu välja laenatud tööriist ei ole samal ajal Vabad rühmas.

Broneeringuloendite järjestus: start_date ASC, booking.id ASC. Vabad: tool.created_at DESC, tool.id DESC. Tagastatakse kõik sobivad kirjed ilma paginatsioonita, sest kujunduses lehevahetust ei ole. Kõik tühjad loendid on [], mitte null.

**Kujundusega katmata olukorrad:** tulevane kinnitatud broneering ei kuulu praeguste laenutuste rühma; U tööriist ilma käimasoleva C broneeringuta ei kuulu Vabad ega Välja laenatud rühma. Käesolev leping jätab need sellest ülevaatest välja ja ei nimeta neid ekslikult vabaks või ootel olevaks. Nende eraldi kuvamine vajab edasist tooteotsust; neid ei kustutata andmebaasist. Tagasilükatud ja lõppenud taotlusi samuti selles vaates ei näidata.


Kontrolli sessiooni, kasutaja blokeeritust ja profiili olemasolu enne loendite koostamist. Kõik päringud peavad kasutama sama sessiooni userId-d; kliendilt kasutaja ID-d ei küsita. Profiili nimi tuleb app_user, email ja phone profile tabelist. Piltide liitmine ei tohi paljundada broneeringuid ega kaarte.

## Seotud andmebaasi tabelid

### `app_user`

```sql
CREATE TABLE app_user (
    id serial PRIMARY KEY,
    role_id integer NOT NULL REFERENCES role (id),
    first_name varchar(100) NOT NULL,
    last_name varchar(100) NOT NULL,
    google_sub varchar(255) NOT NULL UNIQUE,
    status char(1) NOT NULL DEFAULT 'A' CHECK (status IN ('A', 'B'))
);
```

### `profile`

```sql
CREATE TABLE profile (
    id serial PRIMARY KEY,
    user_id integer NOT NULL UNIQUE REFERENCES app_user (id),
    location_id integer NOT NULL REFERENCES location (id),
    email varchar(254) NOT NULL UNIQUE,
    phone varchar(32) NOT NULL,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp
);
```

### `tool`

```sql
CREATE TABLE tool (
    id serial PRIMARY KEY,
    owner_id integer NOT NULL REFERENCES app_user (id),
    category_id integer NOT NULL REFERENCES category (id),
    name varchar(150) NOT NULL,
    description varchar(2000),
    status char(1) NOT NULL DEFAULT 'A' CHECK (status IN ('A', 'U')),
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp
);
```

### `tool_image`

```sql
CREATE TABLE tool_image (
    id serial PRIMARY KEY,
    tool_id integer NOT NULL REFERENCES tool (id) ON DELETE CASCADE,
    image_data bytea NOT NULL,
    is_main boolean NOT NULL DEFAULT false,
    CONSTRAINT tool_image_data_unique UNIQUE (tool_id, image_data)
);
```

### `booking`

```sql
CREATE TABLE booking (
    id serial PRIMARY KEY,
    tool_id integer NOT NULL REFERENCES tool (id),
    renter_id integer NOT NULL REFERENCES app_user (id),
    start_date date NOT NULL,
    end_date date NOT NULL,
    status char(1) NOT NULL DEFAULT 'P' CHECK (status IN ('P', 'C', 'R')),
    owner_message varchar(500),
    google_event_id varchar(255) UNIQUE,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp,
    CONSTRAINT booking_period_check CHECK (start_date <= end_date)
);
```

Vt [2_create.sql](../../../database/2_create.sql). Seosed: profile.user_id → app_user.id; tool.owner_id → app_user.id; booking.renter_id → app_user.id; booking.tool_id → tool.id; tool_image.tool_id → tool.id. category, location ja role on skeemi FK sihid, kuid selle vastuse koostamiseks pole nende välju vaja. Andmeid ega skeemi ei muudeta.

## Veaolukorrad

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | AUTHENTICATION_REQUIRED | Vaate avamiseks logi sisse. | Ühine sisselogimise käsitlus. |
| 403 | USER_BLOCKED | Sinu konto on blokeeritud. | Kuva viga ja peata selle vaate tegevused. |
| 404 | PROFILE_NOT_FOUND | Kasutaja profiili ei leitud. | Kuva teade ja suuna MyProfile täitmise voogu. |
| 500 | INTERNAL_SERVER_ERROR | Minu tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva laadimisviga ja võimalda korduskatse. |

```json
{
  "errorCode": "AUTHENTICATION_REQUIRED",
  "message": "Vaate avamiseks logi sisse."
}
```

```json
{
  "errorCode": "USER_BLOCKED",
  "message": "Sinu konto on blokeeritud."
}
```

```json
{
  "errorCode": "PROFILE_NOT_FOUND",
  "message": "Kasutaja profiili ei leitud."
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Minu tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

401/403/404 ärikoodid pärinevad siniselt sildilt, 500 on taski tehniline täpsustus. Praegune OAuth task kirjeldab /api/me jaoks tühja 401 body't; siin kirjeldatud AUTHENTICATION_REQUIRED JSON tuleb seadistada just sellele endpoint'ile ilma teiste teenuste lepingut muutmata. USER_BLOCKED kontroll arvestab ka varem loodud sessiooni. Profile puudumine ei ole tühi edukas tööriistaloend; kasutaja saab profiili täita MyProfile kaudu.

## Vastuvõtu kriteeriumid

- [ ] Vastus sisaldab profiili nime/e-posti/telefoni ja kõiki viit kirjeldatud loendit; vana rentedTools/bookings/ownedTools kuju ei jää uue FE aluseks.
- [ ] Customer ja Admin näevad ainult sessiooniga seotud andmeid; kliendi userId ei mõjuta tulemust.
- [ ] Kõigi rühmade omaniku/rentija suund, P/C/R olek ja piirpäevad vastavad reeglitabelile.
- [ ] Välja laenatud tööriist ei ilmu Vabad loendis; mitu sama tööriista taotlust säilitavad eri bookingId-d.
- [ ] Loendid on deterministlikult sorditud ja tühjal juhul []; puuduv peapilt annab null, mitte kadunud kaardi.
- [ ] 25.09.2026 fikseeritud testkuupäeva Liisi vastus vastab JSON näitele.
- [ ] 401/403/404/500 vastused vastavad lepingule, päring ei muuda andmeid.
- [ ] Automaattestid katavad erinevad kasutajad, viis loendit, kuupäevapiirid, tulevase C ja U väljajäämise, puuduvad pildid, duplikaadivabad seosed ja vead.
