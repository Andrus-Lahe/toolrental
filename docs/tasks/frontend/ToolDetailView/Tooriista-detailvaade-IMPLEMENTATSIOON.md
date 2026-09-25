# Tööriista detailvaade — implementatsiooni plaan

**Seotud task:** [Tooriista-detailvaade.md](./Tooriista-detailvaade.md)

**Vaade:** `ToolDetailView.vue`, route `/tools/{toolId}`

**API:** `GET /api/tools/{toolId}` (avalik), `GET /api/users/{userId}` (ainult sisse logitud)

## Hetkeseis (mis on juba olemas)

- `frontend/src/main.js` — Vue app, Pinia, router, Bootstrap 5; Axios globaalselt (`this.$axios`).
- `frontend/src/App.vue` — toorikprojekti `<nav>` ja `<RouterView />`.
- `frontend/src/router/index.js` — rajad `/` ja `/test`.
- `frontend/vite.config.js` — proxy `/api` → `http://localhost:8080`.
- `@phosphor-icons/vue` on sõltuvustes (ikoonid ümbriku ja telefoni jaoks).

Puuduvad: `api-services/`, `navigation/`, `auth/`, `components/common/`, `ToolDetailView.vue`, rajad `/tools/:toolId` ja `/tools/:toolId/booking`. Backendis pole controllereid, seega kuni backendi valmimiseni saab katsetada ainult mock-andmetega.

**Paralleelsed taskid, mis loovad samu faile:** MyProfile (`AlertDanger.vue`, `NavigationService.js`), GoogleLoginView (`auth/`, `GoogleLoginModal.vue`), ToolsView/BookingFormView (`ToolService.js`). Enne faili loomist kontrolli, kas see on juba olemas, ja täienda olemasolevat.

## Sammud

### 1. API teenused

**`frontend/src/api-services/ToolService.js`** (lisa meetod, kui fail on juba olemas)

```js
import axios from 'axios'

export default {
  sendGetToolRequest(toolId) {
    return axios.get(`/api/tools/${toolId}`)
  },
}
```

**`frontend/src/api-services/UserService.js`**

```js
import axios from 'axios'

export default {
  sendGetUserRequest(userId) {
    return axios.get(`/api/users/${userId}`)
  },
}
```

### 2. Navigatsioon

**`frontend/src/navigation/NavigationService.js`** — lisa:

```js
navigateToBookingFormView(toolId) {
  router.push({ name: 'bookingFormRoute', params: { toolId } })
},
```

### 3. Ühised komponendid

**`frontend/src/components/common/ToolImage.vue`**

- `props: { imageData: String, altText: String }`
- `computed.imageSource()` — `imageData ? 'data:image/svg+xml;base64,' + imageData : ''` (MIME-tüüp on taski lahtine ots; hoia see ühes kohas, et hiljem oleks lihtne muuta).
- Template: `<img v-if="imageSource" :src="imageSource" :alt="altText" class="img-fluid">`, muidu kohatäitja (`<div class="bg-light border ...">`).

**`frontend/src/components/common/OwnerContactCard.vue`**

- `props: { owner: Object }` (kuju nagu `UserDetailResponse`)
- `computed.ownerFullName()` — `owner.firstName + ' ' + owner.lastName`
- Template (Bootstrap `card`): pealkiri „Omaniku kontaktinfo“, nimi, `v-if="owner.email"` rida `PhEnvelope` ikooniga, `v-if="owner.phone"` rida `PhDeviceMobile` ikooniga (`@phosphor-icons/vue`).

**`frontend/src/components/common/AlertDanger.vue`** — nagu MyProfile plaanis (prop `errorMessage`, `alert alert-danger`).

### 4. Vaade

**`frontend/src/views/ToolDetailView.vue`**

`data()`:

```js
data() {
  return {
    errorMessage: '',
    ownerErrorMessage: '',
    isLoading: true,
    toolId: 0,
    tool: {
      toolId: 0,
      ownerId: 0,
      toolName: '',
      categoryName: '',
      description: '',
      imageData: '',
      status: '',
    },
    owner: null,
  }
},
```

`computed`:

- `isLoggedIn()` — ühisest kasutajaolekust (samm 6).
- `isToolLoaded()` — `tool.toolId !== 0`.
- `isToolUnavailable()` — `tool.status === 'U'`.

`methods`:

- `getTool()` → `ToolService.sendGetToolRequest(this.toolId)` → `handleGetToolResponse(response.data)` / `handleGetToolError(error)` / `.finally(() => (this.isLoading = false))`.
- `handleGetToolResponse(toolResponse)` — `this.tool = toolResponse`; kui `isLoggedIn`, kutsu `getOwner()`.
- `handleGetToolError(error)` — `!error.response` → üldine võrguvea teade; muidu `errorMessage = error.response.data.message` (404, 400, 500).
- `getOwner()` → `UserService.sendGetUserRequest(this.tool.ownerId)` → `this.owner = response.data` / `handleGetOwnerError(error)`.
- `handleGetOwnerError(error)` — `owner = null`; 401 → tühjenda ühine kasutajaolek (samm 6); muu → `ownerErrorMessage = error.response?.data?.message ?? <üldine teade>`.
- `handleLendClick()` — kui `isLoggedIn` → `NavigationService.navigateToBookingFormView(this.toolId)`; muidu ava ühine sisselogimise modaal (samm 6).

