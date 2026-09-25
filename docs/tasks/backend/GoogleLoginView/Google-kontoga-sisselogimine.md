# Google kontoga sisselogimine

**Teenus:** `GET /api/me`; seotud OAuth sisselogimisvoog ja `POST /logout`.

**Vaste balsamic mockupis:** GoogleLoginView.vue, lehekülg 2/17 failis [Laenukas.pdf](../../../balsamic/notes/Laenukas.pdf) (eraldi STEP-tähis puudub).

![Mockup](./Google-kontoga-sisselogimine.png)

Täiendav allikas: [googlega_login.md](../../../balsamic/notes/googlega_login.md). Backendis autentimise Controller, DTO, SecurityConfig ja OAuth sõltuvus praegu puuduvad. Task kirjeldab soovitud teostust, mitte juba töötavat lepingut.

## Sisend

`GET /api/me` teenusel puuduvad sisendid: path/query parameetreid ja request body pole. Kasutaja tuvastatakse serveri sessioonist, mitte frontendist saadetud ID kaudu.

Seotud voog:

- `GET /oauth2/authorization/google`: brauseri täislehe navigatsioon, mitte Axios päring. Alustab Google OIDC autentimist scope'idega `openid`, `email`, `profile`.
- `/login/oauth2/code/google`: Spring Security hallatav callback. Protokolli parameetrid ja nende kontroll kuuluvad OAuth teostusele; klient ei saada siia omaloodud kasutajaandmeid.
- `POST /logout`: request body puudub; sessiooniküpsis ja CSRF token peavad kaasas olema.

Seadistuses kasuta `GOOGLE_CLIENT_ID` ja `GOOGLE_CLIENT_SECRET` keskkonnamuutujaid. Client secret jääb backendisse. Kohalik callback on juhendi järgi `http://localhost:5173/login/oauth2/code/google`, läbi Vite proxy; Google Console'i seadistus peab sellega kattuma.

## Väljund

**Response (200 OK):** `CurrentUserDto` sisaldab sessiooni kasutaja andmeid ja infot profiili olemasolu kohta.

```json
{
  "userId": 1,
  "firstName": "Marko",
  "lastName": "Tamm",
  "roleName": "admin",
  "email": "email@Gmail.com",
  "hasProfile": true
}
```

Näide vastab `3_import.sql` kasutajale 1 ja tema profiilile. Demokasutaja `google_sub = demo-marko-tamm` ei ole päris Google identifikaator: näide sobib testitud principal'iga, mitte demokontoga päris Google sisselogimise tõendiks.

| Väli | Java tüüp | Allikas |
|---|---|---|
| userId | Integer | Sessiooni principal'iga seotud `app_user.id` |
| firstName | String | `app_user.first_name` |
| lastName | String | `app_user.last_name` |
| roleName | String | `role.role_name` kasutaja rolli kaudu |
| email | String | Profiili olemasolul `profile.email`, muidu Google principal'i email profiilivormi eeltäitmiseks |
| hasProfile | boolean | Kas `profile.user_id` vastab kasutajale |

Profiilita kasutaja puhul on `hasProfile: false`; teised väljad tulevad tegelikult autenditud kasutajalt. Import ei sisalda profiilita kasutaja näidet, seega selle jaoks ei esitata väljamõeldud identiteeti. `google_sub`, Google tokenid ja client secret vastusesse ei kuulu.

Õnnestunud OAuth autentimine loob serveri sessiooni (`JSESSIONID`) ja suunab brauseri frontendi `/` rajale. Ebaõnnestumine suunab `/?loginError`. `POST /logout` lõpetab sessiooni ja suunab frontendi `/` rajale (302, JSON body puudub); frontend kasutab vormipõhist navigatsiooni. Need sihid pärinevad juhendist; 302 ja tühi body on taski lepingu täpsustused.

## Eesmärk

GoogleLoginView võimaldab Google kontoga sisse logida ning `GET /api/me` annab SPA-le autentimisoleku. Profiilita kasutaja suunatakse frontendis profiili täitma; profiiliga kasutaja saab jätkata tavavaates. Kasutaja loomine ei tähenda profiili automaatset loomist.

Teostuse reeglid:

