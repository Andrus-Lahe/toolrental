# Laenutaotluse loomine

**Teenus:** `POST /api/bookings`

**Vaste balsamic mockupis:** BookingFormView.vue (eraldi STEP-tähis puudub), lehekülg 5/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf).

![Mockup](./Laenutaotluse-loomine.png)

Allikas: [BookingFormView-markmed.md](../../../balsamic/notes/BookingFormView-markmed.md) ning kasutaja täpsustus: oma tööriista broneerimine peab tagastama 403 `OWN_TOOL_BOOKING_FORBIDDEN`, ilma booking kirjet loomata. Pärast FE kinnitusmodaali sulgemist minnakse „Minu tööriistad” vaatesse; see täpsustus asendab märkmete `/my-bookings` sihi.

## Sisend

Autenditud Customer või Admin saadab `BookingCreateRequestDto.java` JSON body. Path- ja query-parameetreid pole.

```json
{
  "toolId": 2,
  "startDate": "2026-09-18",
  "endDate": "2026-09-21",
  "ownerMessage": "Palun tagasta redel 21. septembril enne kella 18."
}
```

| Väli | Java tüüp | Reegel |
|---|---|---|
| toolId | Integer | Kohustuslik positiivne tööriista ID. |
| startDate | LocalDate | Kohustuslik kuupäev kujul `YYYY-MM-DD`. |
| endDate | LocalDate | Kohustuslik kuupäev kujul `YYYY-MM-DD`, peab olema startDate'iga samal päeval või hiljem. |
| ownerMessage | String | Valikuline tekst, kuni 500 märki vastavalt skeemile; puuduv/null väärtus salvestatakse nullina. |

`renterId`, `status`, `bookingId`, ajatemplid ega Google sündmuse ID ei kuulu request DTO-sse. Rentija tuleb sessiooni principal'ist; kliendi võimalikku lisavälja ei tohi kasutada selle asendamiseks. Rakenda sessioonipõhise autentimise taskiga kooskõlaline CSRF-kaitse; CSRF token pole ärilise DTO osa.

Kohustuslikkus, positiivne ID ja pikkuse kontroll on skeemist tuletatud tehnilised täpsustused. Mineviku kuupäevade keeld pole allikates kokku lepitud: task ei lisa `@FutureOrPresent` piirangut.

## Väljund

**Response (200 OK):** `BookingResponseDto.java`, loodud taotlus staatusega `P`.

```json
{
  "bookingId": 2,
  "toolId": 2,
  "renterId": 3,
  "startDate": "2026-09-18",
  "endDate": "2026-09-21",
  "status": "P",
  "ownerMessage": "Palun tagasta redel 21. septembril enne kella 18."
}
```

Väljad: `Integer bookingId`, `Integer toolId`, `Integer renterId`, `LocalDate startDate`, `LocalDate endDate`, `String status`, `String ownerMessage`. `bookingId` määrab andmebaas, `renterId` serveri sessioon. Valikuline `ownerMessage` võib olla null.

Ülal on mockupi näide, mitte uue kirje fikseeritud ID. Impordis on `booking.id = 2` juba olemas staatusega `C` ning tööriist 2 „Redel” staatusega `U`; mockup näitab uue taotluse lepingut `P`. Tegelik uus ID genereeritakse ja FE ei saa impordi Redelit selle saadavusoleku tõttu saata. Eduka integratsioonitesti jaoks sobib näiteks impordi aktiivne tööriist 1 „Akutrell” (omanik 1), rentijaga 3 ning testis valitud kehtiva kuupäevavahemikuga. Ära kirjuta olemasolevat broneeringut üle ega muuda demotööriista staatust taski koostamisel.

## Eesmärk

BookingFormView saadab kasutaja laenutaotluse tööriista omanikule kinnitamiseks. Taotlus luuakse alati ootel olekus `P`; kinnitamine `C` või tagasilükkamine `R` toimub eraldi voos. Teenus ei tähenda automaatset kinnitamist, e-kirja saatmist ega Google Calendar sündmuse loomist.

Teostuse järjekord:

1. Tuvasta autenditud kasutaja sessioonist ja valideeri request.
2. Leia tööriist; puuduva ID puhul tagasta kirjeldatud 404.
3. Võrdle sessiooni kasutaja ID-d `tool.owner_id` väärtusega. Võrdsuse korral tagasta 403 `OWN_TOOL_BOOKING_FORBIDDEN` ja ära loo booking kirjet. Sama reegel kehtib Adminile.
4. Kontrolli kuupäevavahemikku ka serveris. Kõik kontrollid peavad eelnema salvestamisele.
5. Salvesta tehingus uus `booking`: tool_id request'ist, renter_id sessioonist, kuupäevad ja valikuline teade request'ist, status `P`. `google_event_id` jääb nulliks; ajatemplid järgitakse skeemi järgi.
6. Tagasta salvestatud kirje DTO ja HTTP 200. Vea korral ei tohi jääda osalist kirjet.

