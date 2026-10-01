# Tööriistade otsimine ja filtreerimine — implementatsiooni plaan

**Seotud task:** [Tooriistade-otsimine-ja-filtreerimine.md](./Tooriistade-otsimine-ja-filtreerimine.md)

**Vaade:** `ToolsView.vue`, route `/tools` (avalik)

**API:** `GET /api/tools`, `GET /api/categories`, `GET /api/cities`, `GET /api/cities/{cityId}/districts` — kõik avalikud.

## Hetkeseis (mis on juba olemas)

Taski tekst ütleb, et vaade, teenused ja `/tools` route puuduvad — see on aegunud. Koodibaasi tegelik seis:

**Backend (kõik neli endpointi on valmis):**

- `backend/.../controller/tool/ToosListController.java` — `GET /api/tools`; parameetrid võetakse String-ina, valideerimine `ToolService.getTools(...)` sees.
- `backend/.../controller/tool/dto/ToolsResponse.java` — `pageNumber`, `pageSize`, `totalPages`, `totalElements`, `tools`.
- `backend/.../controller/tool/dto/ToolListItemDto.java` — `toolId`, `toolName`, **`description`**, `imageData`, `status`, `cityName`, `districtName`.
- `backend/.../controller/category/CategoryController.java` — `GET /api/categories`.
- `backend/.../controller/city/CityController.java` — `GET /api/cities`.
- `backend/.../controller/district/DistrictController.java` — `GET /api/cities/{cityId}/districts`.
- **NB:** `ToolService.getTools` vaikimisi `pageSize` on **12**, mitte taskis kirjeldatud 6. **Otsus:** järgime backendi — 12 kirjet lehel (vt „Otsused“).

**Frontend:**

- `frontend/src/views/ToolsView.vue` — ainult kohatäitja („koostamisel“), loeb `$route.query.categoryId`. Asendatakse täielikult.
- `frontend/src/router/index.js` — `/tools` (`toolsRoute`) on olemas. `/tools/:toolId` (detailvaade) **puudub**.
- `frontend/src/api-services/ToolService.js` — `sendGetToolDetailsRequest`, `sendCreateToolRequest`; nimekirja päring puudub.
- `frontend/src/api-services/CategoryService.js` — `sendGetCategoriesRequest()` olemas (kasuta seda, mitte `detailed-info`).
- `frontend/src/api-services/CityService.js` — `sendGetCitiesRequest()` ja `sendGetCityDistrictsRequest(cityId)` olemas.
- `frontend/src/navigation/NavigationService.js` — `navigateToTools(router, categoryId)` (HomeView kasutab) ja `navigateToToolDetail(router, toolId)` olemas.
- `frontend/src/navigation/AppHeader.vue` — „Otsi tööriistu“ link `/tools` olemas.
- `frontend/src/components/forms/CitiesDropdown.vue`, `DistrictsDropdown.vue` — korduvkasutatavad rippmenüüd (`event-new-city-selected`, `event-new-district-selected`, `isDisabled`, `firstOptionLabel`). Kasutusel MyProfile ja AddToolView vaates.
- `frontend/src/components/common/AlertDanger.vue` — olemas.
- `frontend/src/components/common/MyToolCard.vue` — sarnane kaart (SVG data-URL, kohatäitja, `@error`), aga ilma kirjelduseta ja objekti emititega. Eeskujuks, **mitte muutmiseks**.
- Võrguvea muster: `MyProfile.vue` / `MyToolsView.vue` `handleApiError` — `if (!error.response)` → `NETWORK_ERROR_MESSAGE`.

## Merge-konfliktide analüüs (teised harud)