1. Kasuta Spring Security OIDC voogu ja tokenite kontrolli; seo konto kontrollitud `sub` väärtusega, mitte e-postiga. Leia `app_user` `google_sub` järgi.
2. Kui kasutajat pole, loo `customer` rolliga (impordis ID 2), staatusega `A` kasutaja. Eesnimi tuleb `given_name`, perekonnanimi `family_name`; puuduva perekonnanime korral kasuta juhendi järgi tühja stringi. Puuduva eesnime või skeemi pikkuspiiranguid ületavate väärtuste korral katkesta autentimine üldise loginError tulemusega, ära salvesta vigast ega kärbitud identiteeti (tehniline täpsustus).
3. Olemasoleva kasutaja roll säilib; `status = B` korral uut autenditud sessiooni ei looda. Korduv ja samaaegne esmasisselogimine ei tohi luua sama `sub` jaoks mitut kasutajat.
4. Hoia principal'is sisemist `userId` väärtust ning kasuta seda `/api/me` päringus. Anna õigused andmebaasi rolli järgi. Olemasoleva nime või profiili andmete ülekirjutamist see task ei nõua.
5. Ära loo `profile` kirjet: Google ei anna kohustuslikku telefoni ega asukohta. Profiili puudumine ei tohi põhjustada `/api/me` viga.
6. Hoia sessioonipõhise rakenduse CSRF-kaitse alles. Rakenda samal originil SPA-le loetava CSRF küpsise ja vastava päise/vormivälja leping. Erinevalt juhendi lihtsustatud `csrf.disable()` näitest ei lülitata kaitset välja. Frontend peab saama värske tokeni enne logout POST-i; täpne väljastamise viis ja tokeni nimed tuleb mõlemas kihis koos määrata enne teostust.
7. Lisa vajalik OAuth client sõltuvus ja konfiguratsioon, säilitades avalike kategooria- ja tööriistapäringute ligipääsu. Profiili loomine, tööriista lisamine ja nende ärireeglid ei kuulu sellesse taski.

**Allikate erinevus:** HomeView märkmetes olev `POST /auth/google` ei vasta GoogleLoginView lehekülje ja Google juhendi voole. Selle taski alus on viimane: OAuth navigatsioon + sessioon + `/api/me`. Paralleelset `POST /auth/google` endpoint'i ei looda; HomeView vana märge vajab eraldi kooskõlastamist.

## Seotud andmebaasi tabelid

Vt [2_create.sql](../../../database/2_create.sql).

### `role`

```sql
CREATE TABLE role (
    id serial PRIMARY KEY,
    role_name varchar(20) NOT NULL UNIQUE
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

`app_user.role_id` seob kasutaja rolliga; `google_sub` on kohustuslik ja unikaalne. `profile.user_id` on kohustuslik unikaalne viide kasutajale, kuid kasutajal võib profiil puududa. Profiili `location_id` viitab `location` tabelile; selle päringu jaoks asukohaandmeid ega teisi asukohatabeleid ei loeta.

[3_import.sql](../../../database/3_import.sql) seotud näidisandmed:

| app_user.id | Nimi | role_id / role_name | google_sub | status | profile.id | profile.email |
|---|---|---|---|---|---|---|
| 1 | Marko Tamm | 1 / admin | demo-marko-tamm | A | 1 | email@Gmail.com |
| 3 | Liis Kask | 2 / customer | demo-liis-kask | A | 3 | liis.kask@example.com |

Päris Google konto esmasisselogimine loob oma kontrollitud `sub` väärtusega kasutaja. Demoandmete kustutamine ega ümberseostamine ei kuulu taski.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `GET /api/me`: sessioon puudub või on aegunud. | 401 Unauthorized | Tühi body; Google'i lehele ei suunata. |
| `GET /api/me`: ootamatu andmebaasitõrge. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |
| OAuth autentimine ebaõnnestub või kasutaja on blokeeritud. | 302 Found | Body puudub; `Location: /?loginError` frontendi originil. |
| `POST /logout`: CSRF token puudub või on vigane. | 403 Forbidden | Body pole selle taskiga määratud; klient käsitleb staatust. |

401 ja loginError käitumine tulevad juhendist. 500 JSON on taski tehniline täpsustus olemasoleva `ApiError` stringväljadega; praegune `RestExceptionHandler` seda veel ei taga. Kliendile ei tagastata SQL-i, stack trace'i ega Google tokenit. CSRF tokeni aegumisel peab uus katse kasutama värsket tokenit.

## Vastuvõtu kriteeriumid

- [ ] Google nupp saab alustada OAuth voogu `/oauth2/authorization/google` kaudu; callback kontrollitakse Spring Security poolt.
- [ ] Uus konto seotakse `sub` järgi, luuakse aktiivse customer'ina ilma profiilita; korduv/samaaegne login ei dubleeri kontot.
- [ ] Olemasolev roll säilib ja blokeeritud kontole sessiooni ei looda.
- [ ] `/api/me` kasutab sessiooni principal'i ning tagastab täpselt kuus kirjeldatud välja, õige rolli ja e-posti allika.
- [ ] Profiilita kasutaja saab 200 ning `hasProfile: false`; autentimata päring saab 401 ilma Google redirect'ita.
- [ ] OAuth õnnestumine, ebaõnnestumine ja logout järgivad kirjeldatud suunamisi.
- [ ] Logout lõpetab sessiooni; pärast seda annab `/api/me` 401. CSRF leping on frontendiga kooskõlas ja kehtetu tokeniga POST lükatakse tagasi.
- [ ] Automaattestid katavad uue ja olemasoleva kasutaja, blokeeritud konto, vigase autentimistulemuse, korduva loomise, profiiliga/profiilita vastuse, 401, 500 ning logout/CSRF käitumise. Testid ei vaja päris Google kontot.
- [ ] Eraldi integratsioonikontroll päris seadistatud testkontoga kinnitab OAuth redirect'i, callback'i ja sessiooniküpsise töö; seda ei asenda mock-testide läbimine.
