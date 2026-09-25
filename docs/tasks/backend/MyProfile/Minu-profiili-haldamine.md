# Minu profiili haldamine

**Teenus:** `GET /api/me/profile`, `PUT /api/me/profile`

**Vaste balsamic mockupis:** MyProfile.vue, lehekülg 11/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf).

![Mockup](./Minu-profiili-haldamine.png)

**Kasutaja kinnitatud ulatus:** profiili täitmine ja muutmine pärast Google sisselogimist. See asendab PDF-i vana RegisterView / POST /api/users / googleSub body / sessionStorage-põhise registreerimise kirjelduse. [Google autentimise task](../GoogleLoginView/Google-kontoga-sisselogimine.md) loob app_user juba sisselogimisel. Uued endpoint'id, DTO nimed ja `/my-profile` route on selle ülesande tehnilised lahendusettepanekud; need ei ole olemasolev API.

## Sisend ja väljund

### `GET /api/me/profile`

Path/query parameetrid ja request body puuduvad. Kasutaja ID tuleb sessiooni principal'ist. `MyProfileResponseDto` — response 200:

```json
{
  "userId": 3,
  "hasProfile": true,
  "firstName": "Liis",
  "lastName": "Kask",
  "email": "liis.kask@example.com",
  "phone": "55501002",
  "districtId": 2,
  "streetName": "Sõpruse pst",
  "houseNumber": "120",
  "apartmentNumber": "8",
  "cityId": 1
}
```

Näide vastab `3_import.sql` Liisi profiilile ja asukohale. `userId`, `cityId`, `districtId` on Integer, `hasProfile` boolean, ülejäänud väljad String. `cityId` tuletatakse district.city_id kaudu, et frontend saaks linna ja linnaosa eeltäita.

Kui profiili veel pole, tagastatakse samuti 200: userId, firstName ja lastName olemasolevast app_user kirjest, email Google principal'ist ning hasProfile=false; phone, cityId, districtId, streetName, houseNumber ja apartmentNumber on null. See on vormi algolek, mitte 404. Puuduv Google email jääb nulliks ja kasutaja täidab selle vormil. Olemasoleva profiili korral kasutatakse alati salvestatud kontakte, mitte Google väärtustega ülekirjutamist.

### `PUT /api/me/profile`

Path/query parameetrid puuduvad. `MyProfileRequestDto` — request body:

```json
{
  "firstName": "Liis",
  "lastName": "Kask",
  "email": "liis.kask@example.com",
  "phone": "55501002",
  "districtId": 2,
  "streetName": "Sõpruse pst",
  "houseNumber": "120",
  "apartmentNumber": "8"
}
```

Response 200: sama täielik `MyProfileResponseDto` nagu GET näites, `hasProfile: true`. Esimesel salvestamisel luuakse olemasolevale app_user kirjele profile ja location; hiljem uuendatakse sama kasutaja profiili. PUT ei loo app_user kirjet ega muuda google_sub, rolli või staatust.

| Väli | Valideerimine |
|---|---|
| firstName | Kohustuslik mittetühi String, kuni 100 märki. |
| lastName | String kuni 100 märki; tühi on lubatud Google ühe nimega kontode jaoks. Puuduv/null normaliseeritakse tühjaks stringiks. |
| email | Kohustuslik mittetühi korrektne e-post, kuni 254 märki; ei tohi kuuluda teise kasutaja profiilile. Oma muutmata e-post on lubatud. |
| phone | Kohustuslik mittetühi String, kuni 32 märki; täiendavat riigipõhist mustrit ei kehtestata. |
| districtId | Kohustuslik positiivne Integer ja olemasolev district. |
| streetName | Kohustuslik mittetühi String, kuni 150 märki. |
| houseNumber | Kohustuslik mittetühi String, kuni 20 märki; mitte arv, et lubada nt tähelisi numbreid. |
| apartmentNumber | Valikuline String kuni 20 märki, tühi/puuduv väärtus normaliseeritakse nulliks. |

cityId kuulub GET vastusesse ja FE valikusse, mitte PUT body sisse: linn määratakse valitud district'i järgi. userId, profileId, locationId, googleSub, roleName, hasProfile ja ajatemplid ei ole kliendi muudetavad väljad. Mõlemad endpoint'id on ainult autenditud kasutajale, sh Adminile tema enda profiili jaoks.

## Eesmärk ja ärireeglid

Üks vorm toetab profiili esmast täitmist ja hilisemat muutmist. Esmasel täitmisel eeltäidetakse Google sisselogimisest juba salvestatud nimed ja principal'i e-post; olemasoleva profiili muutmisel laaditakse salvestatud andmed.