- `git fetch` järel: `origin/ANDRUS-FE43` on masterist 1 commit ees, muudab ainult `.claude/skills/uus-projektigen/SKILL.md`. Ükski teine haru (`ANDRUS-BE3/4/5`, `JAROSLAV-AI-MODULE`, `KERSTI`, `KERSTI-BE-36-CITIES`, `KERSTI-BE-38-TOOLS-katki`) ei ole masterist ees → frontend failides konflikti hetkel ei ole.
- `KERSTI-FE-45` on `origin/master`-ist **6 commitit maas** (ainult backend: admini kategooriad, broneeringud). Frontend faile master ei muuda. Enne alustamist: `git merge origin/master`.
- **Tööpuus on 169 faili muudetud ainult reavahetuste (CRLF/LF) tõttu** (`git diff --ignore-cr-at-eol` on tühi). Põhjus: Windowsi pool võtab failid välja CRLF-iga, WSL-i gitil pole `core.autocrlf` seadistatud. Neist 7 faili (`BookingController`, `BookingService`, `BookingMapper`, `BookingRepository` + testid) muudab ka master → `git merge` keeldub. Neid **ei tohi commitida**. Lahendus (kasutaja teeb ise): `git config core.autocrlf true`, `git status`, vajadusel `git checkout -- .`, seejärel `git merge origin/master`.
- Jagatud failid, mida see task puudutab: `ToolService.js`, `NavigationService.js`. Lisa uued meetodid **faili lõppu** ega muuda olemasolevaid → minimaalne konfliktirisk.
- Mitte puudutada: `router/index.js` (`/tools/:toolId` kuulub ToolDetailView taskile), `MyToolCard.vue`, `CitiesDropdown.vue`, `DistrictsDropdown.vue`, `HomeView.vue`.
- Kõik ülejäänud failid on uued (`ToolsFilterForm.vue`, `CategoriesDropdown.vue`, `ToolCard.vue`, `ToolsPagination.vue`).

## Puuduv/muudetav

| Fail | Tegevus |
|---|---|
| `frontend/src/api-services/ToolService.js` | Lisa `sendGetToolsRequest(params)`. |
| `frontend/src/navigation/NavigationService.js` | Lisa `navigateToToolsSearch(router, query)`. |
| `frontend/src/components/forms/CategoriesDropdown.vue` | Uus, sama muster nagu `CitiesDropdown`. |
| `frontend/src/components/forms/ToolsFilterForm.vue` | Uus: kolm rippmenüüd + Rakenda / Tühista filtrid. |
| `frontend/src/components/common/ToolCard.vue` | Uus: pilt, nimi, kirjeldus, „Vaata detaile“. |
| `frontend/src/components/common/ToolsPagination.vue` | Uus: Eelmine, lehenumbrid, Järgmine. |
| `frontend/src/views/ToolsView.vue` | Asenda kohatäitja täisvaatega. |

## Arhitektuurne põhiotsus: URL on rakendatud filtrite allikas

Rakendatud filtrid (`categoryId`, `cityId`, `districtId`, `pageNumber`) elavad **route query's**. Vorm hoiab eraldi pooleliolevaid valikuid (`formFilters`).

- „Rakenda“, „Tühista filtrid“ ja lehevahetus **ainult muudavad URL-i** (`NavigationService.navigateToToolsSearch`).
- Tööriistapäringu teeb **üks koht**: `loadToolsFromRoute()`, mida kutsutakse `beforeMount`-is ja `$route.query` watcheris.
- Nii töötavad HomeView link, brauseri tagasi/edasi ja otselink ühtemoodi ning topeltpäringuid ei teki (vastab taski p 6).
- Erand: kui uus query on identne praegusega (nt „Tühista“ juba vaikeolekus või korduskatse), siis router ei navigeeri → kutsu `loadToolsFromRoute()` otse.
- `status` ja `pageSize` jäetakse päringust välja — kehtivad backendi vaikeväärtused (`A`, `12`). URL-i neid ei kirjutata. Paginatsioon kasutab vastuse `pageSize`/`totalPages` väärtusi, seega frontendis lehe suuruse konstanti pole.

## Sammud

### 1. API teenus — `frontend/src/api-services/ToolService.js`

Lisa faili lõppu:

```js
  sendGetToolsRequest(params) {
    return axios.get('/api/tools', { params })
  },
```

`params` = `{ categoryId, cityId, districtId, pageNumber }` — alati arvud, mitte tühjad stringid. `status` ja `pageSize` jäetakse välja (backendi vaikeväärtused `A` ja `12`).

### 2. Navigatsioon — `frontend/src/navigation/NavigationService.js`

Lisa faili lõppu:

```js
  navigateToToolsSearch(router, query) {
    return router.push({ path: '/tools', query })
  },
```

Vaikeväärtusega (`0`, `pageNumber=1`) väljad jäetakse query'st välja → „Tühista filtrid“ annab puhta `/tools` URL-i ja HomeView `categoryId` ei taastu (taski p 5). Olemasolevat `navigateToTools` ei muudeta (HomeView kasutab).

### 3. `frontend/src/components/forms/CategoriesDropdown.vue` (uus)

