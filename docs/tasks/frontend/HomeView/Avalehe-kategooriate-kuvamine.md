# Avalehe kategooriate kuvamine

**Vaade:** `HomeView.vue`, route `/`

**Roll:** Kõik rollid (Admin, Customer, Külastaja)

**Vaste balsamic mockupis:** „Laenukas.pdf“, HomeView.vue, lehekülg 1/17 (vt lisatud pilt `Avalehe-kategooriate-kuvamine.png`). Pilt on võetud kasutaja juhisel PDF-i esimeselt leheküljelt.

![Mockup](./Avalehe-kategooriate-kuvamine.png)

Tekstilised lähteandmed: [HomeView-markmed.md](../../../balsamic/notes/HomeView-markmed.md). PDF-i esimese lehekülje märkmed ja eraldi märkmefail kirjeldavad sama käitumist.

## Kasutajavoog

Kasutaja avab avalehe ning näeb teenuse tutvustust ja kategooriakaarte koos piltide, nimede ja kirjeldustega. Sisse logimata külastaja saab avada Google sisselogimise modaali nupust „Logi sisse / Registreeru“ või kategooriakaardile vajutades. Sisse loginud kasutaja suunatakse kategooriakaardilt otsinguvaatesse valitud kategooria query-parameetriga, näiteks `/tools?categoryId=1`. Task hõlmab kategooriate laadimist, avalehe kujundust ja autentimise modaali avamise ühenduskohta; Google autentimise API teostus, teiste vaadete funktsionaalsus ning „Kuidas see töötab“ plokkide täpsustamata sisu jäävad eraldi ülesanneteks.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---------|------|--------------------|
| Logo | Päise pilt | Mockupil päise vasakus servas. Lõplik logo ja selle võimalik lingisiht vajavad täpsustamist. |
| Avaleht | Navigatsioonilink | Suunab rajale `/`. |
| Otsi tööriistu | Navigatsioonilink | Otsinguvaade `/tools`; selle raja loomine on sõltuvus. Külastaja jaoks lingile vajutamise reeglit avalehe märkmed eraldi ei täpsusta. |
| Minu tööriistad, Sõnumid, Profiil | Navigatsioonilingid | Säilita mockupil näidatud kohad päises; sihtrajad ja rollipõhine käitumine tuleb täpsustada vastavate vaadete ülesannetes. |
| Logi sisse / Registreeru | Nupp | Nähtav ainult sisse logimata külastajale. Avab Google sisselogimise modaali. Kasuta märkmefaili korrektset kirjapilti. |
| Laena tööriistu naabritelt | Pealkiri | Avalehe tutvustuse pealkiri. |
| Tutvustustekst | Tekst | „Meie kogukonna kõige parem kraami jagamise pleiss. Tööriistade sirvimiseks ja lisamiseks pead olema sisse logitud“. |
| Saadaolevad tööriistad | Jaotise pealkiri | Selle all kuvatakse kategooriaid, mitte üksikuid tööriistu. |
| Kategooriakaardid | Klikitavad kaardid | Iga API vastuse element annab ühe kaardi: `imageData`, `categoryName`, `description`. Kaardi valik edastab `categoryId`. |
| Kaartide paigutus | Kaardivõrgustik | Mockupil kolm kaarti esimeses reas ja neljas keskel järgmises reas. Kaartide arv sõltub vastusest; kitsal ekraanil vähenda veergude arvu. |
| Kuidas see töötab | Pealkiri ja kolm sisuplokki | Mockupil kolm tühja ristkülikut. Säilita jaotise paigutus; tekstide, piltide ega videote sisu pole määratud. |
| Google sisselogimise modaal | Modaal | Avatakse kirjeldatud tegevustest; modaali sisemine kujundus ja autentimisprotsess ei ole selle lehekülje API märkmetes kirjeldatud. |
| Laadimis-, vea- ja tühja loendi olek | Olekuteated | Rakendamisel vajalikud olekud kategooriate piirkonnas; mockup ei määra nende täpset sõnastust. |

Mockupi näidiskaardid on „Aiatööd“, „Ehitustööd“, „Koristamine“ ja „Muud asjad“. Neid ei tohi tootmiskoodis fikseeritud andmetena kasutada. Brauseri raam, aadressiriba, märkmelehed ja Balsamiqi vesimärk ei kuulu rakenduse kasutajaliidesesse.

## Käitumine ja valideerimine

