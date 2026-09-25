# Profiili täitmine ja muutmine — implementatsiooni plaan

**Seotud task:** [Profiili-taitmine-ja-muutmine.md](./Profiili-taitmine-ja-muutmine.md)

**Vaade:** `MyProfile.vue`, route `/profile`

**API:** `GET /api/users/me/profile`, `PUT /api/users/me/profile`, `GET /api/cities`, `GET /api/cities/{cityId}/districts`

## Hetkeseis (mis on juba olemas)

- `frontend/src/main.js` — Vue app, Pinia, router, Bootstrap 5; Axios on globaalne (`this.$axios`).
- `frontend/src/App.vue` — toorikprojekti `<nav>` (lingid Home, Test) ja `<RouterView />`.
- `frontend/src/router/index.js` — rajad `/` (`homeRoute`, `HomeView.vue`) ja `/test`.
- `frontend/vite.config.js` — proxy `/api` → `http://localhost:8080`.
- `frontend/src/views/HomeView.vue`, `TestView.vue`, `components/TestComponent.vue` — toorikfailid.

Puuduvad: kaustad `api-services/`, `navigation/`, `components/common|forms/`, vaade `MyProfile.vue`, rada `/profile`, `AlertDanger.vue`. Backendis pole veel controllereid (`find backend/src/main/java -name "*Controller.java"` ei leia midagi), seega kontrakt tuleb backend taskidest ja kuni backendi valmimiseni saab vaadet katsetada ainult mock-andmetega.

## Sammud

### 1. API teenused

**`frontend/src/api-services/ProfileService.js`**

```js
import axios from 'axios'

export default {
  sendGetMyProfileRequest() {
    return axios.get('/api/users/me/profile')
  },

  sendPutMyProfileRequest(profile) {
    return axios.put('/api/users/me/profile', profile)
  },
}
```