Täpselt `CitiesDropdown.vue` mustris: props `categories`, `selectedCategoryId`, `firstOptionLabel` (vaikimisi „Vali kategooria“), emit `event-new-category-selected` (Number). Kui `selectedCategoryId > 0` ja seda valikutes pole, lisa keelatud valik „Tundmatu kategooria (ID x)“, et olematu kategooria jääks nähtavaks (taski p 7).

### 4. `frontend/src/components/forms/ToolsFilterForm.vue` (uus)

- **props:** `categories`, `cities`, `districts`, `selectedCategoryId`, `selectedCityId`, `selectedDistrictId`, `isDistrictsLoading`, `isDisabled`.
- **emits:** `event-category-changed`, `event-city-changed`, `event-district-changed`, `event-apply-filters`, `event-reset-filters`.
- Kasutab `CategoriesDropdown`, `CitiesDropdown`, `DistrictsDropdown` (viimane `:is-disabled="selectedCityId === 0 || isDistrictsLoading"`).
- Pealkiri „Filtreeri“, `<label>` iga rippmenüü juures, kaks nuppu: „Rakenda“ (primary) ja „Tühista filtrid“ (outline).
- Komponent ei tee API päringuid ega hoia olekut — kõik valikud tulevad propsidest (vaade on olekuomanik).

### 5. `frontend/src/components/common/ToolCard.vue` (uus)

- **props:** `tool` (Object, required). **emits:** `event-view-details` (saadab `tool.toolId`).
- Pilt: `data:image/svg+xml;base64,${imageData}` nagu `MyToolCard`; `imageFailed` + `@error` → kohatäitja „Pilt puudub“. `alt` = `toolName`. **Ei kasuta `v-html`.**
- Nimi `toolName`, kirjeldus `description` ainult `v-if="tool.description"` (null ei kuvata).
- Hinda **ei kuvata**.
- Nupp „Vaata detaile“.
- Watch `tool.imageData` → `imageFailed = false` (kaardid taaskasutatakse lehevahetusel `:key="toolId"` korral harva, kuid ohutu).

### 6. `frontend/src/components/common/ToolsPagination.vue` (uus)

- **props:** `pageNumber`, `totalPages`, `isDisabled`. **emits:** `event-page-changed` (Number).
- Lehenumbrid `1..totalPages` (computed). Kui `totalPages > 7`, näita lühendatult (1 … n-1 n n+1 … last) — valikuline täiustus.
- Eelmine keelatud `pageNumber <= 1` või `totalPages === 0`; Järgmine keelatud `pageNumber >= totalPages` või `totalPages === 0`.
- `aria-current="page"` aktiivsel lehel, Bootstrapi `.pagination`.

### 7. `frontend/src/views/ToolsView.vue` (asendada)

**Konstandid:**

```js
const NETWORK_ERROR_MESSAGE = 'Serveriga ei saanud ühendust. Palun proovi hiljem uuesti.'
const MAX_INTEGER = 2147483647
```

**data():**

```js
{
  categories: [], cities: [], districts: [],
  isCategoriesLoading: false, isCitiesLoading: false, isDistrictsLoading: false, isToolsLoading: false,
  categoriesErrorMessage: '', citiesErrorMessage: '', districtsErrorMessage: '', toolsErrorMessage: '',
  urlErrorMessage: '',
  formFilters: { categoryId: 0, cityId: 0, districtId: 0 },
  appliedFilters: { categoryId: 0, cityId: 0, districtId: 0, pageNumber: 1 },
  toolsResponse: null,          // { pageNumber, pageSize, totalPages, totalElements, tools }
  isToolsResultStale: false,
  toolsRequestId: 0,
  districtsRequestId: 0,
}
```

**computed:** `hasTools`, `isEmptySearch` (`totalElements === 0`), `isPageBeyondLast` (`tools.length === 0 && totalElements > 0`).

**watch:** `'$route.query'` → `loadToolsFromRoute()` (ainult kui route on endiselt `toolsRoute`).

**methods (väljakutsumise järjekorras):**

1. `loadToolsFromRoute()`
   - `parseRouteQuery(this.$route.query)` → `{ filters, errorMessage }`.
   - Viga → `urlErrorMessage` (nt „Vigane URL-i parameeter: pageNumber“), tööriistapäringut **ei tehta**, kuvatakse „Lähtesta otsing“ nupp.
   - Õnnestus → `appliedFilters = filters`; `syncFormWithApplied()`; `getTools()`.