1. Vaate avamisel laadi kategooriad `beforeMount` kaudu. Kategooriate päring ei vaja kasutaja sisestust ega vormivalideerimist ning avalehe kategooriad peavad olema nähtavad ka külastajale.
2. Laadimise ajal kuva laadimisolek. Eduka vastuse korral kuva kaardid API tagastatud järjekorras: backend järjestab kategooriad `sequence` järgi kasvavalt. `sequence` ei kuulu vastuse DTO-sse ning frontend seda ei arvuta.
3. Seo kaardid `categoryId` kaudu. Kategooria nimi ja kirjeldus kuvatakse tekstina; pildile anna kategooria nimega alternatiivtekst. Kogu kaardi valimine peab olema võimalik ka klaviatuuriga.
4. `200` ja tühja massiivi korral kuva tühja loendi olek. Võrguvea või ebaõnnestunud HTTP vastuse korral kuva laadimise veaolek; ära esita viga tühja kategoorialoendina. Lõpeta laadimisolek `.finally()` kaudu ka vea korral. Teadete täpne sõnastus täpsustada enne implementeerimist.
5. Pildi puudumine või laadimistõrge ei tohi takistada kategooria nime, kirjelduse ega kaardi valiku kasutamist. `imageData` näites on Base64 sisu, kuid MIME-tüüp ja data-URL prefiksi olemasolu pole määratud: täpsusta pildi formaat backend taskis enne pildisidumise teostamist, ära eelda kindlat pildivormingut.
6. Kuva „Logi sisse / Registreeru“ ainult autentimata kasutajale ning ava vajutamisel ühine Google sisselogimise modaal.
7. Kontrolli kategooria valimisel autentimisolekut. Autentimata külastajale ava sama modaal; ära suuna teda kohe otsinguvaatesse. Autenditud kasutaja suuna rajale `/tools` query-parameetriga `categoryId`, mille väärtus tuleb valitud kaardilt, mitte kuvatavast nimest.
8. Google autentimise request/response, sessiooni tehniline kontroll ja võimalik automaatne suunamine pärast sisselogimist ei ole selle taski lähteandmetes määratud. Need tuleb lahendada autentimise ülesandes; siin ei lisata oletuslikku sessiooni API kutset ega automaatset suunamist.

## API kutsed

### `GET /api/categories/detailed-info`

**Backend task:** puudub — kontrakt tuletatud otse PDF-i API märkmetest ja `HomeView-markmed.md` failist. Kontrollitud backendis pole selle teenuse Controller/DTO realisatsiooni. Loo vastav backend task enne frontend-integratsiooni või paralleelselt `skill-loo-backend-task` abil.

Olemasolev `docs/tasks/backend/ToolsView/Kategooriate-nimekirja-paring.md` kirjeldab teist teenust (`GET /api/categories`), mis annab otsingufiltri lihtloendi. See ei asenda avalehe detailsete kategooriate kontrakti.

**Request:** body puudub; path- ja query-parameetreid pole kirjeldatud.

`CategoryDetailedInfoDto.java` — response (`200`, JSON massiiv):

```json
[
  {
    "categoryId": 1,
    "categoryName": "Aiatööd",
    "description": "Muruniidukid, labidad, rehad",
    "imageData": "BASE64-image-data"
  }
]
```

Näide sisaldab ühte elementi; tegelik vastus sisaldab kõiki kategooriaid. Märkmed määravad väljade nimed ja näidisväärtused, mitte Java väljade täpsed tüübid, nullitavuse ega pikkuspiirangud. `categoryId` on näites arv ning ülejäänud väljad stringid. Backend tagastab kirjed `sequence` järgi kasvavalt sordituna.

**Veateated:** mockupi väärtus on „—“: teenusepõhiseid veakoode ja sõnumeid pole kirjeldatud.

| Status code | errorCode | message | Frontend käitumine |
|-------------|-----------|---------|--------------------|
| Ebaõnnestunud HTTP vastus, täpne kood määramata | Määramata | Määramata | Lõpeta laadimine ning kuva kategooriate laadimise veaolek. |
| HTTP vastus puudub, nt võrguviga | Puudub | Puudub | Sama veaolek; veakäsitlus ei tohi eeldada `error.response` olemasolu. |

Olemasolev ühine `ApiError` sisaldab stringvälju `message` ja `errorCode`, kuid see ei määra veel selle implementeerimata teenuse veavastuseid. Ära lisa oletuslikke `404` või muid ärivigu.