1. Tuvasta kasutaja sessioonist. GET ei loo ega muuda andmeid. PUT valideerib kõik väljad, district'i olemasolu ja e-posti unikaalsuse teise kasutaja suhtes enne salvestamist.
2. Salvesta app_user ees-/perekonnanimi, profiili kontaktid ja aadress ühes transaktsioonis. Profiili loomisel täida created_at/updated_at; muutmisel säilita created_at ja uuenda updated_at. Sama konto korduv/samaaegne esmasalvestamine ei tohi luua mitut profiili.
3. Asukoht võib skeemi järgi olla jagatud. Aadressi muutmisel ära muuda teise profiili aadressi: loo muutunud aadressile uus location ja seo ainult käesolev profiil sellega. Muutumatu aadressi korral säilita olemasolev location. Vana asukoha automaatne kustutamine ega geokodeerimine ei kuulu taski; uue asukoha lng/lat on null.
4. E-posti duplikaadikontroll välistab sama profiili. Säilita skeemi täpne unikaalsusreegel; case-insensitive skeemimuudatust ei lisata. Konkureeriva salvestuse unikaalsusviga tuleb samuti teisendada EMAIL_ALREADY_EXISTS-iks.
5. Pärast salvestamist peab olemasolev GET /api/me tagastama värske nime, profiili e-posti ja hasProfile=true. Autentimine säilib; profiili salvestamine ei ole uus Google registreerimine.
6. PUT kasutab ühise sessioonilahenduse CSRF-kaitset. Saadetud võõras userId või roll ei tohi mõjutada salvestatavat kontot. Profiilita autenditud kasutaja peab saama neid endpoint'e kasutada, et vältida ligipääsureeglite ummikut.

## Seotud andmebaasi tabelid

Vt [2_create.sql](../../../database/2_create.sql). Skeemi muutmist ei nõuta.

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

### `location`

```sql
CREATE TABLE location (
    id serial PRIMARY KEY,
    district_id integer NOT NULL REFERENCES district (id),
    street_name varchar(150) NOT NULL,
    house_number varchar(20) NOT NULL,
    apartment_number varchar(20),
    lng decimal(10, 7),
    lat decimal(10, 7)
);
```

### `district`

```sql
CREATE TABLE district (
    id serial PRIMARY KEY,
    city_id integer NOT NULL REFERENCES city (id),
    district_name varchar(100) NOT NULL,
    CONSTRAINT district_city_name_unique UNIQUE (city_id, district_name)
);
```

### `city`

```sql
CREATE TABLE city (
    id serial PRIMARY KEY,
    city_name varchar(100) NOT NULL UNIQUE
);
```

role tabelit kasutatakse olemasolevas autentimises, selle väärtusi vorm ei muuda. Näidisandmed [3_import.sql](../../../database/3_import.sql): app_user 3 Liis Kask, profile 3, location 3, district 2 Mustamäe, city 1 Tallinn. Profiili e-post on liis.kask@example.com, telefon 55501002, aadress Sõpruse pst 120–8. Import sisaldab juba profiili, seega esmatäitmise test loob eraldi profiilita autentitud konto testandmetes.

## Veaolukorrad

| Päring | Status code | errorCode | message / body |
|---|---|---|---|
| GET/PUT | 401 | — | Tühi body; sessioon puudub või on aegunud. |
| PUT | 400 | INCORRECT_INPUT | <väli>: <valideerimise teade> |
| PUT | 403 | EMAIL_ALREADY_EXISTS | Sellise e-postiga kasutaja on juba süsteemis olemas |
| PUT | 404 | PRIMARY_KEY_NOT_FOUND | Ei leidnud primary keyd 'districtId' väärtusega: 123 |
| GET | 500 | INTERNAL_SERVER_ERROR | Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti. |
| PUT | 500 | INTERNAL_SERVER_ERROR | Profiili salvestamine ebaõnnestus. Palun proovi hiljem uuesti. |

```json
{
  "errorCode": "INCORRECT_INPUT",
  "message": "<väli>: <valideerimise teade>"
}
```

```json
{
  "errorCode": "EMAIL_ALREADY_EXISTS",
  "message": "Sellise e-postiga kasutaja on juba süsteemis olemas"
}
```

```json
{
  "errorCode": "PRIMARY_KEY_NOT_FOUND",
  "message": "Ei leidnud primary keyd 'districtId' väärtusega: 123"
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Profiili salvestamine ebaõnnestus. Palun proovi hiljem uuesti."
}
```

403 EMAIL_ALREADY_EXISTS ja district'i 404 säilitavad PDF-i semantika. GOOGLE_ACCOUNT_ALREADY_REGISTERED selles voos puudub: olemasolev Google konto on eeltingimus, mitte viga. 400 järgib ApiError stringvälju; 500 tekstid on taski tehnilised täpsustused. CSRF-kontrolli 403 ei tohi kuvada e-posti duplikaadina. Vigade korral rulluvad nimede, kontaktide ja aadressi muudatused koos tagasi.

## Vastuvõtu kriteeriumid

- [ ] GET tagastab salvestatud profiili või 200 eeltäidetava profiilita oleku; GET ei kirjuta andmebaasi.
- [ ] Esimene PUT loob profile/location olemasolevale kasutajale, korduv PUT muudab sama profiili; app_user kirjete arv ei kasva.
- [ ] Rentija ega Admin ei saa muuta teise kasutaja profiili kliendi ID kaudu; Google seos, roll ja staatus säilivad.
- [ ] Oma muutmata e-post on lubatud; teise profiili e-post annab 403 EMAIL_ALREADY_EXISTS ka konkureerivate päringute korral.
- [ ] Valideerimise piirväärtused, tühi perekonnanimi ja valikuline korterinumber on toetatud; puuduv district annab 404.
- [ ] Aadressi muutmine ei muuda jagatud asukohta kasutava teise profiili andmeid.
- [ ] Kogu salvestus on atomaarne ja /api/me näeb pärast seda värskeid andmeid.
- [ ] Testid katavad esimese/korduva salvestuse, profiilita GET, autentimise, CSRF, võõra ID, e-posti konflikti, väljade piirid, jagatud aadressi ja rollback'i.
