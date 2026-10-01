# Tööriista detailvaade — implementatsiooni plaan

**Seotud task:** [Tooriista-detailvaade.md](./Tooriista-detailvaade.md)

**Vaade:** `ToolDetailView.vue`, route `/tools/:toolId` (`toolDetailRoute`)

**API:** `GET /api/tools/{toolId}` (avalik), `GET /api/users/{userId}` (ainult sisse logitud)

> Plaan on 2026-10-01 ümber kirjutatud. Eelmine versioon eeldas, et kõik failid puuduvad. Tegelikult on vaade master'is suures osas valmis (Jaroslav, commit `10208bb` „BE-49-new classes“), seega on see plaan **lünkade analüüs**: mis on olemas, mis vajab parandamist.

## Hetkeseis (mis on juba olemas)

**Backend (mõlemad endpointid valmis):**

- `backend/.../controller/tool/ToolController.java` — `GET /api/tools/{toolId}` → `ToolDetailResponse` (`toolId`, `ownerId`, `toolName`, `categoryName`, `description`, `imageData`, `status`). BE-47.
- `backend/.../controller/appuser/UserDetailController.java` — `GET /api/users/{userId}` → `UserDetailResponse` (`userId`, `firstName`, `lastName`, `email`, `phone`).
- `backend/.../controller/appuser/AppUserController.java` — `GET /api/me` → `CurrentUserDto` (sh `userId`).

**Frontend:**

| Fail | Seis |
|---|---|
| `frontend/src/views/ToolDetailView.vue` | **Olemas.** Laeb tööriista, sisse logitud kasutajale omaniku; laadimise olek; 404/400/500 → backendi `message`, tööriista ja nuppu ei näidata; staatus `U` → teade; „Laenuta“ → booking või `openLoginModal()`; `$route.params.toolId` watch; vana vastuse ignoreerimine (`loadGeneration`); omaniku 401 → `session.status = 'guest'`. |
| `frontend/src/components/common/ToolImage.vue` | **Olemas.** Base64 → `data:image/svg+xml`, `@error` → kohatäitja. |
| `frontend/src/components/common/OwnerContactCard.vue` | **Olemas.** Nimi, `mailto:` e-mail ja `tel:` telefon, `null` read peidetud. Kasutab ainult ToolDetailView. |
| `frontend/src/components/common/AlertDanger.vue` | Olemas. |
| `frontend/src/api-services/ToolService.js` | `sendGetToolDetailsRequest(toolId)` olemas. |
| `frontend/src/api-services/UserService.js` | `sendGetUserDetailsRequest(userId)` olemas. |
| `frontend/src/navigation/NavigationService.js` | `navigateToBookingFormView(router, toolId)` olemas. |
| `frontend/src/router/index.js` | `/tools/:toolId` (`toolDetailRoute`) ja `/tools/:toolId/booking` olemas. |
| `frontend/src/auth/session.js` | Ühine olek (`session.status`: `loading`/`authenticated`/`guest`/`error`, `session.user.userId`). |
| `frontend/src/App.vue` | `provide('openLoginModal', returnPath)`; `auth/loginReturnPath.js` lubab tagasipöördumist **ainult** `/bookings/<id>` radadele. |

## Merge-konfliktide analüüs (teised harud)

- `KERSTI-FE-49-TOOLFRONTEND` on `origin/master`-iga samal seisul (0/0). `git fetch` järel ükski remote haru pole master'ist ees.
- **Risk:** `ToolDetailView.vue`, `ToolImage.vue`, `OwnerContactCard.vue` on Jaroslavi kirjutatud (BE-49 / WIP commitid). Kui Jaroslavil on kohalikus harus nende failide pooleli muudatusi, tekib konflikt. **Enne alustamist lepi Jaroslaviga kokku**, et ToolDetailView fail on nüüd sinu käes.
- Muudatused on väikesed ja lokaalsed (ühe faili sees), seega ka konflikti korral lihtsalt lahendatavad.
- `auth/loginReturnPath.js`, `App.vue`, `session.js` on jagatud failid (BookingApprovalView kasutab) — puudutada ainult otsuse p 3 korral.
- Tööpuus olevad CRLF-ainult muudatused jäävad commitist välja.

## Puuduv/muudetav

| # | Lünk | Fail | Taski viide |
|---|---|---|---|
| 1 | `toolDescription` loeb `categoryDescription ?? description` — `categoryDescription` välja pole (otsus: `description`). | `ToolDetailView.vue` | Kasutajaliidese elemendid |
| 2 | Võrguvea (`!error.response`) korral näidatakse 500-ga sama teadet; taski järgi üldine võrguviga. Teistes vaadetes on `NETWORK_ERROR_MESSAGE`. | `ToolDetailView.vue` | Veatabelid „Vastus puudub“ |
| 3 | Taski „Komponendid ja failistruktuur“ tabel ja API lõigud väidavad, et failid/controllerid puuduvad; meetodinimed (`sendGetToolRequest`, `sendGetUserRequest`) erinevad tegelikest. | `Tooriista-detailvaade.md` | — |
| 4 | Lahtised otsad (oma tööriist, e-posti link, tagasipöördumine pärast sisselogimist) — vt „Avatud küsimused“. | — | Täpsusta enne implementeerimist |