2. `parseRouteQuery(query)` — iga toetatud võti: puudu → vaikeväärtus; olemas → `/^\d+$/` ja `<= MAX_INTEGER`; `pageNumber >= 1`. Tühi string = viga.
3. `syncFormWithApplied()` — kopeeri `appliedFilters` vormi; kui `cityId` muutus, `getDistricts(cityId)` (ilma districtId nullimiseta, sest see tuleb URL-ist).
4. `getTools()` — `const requestId = ++this.toolsRequestId`; `isToolsLoading = true`; `ToolService.sendGetToolsRequest({ ...appliedFilters })`
   - `.then` → `if (requestId === this.toolsRequestId) handleGetToolsResponse(response.data)`
   - `.catch` → sama kontroll → `handleGetToolsError(error)`
   - `.finally` → `if (requestId === this.toolsRequestId) this.isToolsLoading = false`
5. `handleGetToolsResponse(data)` — `toolsResponse = data`, `toolsErrorMessage = ''`, `isToolsResultStale = false`.
6. `handleGetToolsError(error)` — `getErrorMessage(error)`; kui `toolsResponse` on olemas, `isToolsResultStale = true` (vana tulemus märgitakse aegunuks).
7. `getCategories()` / `handleGetCategoriesResponse` / `handleGetCategoriesError` — oma laadimis- ja veaolek, korduskatse nupp.
8. `getCities()` / `handleGetCitiesResponse` / `handleGetCitiesError` — sama.
9. `getDistricts(cityId)` — `const requestId = ++this.districtsRequestId`; `cityId === 0` → `districts = []`, päringut ei tehta. Vastus/viga rakendatakse ainult kui `requestId === this.districtsRequestId`. 404 → `districtsErrorMessage = message`, `districts = []`.
10. `handleCategoryChanged(id)` → `formFilters.categoryId = id`.
11. `handleCityChanged(id)` → `formFilters.cityId = id`, `formFilters.districtId = 0`, `districts = []`, `getDistricts(id)`.
12. `handleDistrictChanged(id)` → `formFilters.districtId = id`.
13. `handleApplyFilters()` → `navigateWithFilters({ ...formFilters, pageNumber: 1 })`.
14. `handleResetFilters()` → `formFilters` nulli, `districts = []`, `urlErrorMessage = ''`, `navigateWithFilters({ categoryId: 0, cityId: 0, districtId: 0, pageNumber: 1 })`.
15. `handlePageChanged(page)` → `navigateWithFilters({ ...appliedFilters, pageNumber: page })` (rakendatud, mitte vormi filtrid).
16. `navigateWithFilters(filters)` → `buildQuery(filters)` (jäta vaikeväärtused välja, väärtused Stringiks); kui query on sama mis `$route.query` → `loadToolsFromRoute()`, muidu `NavigationService.navigateToToolsSearch(this.$router, query)`.
17. `handleViewDetails(toolId)` → `NavigationService.navigateToToolDetail(this.$router, toolId)`.
18. `getErrorMessage(error, fallback)` → `!error.response` → `NETWORK_ERROR_MESSAGE`; muidu `error.response.data?.message ?? fallback`.

**beforeMount():** `getCategories()`, `getCities()`, `loadToolsFromRoute()` — sõltumatult paralleelselt; `/tools?categoryId=1` korral on esimene tööriistapäring juba filtriga (filtrita päringut ei tehta).

**Template:**

- `<h1>Otsing</h1>`; `row`: vasakul `col-lg-3` `ToolsFilterForm`, paremal `col-lg-9` tulemused.
- `AlertDanger` iga vea jaoks (URL, kategooriad, linnad, linnaosad, tööriistad) + „Proovi uuesti“ nupp vastavale päringule.
- Laadimine: `role="status"` tekst.
- Aegunud tulemus: `alert-warning` „Kuvatud tulemus võib olla aegunud“.
- Tühi otsing (`totalElements === 0`): „Otsingule vastavaid tööriistu ei leitud.“
- Üle viimase lehe: „Sellel lehel tööriistu pole.“ + nupp „Mine viimasele lehele“.
- Kaardid: CSS grid 3 veergu (12 kaarti = 4 rida), `≤991px` 2, `≤575px` 1 (HomeView `category-grid` muster).
- `ToolsPagination` all, `v-if="toolsResponse && toolsResponse.totalPages > 0"`.

## Veakäsitlus