**`frontend/src/api-services/CityService.js`** (jagatud ToolsView'ga — kui see on juba loodud, kasuta olemasolevat)

```js
import axios from 'axios'

export default {
  sendGetCitiesRequest() {
    return axios.get('/api/cities')
  },

  sendGetCityDistrictsRequest(cityId) {
    return axios.get(`/api/cities/${cityId}/districts`)
  },
}
```

Sessiooniküpsis (`JSESSIONID`) liigub samal originil (Vite proxy) automaatselt; `withCredentials` pole vaja.

### 2. Navigatsiooniteenus

**`frontend/src/navigation/NavigationService.js`** (kui puudub)

```js
import router from '@/router'

export default {
  navigateToHomeView() {
    router.push({ name: 'homeRoute' })
  },
}
```

### 3. Ühine veateate komponent

**`frontend/src/components/common/AlertDanger.vue`**

- `props: { errorMessage: String }`
- Template: `<div v-if="errorMessage" class="alert alert-danger" role="alert">{{ errorMessage }}</div>`

### 4. Rippmenüüde komponendid

**`frontend/src/components/forms/CitiesDropdown.vue`**

- `props`: `cities: Array`, `selectedCityId: Number`, `firstOptionLabel: { type: String, default: 'Kõik linnad' }`
- `emits: ['event-new-city-selected']`
- `<select class="form-select">`: esimene valik `value="0"` tekstiga `firstOptionLabel`, siis `v-for="city in cities" :key="city.cityId" :value="city.cityId"`.
- `@change` → `this.$emit('event-new-city-selected', Number($event.target.value))`.

**`frontend/src/components/forms/DistrictsDropdown.vue`**

- `props`: `districts: Array`, `selectedDistrictId: Number`, `isDisabled: Boolean`, `firstOptionLabel: { type: String, default: 'Kõik linnaosad' }`
- `emits: ['event-new-district-selected']`
- Sama muster; `:disabled="isDisabled"`.

`0` tähendab „pole valitud“. Vaikimisi tekst („Kõik linnad“ / „Kõik linnaosad“) sobib ToolsView filtrile; profiilivormis antakse propina `first-option-label="Vali linn"` ja `first-option-label="Vali linnaosa"` (kinnitatud).

### 5. Vaade

**`frontend/src/views/MyProfile.vue`**

`data()`:

```js
data() {
  return {
    errorMessage: '',
    isLoading: true,
    isSaving: false,
    cities: [],
    districts: [],
    selectedCityId: 0,
    profile: {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      districtId: 0,
      streetName: '',
      houseNumber: '',
      apartmentNumber: '',
    },
  }
},
```

`computed`:

- `isFormValid()` — kõik stringiväljad peale `apartmentNumber` on `trim()`-i järel mittetühjad, `selectedCityId !== 0`, `profile.districtId !== 0`.
- `isDistrictsDisabled()` — `selectedCityId === 0`.

`methods` (järjekord: laadimine → vastuse handle'id → kasutajategevused → salvestamine):

- `getMyProfile()` → `ProfileService.sendGetMyProfileRequest()` → `handleGetMyProfileResponse(response.data)` / `handleApiError(error)` / `.finally(() => (this.isLoading = false))`.
- `handleGetMyProfileResponse(profileDto)` — kopeeri väljad `profile` objekti (`null` → `''`, `districtId null` → `0`); `selectedCityId = profileDto.cityId ?? 0`; kui `selectedCityId !== 0`, kutsu `getCityDistricts()`.
- `getCities()` → `CityService.sendGetCitiesRequest()` → `this.cities = response.data` / `handleApiError`.
- `getCityDistricts()` → `CityService.sendGetCityDistrictsRequest(this.selectedCityId)` → `this.districts = response.data` / `handleGetCityDistrictsError(error)` (tühjenda `districts`, siis `handleApiError`).
- `handleCitySelected(cityId)` — `selectedCityId = cityId`; `profile.districtId = 0`; `districts = []`; kui `cityId !== 0`, `getCityDistricts()`.
- `handleDistrictSelected(districtId)` — `profile.districtId = districtId`.
- `saveProfile()` — `errorMessage = ''`; kui `!isFormValid` → `errorMessage = 'Täida kõik kohustuslikud väljad'` ja `return`; muidu `isSaving = true` ja `ProfileService.sendPutMyProfileRequest(this.createProfileRequest())` → `handleSaveProfileResponse()` / `handleApiError` / `.finally(() => (this.isSaving = false))`.
- `createProfileRequest()` — tagastab objekti väljadega `firstName`, `lastName`, `email`, `phone`, `districtId`, `streetName`, `houseNumber` (kõik `trim()`-itud) ja `apartmentNumber: this.profile.apartmentNumber.trim() || null`. `cityId`-d ei lisata.
- `handleSaveProfileResponse()` — uuenda ühine kasutajaolek (vt samm 7) ja `NavigationService.navigateToHomeView()`.
- `handleApiError(error)`:
  - `!error.response` → `errorMessage = 'Serveriga ei saanud ühendust. Palun proovi hiljem uuesti.'` (täpne sõnastus on UI täpsustus);
  - `status === 401` → tühjenda kasutajaolek, `navigateToHomeView()`;
  - muidu `errorMessage = error.response.data.message`.

`beforeMount()`:

```js
beforeMount() {
  this.getMyProfile()
  this.getCities()
},
```

Template (mockupi järjekord, kinnitatud — Linnaosa enne Linna):

- `<h1>Minu profiil</h1>`, `<AlertDanger :error-message="errorMessage" />`
- `v-model` väljad: Eesnimi, Perenimi, E-post (`type="email"`), Telefon, Tänava nimi, Majanumber, Korteri number (valikuline) — Bootstrap `form-label` + `form-control`, `maxlength` vastavalt taski tabelile.
- `<DistrictsDropdown first-option-label="Vali linnaosa" :districts="districts" :selected-district-id="profile.districtId" :is-disabled="isDistrictsDisabled" @event-new-district-selected="handleDistrictSelected" />`
- `<CitiesDropdown first-option-label="Vali linn" :cities="cities" :selected-city-id="selectedCityId" @event-new-city-selected="handleCitySelected" />`
- `<button class="btn btn-primary" :disabled="isLoading || isSaving" @click="saveProfile">Salvesta</button>`

### 6. Router

**`frontend/src/router/index.js`** — lisa:

```js
import MyProfile from '@/views/MyProfile.vue'
// ...
{
  path: '/profile',
  name: 'profileRoute',
  component: MyProfile,
},
```

### 7. Ühine kasutajaolek (sõltuvus)

Pärast salvestamist peab `hasProfile` muutuma `true`-ks, et päis ja teised vaated kasutajat enam `/profile` peale ei suunaks. Kasutajaolek ja `GET /api/me` kuuluvad Header/sessioonikontrolli taski (vt [Google kontoga sisselogimine](../GoogleLoginView/Google-kontoga-sisselogimine.md), `frontend/src/auth/`).

- Kui ühine olek (nt Pinia store `src/stores/` või `src/auth/`) on juba olemas, kutsu selle värskendusmeetodit (korda `GET /api/me`).
- Kui seda veel pole, jäta `handleSaveProfileResponse()` sisse selge `TODO` kommentaar ja suuna ainult avalehele; ära loo selle taski sees paralleelset kasutajaolekut.

### 8. Lint ja vormindus

```sh
cd frontend
npm run lint
npm run format
```

Prettier: ilma semikooloniteta, ülakomad, rea laius 100.

## Kontroll

Frontendis automaattestide raamistikku pole, seega kontrolli käsitsi (`npm run dev`, http://localhost:8081/profile):

1. **Backendita:** API teenustes saab ajutiselt tagastada `Promise.resolve({ data: ... })` taski JSON näidetega (profiiliga ja profiilita kasutaja), et kontrollida eeltäitmist, linna/linnaosa sõltuvust ja valideerimist. Eemalda mock enne commit'i.
2. **Backendiga** (kui backend taskid on tehtud ja Google login töötab):
   - profiiliga kasutaja (demokasutajaga seotud `sub`, vt `googlega_login.md` jaotis 9) → väljad eeltäidetud;
   - uus Google kasutaja → `hasProfile: false`, nimi ja e-post eeltäidetud; salvestamise järel suunamine `/` ja `GET /api/me` → `hasProfile: true`;
   - teise kasutaja e-post (`email@Gmail.com`) → teade „Sellise e-postiga kasutaja on juba süsteemis olemas“;
   - tühi kohustuslik väli → „Täida kõik kohustuslikud väljad“, võrgupäringut ei tehta (DevTools → Network);
   - korterinumber tühi → päringus `"apartmentNumber": null`;
   - väljalogitud olekus `/profile` → 401 → suunamine avalehele.
3. `npm run lint` lõpeb vigadeta.

## Riskid ja sõltuvused

- Backendi `GET/PUT /api/users/me/profile` ja `/api/cities/**` pole veel implementeeritud.
- Ühine kasutajaolek ja `hasProfile: false` suunamine sõltuvad Header/GoogleLoginView taskist.
- `api-services`, `navigation`, `AlertDanger.vue` ja rippmenüüd võivad tekkida paralleelselt ka teistes taskides (ToolsView, BookingFormView). Enne loomist kontrolli, kas fail on juba olemas, ja taaskasuta seda.