`beforeMount()`:

```js
beforeMount() {
  this.toolId = Number(this.$route.params.toolId)
  this.getTool()
},
```

`toolId` saadetakse backendile ka siis, kui see pole number (`NaN` → backend annab 400); eraldi frontendi valideerimist pole vaja, sest veateate annab backend.

Template:

- `<h1>Tööriista detailid</h1>`, `<AlertDanger :error-message="errorMessage" />`
- `v-if="isToolLoaded"` plokk (Bootstrap `row`):
  - vasak veerg: `<ToolImage :image-data="tool.imageData" :alt-text="tool.toolName" />`, nimi, kategooria, kirjeldus (kirjutuskaitstud tekst, mitte `<input>`), `v-if="isToolUnavailable"` teade „Tööriist pole hetkel saadaval“;
  - parem veerg: `v-if="isLoggedIn"` → `<AlertDanger :error-message="ownerErrorMessage" />` ja `<OwnerContactCard v-if="owner" :owner="owner" />`.
- `<button v-if="isToolLoaded" class="btn btn-primary" :disabled="isLoading" @click="handleLendClick">Laenuta</button>`
- Lehekülgede riba ei implementeerita (skoobist väljas).

### 5. Router

**`frontend/src/router/index.js`** — lisa:

```js
import ToolDetailView from '@/views/ToolDetailView.vue'
// ...
{
  path: '/tools/:toolId',
  name: 'toolDetailRoute',
  component: ToolDetailView,
},
```

Rada `/tools/:toolId/booking` (`bookingFormRoute`) lisab BookingFormView task. Kui seda veel pole, lisa see koos platsihoidjaga või jäta `navigateToBookingFormView` kutse `TODO`-ga, et navigeerimine ei viskaks viga.

Kui kasutaja liigub ühelt tööriistalt teisele sama komponendi sees (rada muutub, komponent jääb), ei käivitu `beforeMount` uuesti. Praegu selliseid linke vaates pole; kui need lisanduvad, lisa `watch: { '$route.params.toolId' }` või `:key="$route.fullPath"` `<RouterView>`-ile.

### 6. Ühine kasutajaolek ja sisselogimine (sõltuvus)

`isLoggedIn`, 401 käsitlemine ja sisselogimise modaal kuuluvad GoogleLoginView taski (`frontend/src/auth/`, `GoogleLoginModal.vue`, `GET /api/me`).

- Kui need on olemas: `isLoggedIn` loe ühisest olekust; külastaja „Laenuta“ avab `GoogleLoginModal.vue`.
- Kui neid veel pole: ajutiselt `isLoggedIn` = `false` ja külastaja „Laenuta“ → `window.location.href = '/oauth2/authorization/google'` (sama link, mida modaal kasutaks), koos `TODO` kommentaariga. Paralleelset kasutajaolekut ära loo.

Oluline: omaniku päring tehakse alles siis, kui kasutajaolek on teada. Kui `GET /api/me` vastus saabub pärast tööriista vastust, kutsu `getOwner()` kasutajaoleku muutumisel (nt `watch` ühise oleku `isLoggedIn` peale).

### 7. Lint ja vormindus

```sh
cd frontend
npm run lint
npm run format
```

## Kontroll

Frontendis automaattestide raamistikku pole, seega kontrolli käsitsi (`npm run dev`, http://localhost:8081/tools/1):

1. **Backendita:** API teenustes ajutiselt `Promise.resolve({ data: ... })` taski JSON näidetega; `isLoggedIn` ajutiselt `true`/`false`, et kontrollida mõlemat varianti. Eemalda mock enne commit'i.
2. **Backendiga:**
   - külastaja: `/tools/1` → Akutrell ja pilt; kontaktikasti pole; Network'is pole `GET /api/users/1`; „Laenuta“ → sisselogimine;
   - sisse logitud: kontaktikastis Marko Tamm, email@Gmail.com, 56565656; „Laenuta“ → `/tools/1/booking`;
   - `/tools/2` → teade „Tööriist pole hetkel saadaval“;
   - `/tools/999` → „Ei leidnud primary keyd 'toolId' väärtusega: 999“, nuppu pole;
   - `/tools/abc` → 400 teade;
   - profiilita omanik (testandmetega) → e-posti ja telefoni ridu pole.
3. `npm run lint` lõpeb vigadeta.

## Riskid ja sõltuvused

- Backendi `GET /api/tools/{toolId}` ja `GET /api/users/{userId}` pole implementeeritud.
- Sisselogimise olek ja modaal sõltuvad GoogleLoginView taskist.
- Pildi MIME-tüüp on lahtine (praegu eeldatakse SVG-d impordiandmete järgi).
- Pärast Google'iga sisselogimist suunab backend avalehele, mitte tagasi tööriista lehele.
