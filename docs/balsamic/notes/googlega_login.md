# Google'iga sisselogimine (Spring Boot 4 + Vue SPA)

See juhend kirjeldab, kuidas lisada toolrental projekti Google'iga sisselogimine nii, et see sobiks meie andmebaasi struktuuriga (`app_user.google_sub`, eraldi `profile` tabel).

## Üldpilt

1. Kasutaja vajutab frontendis nuppu „Logi sisse Google'iga“ ja satub aadressile `/oauth2/authorization/google`.
2. Spring suunab ta Google'i sisselogimislehele.
3. Google saadab ta tagasi redirect URI-le koos ühekordse **koodiga**.
4. Backend vahetab koodi serveri poolel **ID tokeni** vastu (kasutades client secret'it) ja kontrollib tokeni allkirja.
5. Meie `AppUserOidcService` otsib `app_user` tabelist rea `google_sub` järgi. Kui rida puudub, luuakse uus kasutaja rolliga `customer`.
6. Spring loob sessiooni (küpsis `JSESSIONID`) ja suunab kasutaja tagasi frontendi.
7. Frontend küsib `GET /api/me` kaudu, kes on sisse logitud ja kas tal on profiil olemas. Kui profiil puudub, suunatakse ta profiili täitmise vormile.

## Andmebaasi struktuur ja sisselogimine

| Väli | Kust tuleb | Millal täidetakse |
|---|---|---|
| `app_user.google_sub` | Google (`sub`) | esimesel sisselogimisel |
| `app_user.first_name` | Google (`given_name`) | esimesel sisselogimisel |
| `app_user.last_name` | Google (`family_name`), võib puududa → `""` | esimesel sisselogimisel |
| `app_user.role_id` | kood paneb `customer` (id 2) | esimesel sisselogimisel |
| `app_user.status` | andmebaasi vaikeväärtus `'A'` | esimesel sisselogimisel |
| `profile.email` | Google (`email`), eeltäidetakse vormis | profiili täitmisel |
| `profile.phone`, `profile.location_id` | kasutaja sisestab | profiili täitmisel |

**Miks profiili ei looda kohe?** Tabelis `profile` on `phone` ja `location_id` väärtusega `NOT NULL`, aga Google neid ei anna. Seepärast luuakse esimesel sisselogimisel ainult `app_user` rida ja profiil tekib hiljem eraldi vormi kaudu. Skeemi selleks muutma ei pea, aga **kood peab arvestama kasutajaga, kellel profiili veel pole**. Näiteks ei tohiks ta saada tööriista lisada ega broneerida enne, kui profiil on täidetud.

**Tähtis:** kasutajat tuvastatakse alati `sub` väärtuse järgi, mitte e-posti järgi, sest e-post võib muutuda.

## 1. Google Cloud Console

1. Loo projekt ja **OAuth consent screen** (scope'id `openid`, `email`, `profile`).
2. Loo **OAuth client ID** tüübiga *Web application*.
3. Lisa **Authorized redirect URI**:
   ```
   http://localhost:5173/login/oauth2/code/google
   ```
   Port on 5173 (Vite), mitte 8080, sest sisselogimise päringud käivad läbi Vite proxy (vt punkt 6).
4. Kuni rakendus pole avaldatud („Publish app“), saavad sisse logida ainult **testkasutajad**. Lisa oma Google'i konto testkasutajate hulka.

## 2. Sõltuvus (`backend/build.gradle`)

Projekt kasutab Gradle'it. Spring Boot 4-s on starteri nimi `spring-boot-starter-security-oauth2-client` (vana nimi `spring-boot-starter-oauth2-client` on alles vaid ühilduvuse jaoks):

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security-oauth2-client'
}
```

## 3. Seadistus (`backend/src/main/resources/application.properties`)

```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,email,profile
```

Spring teab Google'i aadresse juba ise, nii et need tuleb vaid registreerida.

**Client secret'it ei tohi kunagi Git'i panna.** Määra `GOOGLE_CLIENT_ID` ja `GOOGLE_CLIENT_SECRET` keskkonnamuutujatena (IntelliJ-s: *Run Configuration → Environment variables*).

## 4. Kasutaja sidumine andmebaasiga (`AppUserOidcService`)

Springi vaikimisi `OidcUserService` teab ainult Google'i andmeid. Meil on vaja:

- leida või luua `app_user` rida `google_sub` järgi;
- keelata sisselogimine blokeeritud kasutajal (`status = 'B'`);
- anda kasutajale tema rolli järgi õigus (`ROLE_admin` / `ROLE_customer`);
- hoida sessioonis meie enda `app_user.id` väärtust, et controllerid ei peaks igal päringul `sub` järgi otsima.

### Principal, mis sisaldab `app_user.id` väärtust

```java
package ee.toolrental.infrastructure.security;

public class AppUserPrincipal extends DefaultOidcUser {

    private final Integer userId;

    public AppUserPrincipal(Integer userId, Collection<? extends GrantedAuthority> authorities,
                            OidcIdToken idToken, OidcUserInfo userInfo) {
        super(authorities, idToken, userInfo);
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }
}
```

### Teenus

```java
package ee.toolrental.infrastructure.security;

@Service
@RequiredArgsConstructor
public class AppUserOidcService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final int ROLE_CUSTOMER_ID = 2;
    private static final String STATUS_BLOCKED = "B";

    private final OidcUserService delegate = new OidcUserService();
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser googleUser = delegate.loadUser(userRequest);

        AppUser appUser = appUserRepository.findByGoogleSub(googleUser.getSubject())
                .orElseGet(() -> createAppUser(googleUser));

        if (STATUS_BLOCKED.equals(appUser.getStatus())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("user_blocked"), "Kasutaja on blokeeritud");
        }

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().getRoleName()));

        return new AppUserPrincipal(appUser.getId(), authorities, googleUser.getIdToken(), googleUser.getUserInfo());
    }

    private AppUser createAppUser(OidcUser googleUser) {
        AppUser appUser = new AppUser();
        appUser.setGoogleSub(googleUser.getSubject());
        appUser.setFirstName(googleUser.getGivenName());
        appUser.setLastName(Objects.requireNonNullElse(googleUser.getFamilyName(), ""));
        appUser.setRole(roleRepository.getReferenceById(ROLE_CUSTOMER_ID));
        appUser.setStatus("A");
        return appUserRepository.save(appUser);
    }
}
```

Märkused:

- `family_name` võib Google'is puududa (näiteks ühe nimega kontodel), aga `last_name` on `NOT NULL`. Seepärast kasutame vaikimisi tühja stringi.
- `role_id` väljal pole andmebaasis vaikeväärtust, seega määrab rolli kood.
- `AppUserRepository` vajab meetodit `Optional<AppUser> findByGoogleSub(String googleSub)`.

## 5. Turvaseadistus (`SecurityConfig`)

```java
package ee.toolrental.infrastructure.security;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String FRONTEND_URL = "http://localhost:5173/";

    private final AppUserOidcService appUserOidcService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/tools/**", "/api/categories/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("admin")
                .anyRequest().authenticated())
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(appUserOidcService))
                .defaultSuccessUrl(FRONTEND_URL, true)
                .failureUrl(FRONTEND_URL + "?loginError"))
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .logout(logout -> logout.logoutSuccessUrl(FRONTEND_URL))
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
```

Selgitused:

- **`defaultSuccessUrl(FRONTEND_URL, true)`**: pärast sisselogimist suunatakse kasutaja tagasi Vue rakendusse, mitte backendi juurlehele.
- **`HttpStatusEntryPoint(UNAUTHORIZED)`**: kui sisse logimata kasutaja teeb API päringu, vastab backend koodiga `401`, mitte ei suuna teda Google'i lehele. SPA jaoks on see vajalik, sest axios ei oska ümbersuunamist Google'i lehele kasutada. Frontend näeb `401` vastust ja näitab sisselogimisnuppu.
- **`csrf.disable()`**: CSRF-kaitse blokeeriks vaikimisi Vue POST/PUT/DELETE päringud. Õppeprojektis on lihtsaim see välja lülitada. **Tootmises** tuleks CSRF alles jätta ja kasutada `CookieCsrfTokenRepository`t koos `X-XSRF-TOKEN` päisega.
- `permitAll` teed on näited. Kohanda need vastavalt tegelikele endpointidele.

## 6. Vite proxy (`frontend/vite.config.js`)

Praegu suunab proxy backendi ainult `/api` päringud. Sisselogimiseks on vaja suunata ka OAuth-i ja väljalogimise teed:

```js
proxy: {
  '/api': 'http://localhost:8080',
  '/oauth2': 'http://localhost:8080',
  '/login/oauth2': 'http://localhost:8080',
  '/logout': 'http://localhost:8080',
}
```

Nii näeb brauser kõiki päringuid aadressilt `localhost:5173`. Sessiooniküpsis jääb samale originile ja CORS-i pole vaja seadistada.

**Ära lisa proxyle `changeOrigin: true`.** Spring koostab redirect URI päringu `Host` päise järgi. Päis peab jääma `localhost:5173`, et redirect URI ühtiks Google Console'is registreeritud aadressiga.

## 7. `/api/me` endpoint

Frontend peab teadma, kes on sisse logitud ja kas tal on profiil olemas:

```java
@GetMapping("/api/me")
public CurrentUserDto getCurrentUser(@AuthenticationPrincipal AppUserPrincipal principal) {
    return appUserService.getCurrentUser(principal.getUserId(), principal.getEmail());
}
```

```java
public record CurrentUserDto(
        Integer userId,
        String firstName,
        String lastName,
        String roleName,
        String email,        // profiilist või, kui profiili pole, Google'ist (vormi eeltäitmiseks)
        boolean hasProfile
) {}
```

Teistes controllerites saab sisse logitud kasutaja ID kätte samamoodi:

```java
@PostMapping("/api/tools")
public void addTool(@AuthenticationPrincipal AppUserPrincipal principal, @RequestBody @Valid ToolDto toolDto) {
    toolService.addTool(principal.getUserId(), toolDto);
}
```

**Kasutaja ID-d ei tohi võtta frontendist** (URL-ist ega päringu kehast). See peab alati tulema sessioonist.

## 8. Frontend

- **Sisselogimisnupp** peab olema tavaline link, mitte axiose päring, sest brauser peab tegema täieliku lehe ümbersuunamise:
  ```html
  <a href="/oauth2/authorization/google">Logi sisse Google'iga</a>
  ```
- **Rakenduse käivitumisel** küsi `GET /api/me`:
  - `401` → kasutaja pole sisse logitud;
  - `hasProfile: false` → suuna profiili täitmise vormile (e-post on seal eeltäidetud);
  - `hasProfile: true` → tavaline vaade.
- **Väljalogimine:** `POST /logout` (näiteks vormi või axiose kaudu), seejärel tühjenda frontendi kasutaja olek.

## 9. Testimine demokasutajatega

`3_import.sql` failis on demokasutajatel võltsväärtused (`demo-marko-tamm`, `demo-liis-kask`), seega nendena Google'iga sisse logida ei saa. Kui logid esimest korda oma Google'i kontoga sisse, tekib sulle uus `customer` rollis kasutaja.

Kui tahad logida sisse demokasutajana (näiteks admin Marko), vaata pärast esimest sisselogimist oma päris `sub` väärtus tabelist `app_user` ja seo see demokasutajaga:

```sql
DELETE FROM app_user WHERE google_sub = '<sinu-sub>' AND id NOT IN (1, 3);
UPDATE app_user SET google_sub = '<sinu-sub>' WHERE id = 1;
```

## Muud olulised asjad

- **Tootmises kasuta HTTPS-i.** Google lubab `http`-i ainult `localhost`-i puhul.
- **Avalikuks kasutamiseks** tuleb consent screen avaldada („Publish app“), vastasel juhul pääsevad sisse ainult testkasutajad.
- **Ainult oma firma kontode lubamiseks** kontrolli `AppUserOidcService`-s tokeni `hd` väärtust (Google Workspace'i domeen).