| Päring | Olukord | Käitumine |
|---|---|---|
| kõik | `!error.response` | `NETWORK_ERROR_MESSAGE`, korduskatse nupp. |
| `GET /api/tools` | 400 `INCORRECT_INPUT` | `message` sisendveana; tulemust ei näidata tühjana; „Lähtesta otsing“. |
| `GET /api/tools` | 500 | `message`, „Proovi uuesti“; vana tulemus märgitakse aegunuks. |
| `GET /api/categories`, `/cities` | 500 | Vastava rippmenüü juures viga; tööriistakaarte ei kustutata. |
| `GET /api/cities/{id}/districts` | 404 `PRIMARY_KEY_NOT_FOUND` | `message`, `districts = []`, linnaosa rippmenüü keelatud. |
| `GET /api/cities/{id}/districts` | 200 `[]` | Mitte viga — rippmenüüs ainult „Vali linnaosa“. |
| URL | vigane `categoryId`/`cityId`/`districtId`/`pageNumber` | Päringut ei tehta, `urlErrorMessage` + „Lähtesta otsing“. |

Kõik `isXLoading = false` käib `.finally()` kaudu; tööriistade ja linnaosade puhul ainult viimase `requestId` korral.

## Testid / käsitsi kontroll

Frontendis automaattestide raamistikku pole (`package.json`-is puudub Vitest) — kontroll käib käsitsi `npm run dev` + backend `localhost:8080` + `3_import.sql` andmetega. Enne commitit `npm run lint` ja `npm run format`.

1. `/tools` — 7 tulemust, 1 leht: toolId 1, 3, 4, 5, 6, 7, 8. DevTools Network: päringus pole `pageSize` ega `status`; vastuses `pageSize: 12`. Impordiandmetega ei saa mitut lehte tekitada — mitme lehe kontrolliks lisa ajutiselt tööriistu või kontrolli `?pageNumber=2` (tühja lehe olek).
2. HomeView kategooriakaart (sisse logituna) → `/tools?categoryId=2` — **üks** päring `categoryId=2`, rippmenüüs „Ehitustööd“, 1 tulemus (Akutrell).
3. „Tühista filtrid“ → URL `/tools`, 7 tulemust.
4. Linn Tallinn → linnaosad laetakse; linn „Vali linn“ → districts päringut pole, linnaosa keelatud ja nullitud.
5. Kiire linnavahetus Tallinn → Tartu (Network throttling „Slow 3G“) — Tallinna linnaosad ei ilmu Tartu valikusse.
6. `cityId=1&districtId=2` → toolId 3, 4, 8. `cityId=2` → „ei leitud“.
7. Rippmenüü muutmine ilma „Rakenda“-ta → päringut ei tehta; lehevahetus kasutab rakendatud filtreid.
8. `/tools?pageNumber=99` → „Sellel lehel tööriistu pole“, ToolsPagination töötab. `/tools?pageNumber=abc`, `?categoryId=-1`, `?categoryId=` → URL-viga, päringut ei tehta.
9. `/tools?categoryId=999` → tühi tulemus, rippmenüüs „Tundmatu kategooria (ID 999)“.
10. Brauseri tagasi/edasi → õiged tulemused, topeltpäringuid pole.
11. Backend maha → võrguviga kõigil päringutel, korduskatse töötab.
12. Väljalogitult `/tools` avaneb ilma Google modaalita.
13. „Vaata detaile“ → URL `/tools/<toolId>` (vaade sõltub ToolDetailView taskist).

## Otsused (kasutajaga kinnitatud)

1. **`/tools/:toolId` route'i selles taskis ei lisata** (kuulub ToolDetailView taskile, väldib merge-konflikti). „Vaata detaile“ navigeerib `/tools/<toolId>` URL-ile, mis kuvab tühja lehe kuni ToolDetailView valmib.
2. **Kirjelduse väli on `description`** nagu backendis (`ToolListItemDto`). Taski väljade tabeli `categoryDescription` on viga.
3. **Lehe suurus on 12** nagu backendis. Frontend ei saada `pageSize` parameetrit. Taski tekst (`pageSize=6`, „7 tulemust / 2 lehte“) erineb sellest.
4. **`cityId` ja `districtId` sünkroonitakse URL-i** koos `categoryId` ja `pageNumber`-iga.
5. **CRLF muudatused** (169 faili) parandab kasutaja ise enne `git merge origin/master` (vt „Merge-konfliktide analüüs“).

## Teadmiseks

- **HomeView nõuab sisselogimist** enne `/tools` avamist, ToolsView ise on avalik — HomeView-d see task ei muuda.