## Sammud

1. **Kirjeldus ainult `description` väljast** — fail: `frontend/src/views/ToolDetailView.vue`

   ```js
   toolDescription() {
     return this.tool?.description ?? ''
   },
   ```

2. **Võrguvea teade** — fail: `frontend/src/views/ToolDetailView.vue`
   - Lisa konstant `const NETWORK_ERROR_MESSAGE = 'Serveriga ei saanud ühendust. Palun proovi hiljem uuesti.'` (sama tekst nagu `MyProfile.vue`, `MyToolsView.vue`, `ToolsView.vue`).
   - `handleToolError`: `if (!error?.response) { this.errorMessage = NETWORK_ERROR_MESSAGE; return }`, edasi nagu praegu.
   - `handleOwnerError`: sama, `ownerErrorMessage`-iga (tööriista andmed jäävad).

3. **Taskifaili uuendamine** — fail: `docs/tasks/frontend/ToolDetailView/Tooriista-detailvaade.md` (CRLF säilib)
   - API lõikudest eemalda „Backendi controllerit veel pole“.
   - „Komponendid ja failistruktuur“: kõik read „Olemas“, õiged meetodinimed (`sendGetToolDetailsRequest`, `sendGetUserDetailsRequest`, `navigateToBookingFormView(router, toolId)`), lause „Olemasolevat koodi ... ei muudetud“ asenda viitega sellele plaanile.
   - Lahtiste otste lõik: lisa otsused (vt „Avatud küsimused“).

4. **Valikulised muudatused** — ainult kasutaja otsuse järgi (vt „Avatud küsimused“ p 1–3).

5. **Lint ja vormindus** — `npx eslint` ja `npx prettier --check` muudetud failidele (oxlint ja `npm run build` ei tööta WSL-is Windowsi `node_modules` tõttu — käivita Windowsis).

## Veakäsitlus

| Päring | Olukord | Praegu | Pärast |
|---|---|---|---|
| `GET /api/tools/{id}` | 404 / 400 / 500 | backendi `message`, tööriista ja nuppu pole | muutmata |
| `GET /api/tools/{id}` | võrguviga | `TOOL_LOAD_FAILED` | `NETWORK_ERROR_MESSAGE` |
| `GET /api/users/{id}` | 401 | `session.status = 'guest'`, kontaktikast peidus | muutmata |
| `GET /api/users/{id}` | 404 / 500 | `message` kontaktikasti asemel, tööriist jääb | muutmata |
| `GET /api/users/{id}` | võrguviga | `OWNER_LOAD_FAILED` | `NETWORK_ERROR_MESSAGE` |

## Testid / käsitsi kontroll

Frontendis automaattestide raamistikku pole. Käsitsi (`npm run dev` Windowsis, backend + `3_import.sql`):

1. Külastaja `/tools/1` → Akutrell, „Ehitustööd“, pilt; kontaktikasti pole; Network'is pole `GET /api/users/1`; „Laenuta“ → sisselogimise modaal.
2. Sisse logitud `/tools/1` → Marko Tamm, e-mail, telefon; „Laenuta“ → `/tools/1/booking`.
3. `/tools/2` → „Tööriist pole hetkel saadaval“, nupp aktiivne.
4. `/tools/999` → 404 teade, nuppu pole. `/tools/abc` → 400 teade.
5. Pildita / kirjelduseta tööriist → kohatäitja ja „Kirjeldus puudub“.
6. Profiilita omanik → e-posti ja telefoni ridu pole.
7. Backend maas → võrguvea teade.
8. ToolsView „Vaata detaile“ → õige detailvaade; tagasi-nupp → otsing säilib.

## Otsused (kasutajaga kinnitatud)

1. **Oma tööriist:** „Laenuta“ on keelatud, kui `session.user.userId === tool.ownerId`; nupu all selgitus „See on sinu tööriist. Oma tööriista laenutada ei saa.“ (`ToolDetailView.vue`, computed `isOwnTool`).
2. **E-posti link:** `mailto:`/`tel:` lingid jäävad; taski tekst parandatud.
3. **Tagasipöördumine pärast sisselogimist:** praegu ei muudeta (`auth/loginReturnPath.js` jääb puutumata).
4. **Staatus `U`:** „Laenuta“ jääb aktiivseks, teade näidatakse.

## Teostus (2026-10-01)

- `ToolDetailView.vue`: `description` ilma `categoryDescription` varuvariandita; `NETWORK_ERROR_MESSAGE` tööriista ja omaniku päringu võrguveale; `isOwnTool` + keelatud nupp + selgitus.
- `Tooriista-detailvaade.md`: komponentide tabel, API lõigud, lahtised otsad → otsused, uus vastuvõtu kriteerium.
- Prettieri hoiatused failis (kolm pikka rida) on varasemast koodist ja jäeti konfliktide vältimiseks muutmata.
