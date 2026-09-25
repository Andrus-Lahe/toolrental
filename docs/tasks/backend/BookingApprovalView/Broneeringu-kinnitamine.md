# Broneeringu kinnitamine

**Teenus:** `PATCH /api/bookings/{bookingId}/confirm`

**Vaste balsamic mockupis:** BookingApprovalView.vue (eraldi STEP-tähis puudub), lehekülg 7/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (vt `Broneeringu-kinnitamine.png`).

![Mockup](./Broneeringu-kinnitamine.png)

Täiendavad allikad: [BookingApprovalView märkmed](../../../balsamic/notes/BookingApprovalView-markmed.md) ja [spring_mail.md](../../../balsamic/notes/spring_mail.md) (controlleri, service'i ja e-kirja koodinäide). Task kirjeldab backend'i teenust, mitte Vue vaate teostust.

## Sisend

| Parameeter | Asukoht | Java tüüp | Kohustuslik | Tähendus |
|---|---|---|---|---|
| `bookingId` | Path variable | `Integer` | Jah | Kinnitatava broneeringu `booking.id`. |
| `ownerMessage` | Request body | `String` | Ei | Omaniku sõnum laenajale, kuni 500 märki, võib olla `null`. |

Request body (`BookingDecisionRequest.java`), näide `PATCH /api/bookings/1/confirm`:

```json
{
  "ownerMessage": "Palun tagasta redel 21. septembril enne kella 18."
}
```

Näite tekst on võetud faili `3_import.sql` kinnitatud broneeringult `bookingId = 2`.

DTO (vt `spring_mail.md`):

```java
public record BookingDecisionRequest(
        @Size(max = 500, message = "Sõnum võib olla kuni 500 märki") String ownerMessage
) {}
```

Päring nõuab sisselogimist. Omaniku ID võetakse sessioonist (`@AuthenticationPrincipal AppUserPrincipal`), mitte frontendist.

## Väljund

**Response (200 OK):** tühi body (`Response (200): NONE`).

Ühes transaktsioonis (`@Transactional`):

1. `booking.status` = `'C'`.
2. `booking.owner_message` kirjutatakse üle päringu `ownerMessage` väärtusega, ka siis, kui see on `null`. Laenaja taotluses saadetud sõnum kaob, sest sama välja kasutavad mõlemad pooled (kasutaja otsus).
3. `booking.updated_at` = praegune aeg.
4. Rentijale saadetakse e-kiri (vt `spring_mail.md`):
   - saaja: rentija `profile.email`;
   - Reply-To: omaniku `profile.email` (või tühi, kui omanikul pole profiili);
   - teema: `Broneering kinnitatud: <tool.name>`;
   - sisu: pöördumine, tööriist, periood, omaniku sõnum (kui on) ja link `{toolrental.frontend-url}/bookings/{bookingId}`.

Kui rentijal pole profiili, kirja ei saadeta ja see logitakse. E-kirja saatmise viga (`MailException`) ainult logitakse: broneering jääb kinnitatuks ja vastus on 200.

`google_event_id` ja teised broneeringud ei muutu.

Sama tööriista taotlused ei kattu, sest kattumine välistatakse juba broneerimise algatamisel (`POST /api/bookings`). Uus taotlus saab staatuse `P` (ootel) ja hõivab tööriista valitud perioodiks kuni omaniku otsuseni. Seetõttu ei pea kinnitamine kattuvaid taotlusi kontrollima ega muutma.

## Eesmärk

Teenust kasutab BookingApprovalView nupp „Kinnita“, mis on nähtav ainult tööriista omanikule ootel taotluse korral. Omanik kinnitab laenaja taotluse ja saab lisada sõnumi („Lisainfo laenajale“). Eduka vastuse järel kuvab vaade modaali „Taotlus kinnitatud“, mille sulgemisel suunatakse kasutaja lehele `/my-tools`. Rentija saab otsusest e-kirja.

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

Muudetakse veerge `status`, `owner_message` ja `updated_at`.

### `tool` (ainult lugemine)

```sql
CREATE TABLE tool (
    id serial PRIMARY KEY,
    owner_id integer NOT NULL REFERENCES app_user (id),
    ...
    name varchar(150) NOT NULL,
    ...
);
```

`tool.owner_id` järgi kontrollitakse, kas päringu tegija on omanik. `tool.name` läheb e-kirja.

### `app_user` ja `profile` (ainult lugemine)

`app_user.first_name` kasutatakse kirja pöördumises. `profile.email` annab kirja saaja (rentija) ja Reply-To (omanik). Tabelite struktuur: vt [Broneeringu andmete päring](./Broneeringu-andmete-paring.md).

Algandmed failist [3_import.sql](../../../database/3_import.sql):

| booking.id | Tööriist | Omanik | Rentija | status | Kinnitamise tulemus |
|---|---|---|---|---|---|
| 1 | Akutrell | Marko Tamm (1) | Liis Kask (3) | P | Omanik `userId = 1`: 200; rentija `userId = 3`: 403 `BOOKING_NOT_OWNER` |
| 2 | Redel | Marko Tamm (1) | Liis Kask (3) | C | 403 `BOOKING_NOT_PENDING` |
| 3 | Hekikäärid | Liis Kask (3) | Marko Tamm (1) | R | 403 `BOOKING_NOT_PENDING` |

## Veaolukorrad

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| `ownerMessage` on pikem kui 500 märki. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"ownerMessage: Sõnum võib olla kuni 500 märki"}` |
| Broneeringut `bookingId = 123` pole. Teates kasutada tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'bookingId' väärtusega: 123"}` |
| Päringu tegija pole tööriista omanik (ka rentija või admin). | 403 Forbidden | `{"errorCode":"BOOKING_NOT_OWNER","message":"Ainult tööriista omanik saab taotlust kinnitada või tagasi lükata"}` |
| Broneeringu staatus pole `P`. | 403 Forbidden | `{"errorCode":"BOOKING_NOT_PENDING","message":"Taotlus on juba kinnitatud või tagasi lükatud"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Taotluse kinnitamine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- 404 tuleb olemasolevast `PrimaryKeyNotFoundException` klassist.
- `BOOKING_NOT_OWNER` ja `BOOKING_NOT_PENDING` on uued koodid (kinnitatud sildil). Neid visatakse olemasoleva `ForbiddenException` klassiga `spring_mail.md` meetodis `decideBooking`.
- 400 tuleb `@Valid` + `@Size` kaudu olemasolevast `handleMethodArgumentNotValid` handlerist.
- Kontrollide järjekord: 400, 404, `BOOKING_NOT_OWNER`, siis `BOOKING_NOT_PENDING`. Vea korral andmeid ei muudeta ja e-kirja ei saadeta.
- E-kirja saatmise viga ei ole veaolukord: see logitakse ja vastus on 200.

## Vastuvõtu kriteeriumid

- [ ] `PATCH /api/bookings/{bookingId}/confirm` on olemas ja nõuab sisselogimist.
- [ ] Omanik `userId = 1` kinnitab `bookingId = 1`: vastus 200 tühja body'ga; `status = 'C'`, `owner_message` = päringu väärtus ja `updated_at` on uuendatud.
- [ ] `ownerMessage: null` või puuduv väli kirjutab `owner_message` väärtuseks `NULL`.
- [ ] Rentijale saadetakse e-kiri teemaga `Broneering kinnitatud: Akutrell`, saajaks `liis.kask@example.com` ja Reply-To `email@Gmail.com`.
- [ ] Rentija profiili puudumisel või `MailException` korral on vastus ikka 200 ja broneering kinnitatud; viga logitakse.
- [ ] Mitte-omanik saab 403 `BOOKING_NOT_OWNER`; juba otsustatud broneering (`bookingId = 2` või `3`) annab 403 `BOOKING_NOT_PENDING`.
- [ ] Olematu `bookingId` annab 404; üle 500 märgi pikk sõnum annab 400 eestikeelse teatega.
- [ ] Vea korral broneering ei muutu ja e-kirja ei saadeta.
- [ ] Automaattestid (e-kirja saatmine `JavaMailSender` mockiga) katavad eduka kinnitamise, `null` sõnumi, kirja sisu ja saajad, profiilita rentija, kirja saatmise vea ning 400/401/403/404/500 juhtumid.