Märkmetes nimetatud `POST /auth/google` kohta pole sellel leheküljel eraldi API märkmeid. Selle request/response ja vead kuuluvad autentimise ülesandesse, mitte käesoleva taski API-kontrakti.

## Komponendid ja failistruktuur

Järgi `docs/frontend/projekti-struktuur.md` ja `docs/frontend/vue-komponendi-struktuur.md` konventsioone. Alltoodud uued failinimed on ettepanekud.

| Fail | Ülesanne ja olemasolev seis |
|------|----------------------------|
| `frontend/src/views/HomeView.vue` | Olemas platsihoidjana, sisaldab teavitust ja `TestComponent` komponenti. Asenda platsihoidja avalehe sisuga; koonda siia laadimine ja kategooria valiku käsitlemine. |
| `frontend/src/components/common/CategoryCard.vue` | Uus kategooriakaart, mis saab kategooria andmed props kaudu ning väljastab `event-category-selected` sündmuse koos `categoryId` väärtusega. |
| `frontend/src/api-services/CategoryService.js` | Uus Axios teenus detailsete kategooriate päringuks; näiteks `sendGetCategoriesDetailedInfoRequest()`. HTTP päring ei kuulu kaardikomponenti. |
| `frontend/src/navigation/NavigationService.js` | Uus navigatsiooni abiteenus otsinguvaatesse suunamiseks koos kategooria query-parameetriga. |
| `frontend/src/navigation/AppNavigation.vue` | Ühise päise võimalik komponent; kooskõlasta teiste vaadete navigatsiooniga. |
| `frontend/src/components/modals/GoogleLoginModal.vue` | Autentimise ülesande sõltuvus: avaleht avab selle, autentimise sisemine teostus luuakse eraldi. |
| `frontend/src/auth/` | Autentimisoleku kontrolli kavandatud asukoht; vastav teostus praegu puudub. |
| `frontend/src/router/index.js` | `/` on olemas nimega `homeRoute` ja kasutab `HomeView`. `/tools` puudub: kategoorialt suunamise toimimiseks peab otsinguvaate ülesanne lisama vastava raja. |

Kasuta Options API-t järjekorras `name`, `components`, `props`, `emits`, `data`, `computed`, `methods`, `beforeMount` vastavalt vajadusele. API kutsed kasuta `.then()` / `.catch()` / `.finally()` ahelana ning vastuse ja vea töötlemiseks eraldi `handle...` meetodeid. Kaardiloendi algväärtus on tühi massiiv; laadimise olek ja veateade on eraldi andmeväljad.

## Vastuvõtu kriteeriumid

- [ ] Avaleht on rajal `/` nähtav Adminile, Customerile ja külastajale.
- [ ] Platsihoidja asemel kuvatakse mockupi päis, tutvustus, kategooriate jaotis ning „Kuidas see töötab“ jaotise paigutus; täpsustamata sisu ei mõelda välja.
- [ ] Kategooriad laaditakse `beforeMount` kaudu `GET /api/categories/detailed-info` teenusest ka sisse logimata külastajale.
- [ ] Iga vastuse element annab kaardi oma nime, kirjelduse ja pildiga; järjekord vastab API vastusele ning kaartide arv pole fikseeritud neljaks.
- [ ] Pildiformaat on backendiga täpsustatud ning puuduv või vigane pilt ei takista kaardi kasutamist.
- [ ] Laadimise, tühja massiivi, HTTP vea ja vastuseta võrguvea olekud on eristatavad ning laadimine lõpeb ka vea korral.
- [ ] „Logi sisse / Registreeru“ on nähtav ainult külastajale ja avab Google sisselogimise modaali.
- [ ] Autentimata kasutaja kategooriavalik avab sama modaali ega suuna kohe otsinguvaatesse.
- [ ] Autenditud kasutaja kategooriavalik suunab `/tools?categoryId=<valitud ID>`; `/tools` ja autentimise sõltuvused on selle kontrollimiseks olemas.
- [ ] Kategooriakaardid on kasutatavad klaviatuuriga ja piltidel on alternatiivtekst.
- [ ] API-kutsed, komponentide sündmused ja Options API struktuur järgivad projekti konventsioone.
- [ ] Backend-kontrakti, autentimise, navigatsiooni ja täpsustamata infoplokkide lahtised detailid on enne vastava osa implementeerimist kooskõlastatud.
