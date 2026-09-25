# Päise ja rollipõhise navigatsiooni kuvamine

**Vaade:** `Header.vue`, ühine komponent; oma route puudub.

**Roll:** külastaja, Customer ja Admin.

**Vaste balsamic mockupis:** Header.vue, lk 14/17 failis [Laenukas2509.pdf](../../../balsamic/notes/Laenukas2509.pdf#page=14).

![Mockup](./Paise-ja-rollipohise-navigatsiooni-kuvamine.png)

Alus: [Header märkmed](../../../balsamic/notes/Header-markmed.md), [Header BE-task](../../backend/Header/Sisselogitud-kasutaja-paring.md) ja [GoogleLogin FE-task](../GoogleLoginView/Google-kontoga-sisselogimine.md). Märkmetes kinnitatud nähtavusreeglid täpsustavad staatilist maketti.

## Kasutajavoog

Kõigi vaadete kohal kuvatakse sama päis. Rakenduse alguses laetakse ühise sessioonikontrolliga kasutaja; külastaja saab avalikke lehti avada või Google'iga sisse logida, sisseloginud kasutaja oma tööriistu ja profiili avada. Adminile lisandub Admin-link. Profiilita kasutaja suunatakse `/profile`; väljalogimine lõpetab sessiooni ja viib avalehele.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Logo | RouterLink | Kõigile, `/`. |
| Avaleht | RouterLink | Kõigile, `/`. |
| Otsi tööriistu | RouterLink | Kõigile, `/tools`. |
| Minu tööriistad | RouterLink | Ainult sisseloginud kasutajale, `/my-tools`. |
| Profiil | RouterLink | Ainult sisseloginud kasutajale, `/profile`. |
| Admin | RouterLink | Ainult `roleName === 'admin'`, `/admin`. |
| Logi sisse / Registreeru | Tavaline HTML-link | Ainult külastajale, `/oauth2/authorization/google`; täielik brauseri ümbersuunamine. |
| Logi välja | POST vormi nupp | Ainult sisseloginud kasutajale; `/logout`, seejärel `/`. |
| Laadimisolek | Olek | Kuni sessioon pole teada, ära kuva kasutaja õigusi eeldavaid linke. |
| Veateade / Proovi uuesti | Tehniline olek | GET 500/võrguvea korral näita viga ja võimalda uut sessioonikontrolli. |

„Sõnumid“ link puudub kinnitatud Headeri lepingust. Eraldi registreerimislinki ega registreerimisvormi ei looda.

## Käitumine ja valideerimine

1. Paiguta Header `App.vue` sisse RouterView kohale, asendades toorikprojekti Home/Test navigatsiooni. Päist ei kopeerita igasse vaatesse.
2. Kasuta ühist `auth` kasutajaolekut ja ühte alglaadimise `/api/me` päringut. Header ja GoogleLogin ei käivita kumbki eraldi sama algpäringut; ühine in-flight promise väldib dubleerimist. Options API `beforeMount` käivitab vajadusel jagatud kontrolli.
3. GET 200 salvestab kõik kuus kasutajavälja. `roleName` määrab Admin-lingi nähtavuse. `hasProfile === false` suunab `/profile`, välja arvatud juhul, kui juba ollakse seal; suunamistsüklit ei teki. Google e-post jääb ühisesse olekusse profiili eeltäitmiseks.
4. GET 401 tühjendab kasutajaoleku ja kuvab külastaja päise ilma veateateta. GET 500/võrguviga tähendab teadmata sessiooni, mitte kinnitatud külalist; ära suuna profiilile ega näita Admin-linki. Tehniline fallback: „Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti.“
5. Sisselogimise link on `<a href="/oauth2/authorization/google">`; ära kasuta axios't ega uut Headeri login-modaali. GoogleLogin taski eraldi modaali ei teostata siin uuesti.
6. Väljalogimiseks taaskasuta GoogleLogin taski tavalist POST vormi koos selle ühise CSRF tokeniga. Edukas redirect laeb `/` uuesti ja puhastab mälus oleva kasutajaoleku; uus GET 401 kinnitab väljalogimise. Vea korral ei märgita serveri sessiooni lõppenuks. See task ei defineeri uut CSRF tokeni endpoint'i.
7. Profiili salvestamise järel uuenda ühist kasutajaolekut (sh hasProfile) MyProfile vooga koos, et päis ei suunaks juba täidetud profiili uuesti täitma. Ära lisa selleks iga navigeerimisega automaatset GET päringut.
8. Linkide peitmine ei asenda route'i/API autoriseerimist. Kasuta RouterLink'i siselinkide jaoks ja märgi aktiivne siht; lingid ja nupud peavad töötama klaviatuuriga. Kitsal ekraanil ei tohi navigeerimine kaduda ega kattuda; täpne mobiilimenüü kujundus on teostuse detail.

## API kutsed

Kontrollitud backendis pole veel `/api/me` controllerit ega security teostust. Leping tuleb olemasolevatest BE-taskidest. Päringud kasutavad sama origin'i sessiooniküpsist; kasutaja ID-d, Google tokenit ega saladusi ei saadeta Headerist body/query kaudu.

### `GET /api/me`

**Backend task:** [Sisselogitud kasutaja päring](../../backend/Header/Sisselogitud-kasutaja-paring.md).

Sisendid, query, path variable'id ja request body puuduvad. `CurrentUserDto` — response 200:

**Customer profiiliga:**

```json
{
  "userId": 3,
  "firstName": "Liis",
  "lastName": "Kask",
  "roleName": "customer",
  "email": "liis.kask@example.com",
  "hasProfile": true
}
```

**Admin profiiliga:**

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

**Kasutaja profiilita:**

```json
{
  "userId": 583,
  "firstName": "Mari",
  "lastName": "Maasikas",
  "roleName": "customer",
  "email": "user@gmail.com",
  "hasProfile": false
}
```

`userId` on Integer, nimed/roll/e-post String ning `hasProfile` boolean. E-post tuleb profiilist või profiili puudumisel Google sessioonist. `lastName` võib olla tühi. 583/Mari näide on BE-taski PDF-näide, mitte importandmete kasutaja.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 401 | — | Tühi body | Külastaja; veateadet ei kuvata. |
| 500 | INTERNAL_SERVER_ERROR | Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti. | Kuva message ja võimalda korduspäringut; õigusi ei eeldata. |

500 body:

```json
{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti."}
```

### `GET /oauth2/authorization/google` — brauseri navigeerimine

**Backend task:** [Google kontoga sisselogimine](../../backend/GoogleLoginView/Google-kontoga-sisselogimine.md).

Request body puudub. See pole JSON API: Spring Security suunab Google'i ja käsitleb callback'i, seejärel naaseb rakendusse. Header ei loe protokolli response JSON-i. Õnnestumise järel kasutatakse taas ühist `/api/me` kontrolli; OAuth veavoog kuulub GoogleLogin taski.

### `POST /logout`

**Backend task:** sama GoogleLogin BE-task; endpoint'i haldab Spring Security.

Tavaline HTML POST vorm, JSON request puudub; turvaseadistuse nõutud CSRF token on vormiväli. Eduka vastuse tulemus on redirect avalehele, mitte CurrentUserDto ega eeldatav JSON 200.

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 403 (vigane/puuduv CSRF token) | Pole määratud | Pole määratud | Ära eelda logout'i õnnestumist; kasuta ühise autentimistöö veakäsitlust. |
| Võrgu-/serveritõrge | Pole määratud | Pole määratud | Ära märgi sessiooni lõppenuks; paku uut katset ühises logout-voos. |

Täislehe vormi veavastuse kuvamise eest vastutab brauser/ühine autentimisvoog; Header ei püüa seda Axios `.catch()` kaudu. `googlega_login.md` lihtsustatud CSRF-väljalülitamise näite asemel kasutatakse olemasoleva GoogleLogin taski CSRF lepingut.

## Komponendid ja failistruktuur

| Fail | Vastutus |
|---|---|
| `frontend/src/navigation/Header.vue` | Päis ja linkide tingimuslik kuvamine; Options API. |
| `frontend/src/App.vue` | Üks Header RouterView kohal. |
| `frontend/src/auth/` | GoogleLogin taskiga ühine reaktiivne kasutajaolek ja sessioonikontroll; ära loo paralleelset store'i. |
| `frontend/src/api-services/AuthService.js` | Jagatud GET `/api/me` Axios meetod. |
| `frontend/src/navigation/` | Olemasoleva/planeeritud ühise profiilile suunamise ja logout-voo taaskasutus. |
| `frontend/src/router/index.js` | Sihtvaadete rajad ja nende oma õiguskontroll. Headeril oma rada pole. |
| `frontend/vite.config.js` | OAuth taski `/api`, `/oauth2`, `/login/oauth2`, `/logout` proxy-seadistuse taaskasutus. |

Hetkeseis: App.vue sisaldab toorikprojekti navigatsiooni, Header/auth/API teenused puuduvad. Router sisaldab ainult `/` ja `/test`; `/tools`, `/my-tools`, `/profile`, `/admin` tuleb ühendada vastavate vaadete töödega. Nende vaadete teostamine ei kuulu Headeri taski.

Järgi [projekti struktuuri](../../../frontend/projekti-struktuur.md) ja [Options API juhist](../../../frontend/vue-komponendi-struktuur.md): data/computed/methods/beforeMount, vajadusel `event-` emits, Axios `.then()`/`.catch()`/`.finally()` ja eraldi handle-meetodid. Tavalisele OAuth lingile ja logout vormile Axios mustrit ei rakendata.

## Vastuvõtu kriteeriumid

- [ ] Kõigil vaadetel on üks ühine Header; Home/Test tooriknavigatsioon on asendatud.
- [ ] Külastaja näeb logo, Avaleht, Otsi tööriistu ja Logi sisse / Registreeru.
- [ ] Customer näeb lisaks Minu tööriistad ja Profiil ning login-lingi asemel Logi välja; Admin-link puudub.
- [ ] Admin näeb ka Admin-linki; kõik sihtrajad vastavad tabelile.
- [ ] Sõnumid-linki ega eraldi registreerimisvoogu ei lisata.
- [ ] Alglaadimisel toimub üks jagatud GET; laadimise ajal ei vilgu admini/autenditud lingid.
- [ ] 401 on tavapärane külastajaolek; 500 ja võrguviga kuvatakse veana koos korduskatsega.
- [ ] Profiilita kasutaja suunatakse `/profile` ilma tsüklita; profiili salvestamine uuendab ühist olekut.
- [ ] Google login kasutab täielikku brauseri navigeerimist; logout POST lõpetab sessiooni ja viib avalehele.
- [ ] Logout'i tõrge ei tekita valelikku teadet sessiooni lõppemisest; CSRF-leping on GoogleLogin taskiga ühine.
- [ ] Rollipõhine nähtavus, loading/error olekud ja navigatsioon on kontrollitud mockitud vastustega; päris OAuth teostus on eraldi sõltuvus.