**Kokkuleppimata reeglid:** kuupäevade kattuvuse kontrolli, arvesse võetavaid `P`/`C` olekuid ega `DATE_RANGE_UNAVAILABLE` veakoodi kasutaja ei kinnitanud. Neid ei lisata selle taski lepingusse. `tool.status != A` korral nõuavad märkmed FE saatmisnupu keelamist; vastava serveripoolse keelu vealeping jäi kinnitamata ja tuleb enne saadavuse serverikontrolli teostamist täpsustada. FE keeld üksi ei taga serveris saadavust.

## Seotud andmebaasi tabelid

Vt [2_create.sql](../../../database/2_create.sql).

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

`booking.tool_id` viitab tööriistale, `booking.renter_id` kasutajale. `tool.owner_id` viitab omanikule; selle võrdlus rentijaga on äriloogika, mitte olemasolev SQL CHECK. `booking_period_check` tagab kuupäevade järjekorra, kuid ei kontrolli kattuvust. `google_event_id` on unikaalne valikuline väärtus, mida loomise käigus ei täideta.

`app_user.role_id` viitab rollile ja `tool.category_id` kategooriale; neid tabeleid see teenus ei muuda. `profile`, `tool_image`, `category_image` ega kontaktandmete lugemine ei kuulu POST päringusse.

[3_import.sql](../../../database/3_import.sql) seotud näidisandmed:

| Kirje | Väärtused |
|---|---|
| app_user 1 | Marko Tamm, role_id 1 (admin), status A |
| app_user 3 | Liis Kask, role_id 2 (customer), status A |
| tool 1 | Akutrell, owner_id 1, status A |
| tool 2 | Redel, owner_id 1, status U |
| booking 1 | tool_id 1, renter_id 3, 2026-10-02 kuni 2026-10-04, status P |
| booking 2 | tool_id 2, renter_id 3, 2026-09-18 kuni 2026-09-21, status C |

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Sessioon puudub või on aegunud. | 401 Unauthorized | Tühi body, kooskõlas autentimise taskiga. |
| Sessiooni kasutaja ID võrdub `tool.owner_id` väärtusega. | 403 Forbidden | `{"errorCode":"OWN_TOOL_BOOKING_FORBIDDEN","message":"Enda tööriista broneerimine ei ole lubatud."}` |
| Tööriista ID ei eksisteeri, näites 123. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'toolId' väärtusega: 123"}` |
| `endDate < startDate`. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"endDate: peab olema startDate'iga samal päeval või hiljem"}` |
| Kohustuslik väärtus puudub, ID pole positiivne, kuupäev on vigane või teade ületab 500 märki. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"<väli>: <valideerimisvea kirjeldus>"}`; täpne tekst sõltub valideerimisreeglist. |
| Ootamatu salvestamise tõrge. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Laenutaotluse saatmine ebaõnnestus. Palun proovi hiljem uuesti."}` |

403 reegel ja FE teate sõnastus on kasutaja kinnitatud; sama sõnum backendis on selle taski lepingu täpsustus. 400 kuupäevavahemiku ja 404 vead pärinevad märkmetest; PDF-i 99 ja märkmete 123 on üksnes puuduva ID näited. Ülejäänud sisendivalideerimine ning 500 leping on tehnilised täpsustused. Kasuta olemasolevat `ApiError` kuju (`String errorCode`, `String message`), `ForbiddenException` ja `PrimaryKeyNotFoundException` käsitlemist. Praegune veahaldur ei taga veel kõiki kirjeldatud valideerimise ja 500 vastuseid; need tuleb teostamisel katta.

CSRF ebaõnnestumine võib autentimiskihis anda samuti 403, kuid seda ei tohi märgistada `OWN_TOOL_BOOKING_FORBIDDEN` koodiga. FE eristab oma tööriista keeldu nii staatuse kui veakoodi järgi.

## Vastuvõtu kriteeriumid

- [ ] Autenditud Customer ja Admin saavad saata kirjeldatud request'i `POST /api/bookings` aadressile; autentimata kasutaja saab 401.
- [ ] Edukal loomisel tagastatakse 200 ja kirjeldatud DTO; kirje ID genereeritakse, staatus on P ja rentija tuleb sessioonist.
- [ ] Oma tööriista korral saab nii Customer kui Admin 403 `OWN_TOOL_BOOKING_FORBIDDEN`; booking kirjete arv ei muutu.
- [ ] Kliendilt saadetud renterId ega status ei saa serveri määratud väärtusi muuta.
- [ ] Puuduv tööriist annab 404; vigased/puuduvad kuupäevad, pööratud vahemik, vigane ID ja liiga pikk teade annavad 400.
- [ ] Sama päeva algus ja lõpp on lubatud; ownerMessage puudumine/null ning 500 märki on lubatud, 501 märki mitte.
- [ ] Salvestamise tõrkel ei jää osalist kirjet ning tagastatakse kirjeldatud üldine viga.
- [ ] Teenus ei kinnita taotlust, muuda tööriista staatust ega loo Google sündmust.
- [ ] Automaattestid katavad loomise, sessiooni rentija, oma tööriista keelu koos salvestamise puudumisega, 400/401/404/500, piirväärtused ja tehingu tagasipööramise.
- [ ] Saadavuse serverikontrolli ja kattuvuse lahtised küsimused on nähtavalt eristatud kinnitatud nõuetest.
