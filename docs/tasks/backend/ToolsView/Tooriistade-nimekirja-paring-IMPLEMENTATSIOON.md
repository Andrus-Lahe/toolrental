# Tööriistade nimekirja päring — implementatsiooni plaan

**Seotud task:** `docs/tasks/backend/ToolsView/Tooriistade-nimekirja-paring.md`

**Teenus:** `GET /api/tools`

Kõik allolevad teed on antud repositooriumi juurkausta suhtes. Baaspakett on `ee.toolrental`, andmebaasi skeem on `tool_rental`.

## Hetkeseis (mis on juba olemas)

**Persistence (taaskasutatav, muudatust ei vaja):**

- `backend/src/main/java/ee/toolrental/persistence/appuser/AppUser.java` — `app_user` entiteet.
- `backend/src/main/java/ee/toolrental/persistence/profile/Profile.java` — `profile` entiteet, `@OneToOne AppUser user`, `@ManyToOne Location location`.
- `backend/src/main/java/ee/toolrental/persistence/location/Location.java` — `location` entiteet, `@ManyToOne District district`.
- `backend/src/main/java/ee/toolrental/persistence/district/District.java` — `district` entiteet, `@ManyToOne City city`, väli `districtName`.
- `backend/src/main/java/ee/toolrental/persistence/city/City.java` — `city` entiteet, väli `cityName`.
- `backend/src/main/java/ee/toolrental/persistence/category/Category.java` — `category` entiteet.
- Vastavad repositooriumid (`AppUserRepository`, `ProfileRepository`, `LocationRepository`, `DistrictRepository`, `CityRepository`, `CategoryRepository`, `RoleRepository`) on olemas — vajalikud testandmete loomiseks.

**Infrastruktuur:**

- `backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` — käsitleb `InternalServerErrorException` (500), `handleTypeMismatch` (400 `"<param>: peab olema Integer-tüüpi täisarv"`), `@Valid` body väljavigu ja üldist `Exception` (500 üldteatega).
- `backend/src/main/java/ee/toolrental/infrastructure/exception/InternalServerErrorException.java` — 500, `errorCode = "INTERNAL_SERVER_ERROR"`, sõnum antakse konstruktoris.
- `backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java` — `message` + `errorCode` (String).
- `backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java` — sisaldab juba `requestMatchers(HttpMethod.GET, "/api/tools/**", ...).permitAll()`. `PathPattern` `/api/tools/**` sobitub ka `/api/tools`-iga, seega **turvaseadistust muuta pole vaja**.

**Eeskujuks sobivad valmis teenused:**

- `DistrictController` + `DistrictService.getDistricts()` — `DataAccessException` → `InternalServerErrorException` muster, `@Transactional(readOnly = true)`, Swaggeri `@ApiResponse` annotatsioonid koos `ApiError` skeemiga.
- `CategoryService.getCategoriesInfo()` — `byte[]` → Base64 (`Base64.getEncoder().encodeToString(...)`).
- `backend/src/main/java/ee/toolrental/persistence/ai/AvailableToolRepository.java` — sama LEFT JOIN ahel (`tool → profile → location → district → city`) natiivses SQL-is AI-mooduli jaoks. Selle taski jaoks seda **ei taaskasutata** (JdbcTemplate, `LIMIT 20`, nime järgi filtrid, ainult `status='A'`), aga JOIN-loogika on sama.
- Testid: `DistrictControllerTest` (standalone MockMvc + `RestExceptionHandler`), `DistrictServiceTest`, `DistrictRepositoryTest` (`@DataJpaTest` kohaliku PostgreSQL-i ja impordiandmetega, rollback), `DistrictPublicAccessTest`.

**Puudub täielikult:**

- `tool` ja `tool_image` JPA entiteedid ning nende repositooriumid.
- `ToolsResponse`, `ToolListItemDto`, `ToolMapper`, `ToolService`, `ToolController`.
- 400 vea erindiklass, mida service saaks visata (`INCORRECT_INPUT` tekib praegu ainult Springi enda binding-vigadest).

## Puuduv/muudetav

| # | Fail | Tegevus |
|---|---|---|
| 1 | `persistence/tool/Tool.java` | uus entiteet |
| 2 | `persistence/toolimage/ToolImage.java` | uus entiteet |
| 3 | `persistence/tool/ToolListRow.java` | uus projektsiooni-record (päringu rida) |
| 4 | `persistence/tool/ToolRepository.java` | uus, JPQL `Page` päring + countQuery |
| 5 | `persistence/toolimage/ToolImageRepository.java` | uus, tühi `JpaRepository` (testandmete jaoks) |
| 6 | `controller/tool/dto/ToolListItemDto.java`, `ToolsResponse.java` | uued DTO-d |
| 7 | `persistence/tool/ToolMapper.java` | uus MapStruct mapper |
| 8 | `infrastructure/exception/IncorrectInputException.java` | uus erind (400) |
| 9 | `infrastructure/RestExceptionHandler.java` | lisa handler uuele erindile |
| 10 | `service/ToolService.java` | uus |
| 11 | `controller/tool/ToolController.java` | uus |
| 12 | testid | uued (vt jaotist „Testid“) |
| 0 | `backend/src/test/java/ee/toolrental/controller/district/DistrictPublicAccessTest.java` | paranda, et kogu testikomplekt kompileeruks |

SQL skeemi ega `3_import.sql` faili ei muudeta.

## Sammud

### Ettevalmistus

0. **Paranda `DistrictPublicAccessTest`** — fail: `backend/src/test/java/ee/toolrental/controller/district/DistrictPublicAccessTest.java`
   - Test impordib `DevSecurityConfig`-i, mis on (ka commititud versioonis) välja kommenteeritud — testid ei kompileeru ja `./gradlew test` ei käivitu.
   - Vii test `CategoryPublicAccessTest` mustrile: eemalda `@Import(DevSecurityConfig.class)`, `@ActiveProfiles("dev-no-auth")`, `@EnableWebSecurity`; lisa `@Import(SecurityConfig.class)`, `@TestPropertySource` (google `client-id`/`client-secret` test-väärtused) ja `@MockitoBean AppUserOidcService appUserOidcService`. `DevSecurityConfig` jääb puutumata.
   - Käivita `./gradlew test` ja veendu, et olemasolev komplekt läbib, enne kui uut koodi lisad.

### Persistence

1. **Loo `Tool` entiteet** — fail: `backend/src/main/java/ee/toolrental/persistence/tool/Tool.java`
   - Muster nagu `Profile`/`District`: `@Getter @Setter @Entity @Table(name = "tool", schema = "tool_rental")`.
   - Väljad: `Integer id`; `@ManyToOne(fetch = LAZY, optional = false) AppUser owner` (`owner_id`); `@ManyToOne(fetch = LAZY, optional = false) Category category` (`category_id`); `String name` (`@Size(max = 150)`); `String description` (`@Size(max = 2000)`, nullable); `String status` (`@Size(max = 1)`, `@ColumnDefault("'A'")`, nagu `AppUser.status`); `Instant createdAt`, `Instant updatedAt` (`@ColumnDefault("CURRENT_TIMESTAMP")`, nagu `Profile`).
   - Ühtegi `cascade` seost ei lisata.

2. **Loo `ToolImage` entiteet** — fail: `backend/src/main/java/ee/toolrental/persistence/toolimage/ToolImage.java`
   - `@Table(name = "tool_image", schema = "tool_rental")`; väljad `Integer id`, `@ManyToOne(fetch = LAZY, optional = false) Tool tool` (`tool_id`), `byte[] imageData` (`image_data`, nagu `CategoryImage`), `Boolean isMain` (`is_main`, `@ColumnDefault("false")`).

3. **Loo päringurea record** — fail: `backend/src/main/java/ee/toolrental/persistence/tool/ToolListRow.java`
   - `public record ToolListRow(Integer toolId, String toolName, String description, byte[] imageData, String status, String cityName, String districtName) {}`
   - Põhjus: JPQL konstruktori-avaldis vajab sihtklassi; `imageData` tuleb baasist `byte[]`-na ja Base64 teisendus tehakse mapperis, mitte SQL-is. Sama muster nagu `persistence/ai/AvailableTool.java`.

4. **Loo `ToolRepository`** — fail: `backend/src/main/java/ee/toolrental/persistence/tool/ToolRepository.java`
   - `public interface ToolRepository extends JpaRepository<Tool, Integer>`
   - Üks meetod, nimi mainib tagastatavat subjekti (backend/CLAUDE.md):

   ```java
   @Query(value = """
           select new ee.toolrental.persistence.tool.ToolListRow(
               t.id, t.name, t.description, ti.imageData, t.status, c.cityName, d.districtName)
           from Tool t
           left join Profile p on p.user = t.owner
           left join p.location l
           left join l.district d
           left join d.city c
           left join ToolImage ti on ti.tool = t and ti.isMain = true
           where (:categoryId = 0 or t.category.id = :categoryId)
             and (:cityId = 0 or c.id = :cityId)
             and (:districtId = 0 or d.id = :districtId)
             and (:status = '0' or t.status = :status)
           order by t.id asc
           """,
           countQuery = """
           select count(t)
           from Tool t
           left join Profile p on p.user = t.owner
           left join p.location l
           left join l.district d
           left join d.city c
           where (:categoryId = 0 or t.category.id = :categoryId)
             and (:cityId = 0 or c.id = :cityId)
             and (:districtId = 0 or d.id = :districtId)
             and (:status = '0' or t.status = :status)
           """)
   Page<ToolListRow> findToolListRowsBy(Integer categoryId, Integer cityId, Integer districtId, String status, Pageable pageable);
   ```

   - Kõik JOIN-id on `left join` — profiilita omanik ja pildita tööriist säilivad (filtrita). Positiivse `cityId`/`districtId` korral on `c.id`/`d.id` NULL ja tingimus ei sobitu — täpselt nagu task nõuab.
   - Dubleerimist ei teki: `profile.user_id` on UNIQUE ja `tool_image_one_main_unique` lubab ühe põhipildi tööriista kohta. `countQuery` ei liida pilte üldse juurde, seega `totalElements` loendab ainult tööriistu.
   - `0` / `'0'` on „filter puudub“ sentinel (vältib PostgreSQL-i `:param is null` tüübituvastuse probleeme).
   - Järjestus on päringus endas (`order by t.id asc`), `Pageable` luuakse ilma `Sort`-ita.

5. **Loo `ToolImageRepository`** — fail: `backend/src/main/java/ee/toolrental/persistence/toolimage/ToolImageRepository.java`
   - `public interface ToolImageRepository extends JpaRepository<ToolImage, Integer> {}` — põhikood seda ei kasuta, aga repositooriumi test vajab pildita/mitme pildiga stsenaariumide loomiseks `saveAndFlush`-i.

### DTO ja mapper

6. **Loo `ToolListItemDto`** — fail: `backend/src/main/java/ee/toolrental/controller/tool/dto/ToolListItemDto.java`
   - Muster nagu `CategoryDetailedInfoDto`: `@Data @AllArgsConstructor @NoArgsConstructor implements Serializable`.
   - Väljad: `Integer toolId`, `String toolName`, `String description` (tööriista kirjeldus `tool.description`), `String imageData`, `String status`, `String cityName`, `String districtName`.
   - Ainult selle ressursi DTO → pakett `controller/tool/dto/`, mitte `common/dto/`.

7. **Loo `ToolsResponse`** — fail: `backend/src/main/java/ee/toolrental/controller/tool/dto/ToolsResponse.java`
   - Väljad: `Integer pageNumber`, `Integer pageSize`, `Integer totalPages`, `Long totalElements`, `List<ToolListItemDto> tools`.
   - NULL-välju ei eemaldata — `@JsonInclude(NON_NULL)` **ei** lisata (Jacksoni vaikekäitumine väljastab `null`-id).

8. **Loo `ToolMapper`** — fail: `backend/src/main/java/ee/toolrental/persistence/tool/ToolMapper.java`
   - Muster nagu `DistrictMapper` (`@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)`).

   ```java
   ToolListItemDto toToolListItemDto(ToolListRow toolListRow);   // väljanimed ühtivad
   List<ToolListItemDto> toToolListItemDtos(List<ToolListRow> toolListRows);

   default String toBase64(byte[] imageData) {
       return imageData == null ? null : Base64.getEncoder().encodeToString(imageData);
   }
   ```

   - MapStruct kasutab `byte[] → String` teisenduseks automaatselt `default` meetodit. NULL pilt jääb `null`-iks.
   - `ToolsResponse` koostab service (lehekülje metaandmed ei ole mapperi ülesanne).

### Infrastruktuur

9. **Loo `IncorrectInputException`** — fail: `backend/src/main/java/ee/toolrental/infrastructure/exception/IncorrectInputException.java`
   - Muster nagu `InternalServerErrorException`: `@Getter`, väljad `message` ja `errorCode`; konstruktor `IncorrectInputException(String message)` seab `errorCode = "INCORRECT_INPUT"`.

10. **Lisa handler** — fail: `backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
    - Uus `@ExceptionHandler` meetod `handleIncorrectInputException(IncorrectInputException exception)` → `ApiError` + `HttpStatus.BAD_REQUEST`, sama kuju nagu teistel handleritel.

### Service

11. **Loo `ToolService`** — fail: `backend/src/main/java/ee/toolrental/service/ToolService.java`
    - `@RequiredArgsConstructor @Service`; sõltuvused `ToolRepository toolRepository`, `ToolMapper toolMapper`.
    - Konstant `TOOLS_LOADING_FAILED = "Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti."` (nagu `DistrictService`).
    - Avalik meetod (`@Transactional(readOnly = true)`):

    ```java
    public ToolsResponse getTools(String categoryId, String cityId, String districtId,
                                  String status, String pageNumber, String pageSize)
    ```

    Järjekord meetodi sees:
    1. **Parsi ja valideeri** (vaikeväärtus ainult siis, kui parameeter on `null` ehk puudub):
       - `Integer validCategoryId = getValidIdFilter("categoryId", categoryId);` — `null` → `0`; mitte-täisarv / tühi / Integer-ülene → `IncorrectInputException("categoryId: peab olema Integer-tüüpi täisarv")`; `< 0` → `"categoryId: peab olema 0 või positiivne täisarv"`. Sama `cityId`, `districtId` jaoks.
       - `Integer validPageNumber = getValidPageValue("pageNumber", pageNumber, 1);` — `null` → vaikeväärtus; parsimisviga → Integer-tüübi teade; `< 1` → `"pageNumber: peab olema vähemalt 1"`. Sama `pageSize` (vaikimisi `12`).
       - `String validStatus = getValidStatus(status);` — `null` → `"A"`; lubatud täpselt `A`, `U`, `0`; muu (sh `""`) → `"status: lubatud väärtused on A, U ja 0"`.
       - Parsimiseks `Integer.parseInt(value)` + `catch (NumberFormatException)` — see katab ühe korraga tühja stringi, mitte-arvu ja Integer-vahemikust välja jääva väärtuse.
    2. **Päring** `try { ... } catch (DataAccessException e) { throw new InternalServerErrorException(TOOLS_LOADING_FAILED); }`:
       - `Pageable pageable = PageRequest.of(validPageNumber - 1, validPageSize);` (API 1-põhine → Spring 0-põhine).
       - `Page<ToolListRow> toolListRowPage = toolRepository.findToolListRowsBy(validCategoryId, validCityId, validDistrictId, validStatus, pageable);`
    3. **Vastus**: `List<ToolListItemDto> toolListItemDtos = toolMapper.toToolListItemDtos(toolListRowPage.getContent());` ja `new ToolsResponse(validPageNumber, validPageSize, toolListRowPage.getTotalPages(), toolListRowPage.getTotalElements(), toolListItemDtos)`.
       - Spring Data `Page` annab üle viimase lehe tühja `content`-i, kuid õiged `totalElements`/`totalPages` (countQuery käivitatakse alati, kui leht pole esimene ega mittetäis) ning tühja tulemuse korral `totalPages = 0` — vastab taskile.
    - Abimeetodid (`getValidIdFilter`, `getValidPageValue`, `getValidStatus`) on `private`, paigutatakse `getTools` alla väljakutsumise järjekorras. `getValid…` prefiks sobib, sest meetod tagastab alati kindla väärtuse või viskab erindi.
    - **Ülisuure nihke kaitse:** Spring Data ei kannata nihet `> Integer.MAX_VALUE` (nt `pageNumber=200000000&pageSize=12`) ja viskab `InvalidDataAccessApiUsageException`-i, mis muutuks 500-ks. Kontrolli `(long) (validPageNumber - 1) * validPageSize > Integer.MAX_VALUE`; sellisel juhul kutsu sama `findToolListRowsBy` päringut `PageRequest.of(0, validPageSize)`-ga, võta sealt `totalElements` ja `totalPages` ning tagasta `tools: []` ja päringu `pageNumber`. Eraldi count-meetodit ega 400 viga ei lisata.
    - Teenus ainult loeb; `save`/`delete` kutseid pole.

### Controller

12. **Loo `ToolController`** — fail: `backend/src/main/java/ee/toolrental/controller/tool/ToolController.java`
    - Muster nagu `DistrictController`: `@RestController @RequiredArgsConstructor @RequestMapping("/api")`.

    ```java
    @GetMapping("/tools")
    @Operation(summary = "Leiab tööriistade nimekirja filtrite ja lehekülgjaotusega",
            description = "Filtrid ühendatakse AND-tingimusega, tulemused järjestuses tool.id ASC. Lehenumber algab ühest.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Vigane päringuparameeter (INCORRECT_INPUT)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "500", description = "Tööriistade laadimine ebaõnnestus (INTERNAL_SERVER_ERROR)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ToolsResponse getTools(@RequestParam(required = false) String categoryId,
                                  @RequestParam(required = false) String cityId,
                                  @RequestParam(required = false) String districtId,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String pageNumber,
                                  @RequestParam(required = false) String pageSize) {
        return toolService.getTools(categoryId, cityId, districtId, status, pageNumber, pageSize);
    }
    ```

    - **Miks `String`, mitte `Integer` + `defaultValue`:** Spring asendab selgelt saadetud tühja väärtuse (`?pageNumber=`) `defaultValue`-ga ja ilma `defaultValue`-ta teisendab `""` → `null`. Mõlemal juhul on tühi ja puuduv parameeter eristamatud, aga task nõuab tühjale väärtusele 400 (kasutajaga kinnitatud).
    - Swaggeris võib lisada `@Parameter(description = ..., example = ...)` iga parameetri juurde, et dokumentatsioonis oleks näha tegelik tüüp ja vaikeväärtus.

13. **SecurityConfig** — muudatust ei vaja (vt „Hetkeseis“).

## Veakäsitlus

| Olukord | Kus tekib | Erind | Vastus |
|---|---|---|---|
| Tühi / mitte-täisarv / Integer-ülene arvuparameeter | `ToolService` parsimine | `IncorrectInputException("<param>: peab olema Integer-tüüpi täisarv")` | 400 `INCORRECT_INPUT` |
| `pageNumber < 1` / `pageSize < 1` | `ToolService` | `IncorrectInputException("<param>: peab olema vähemalt 1")` | 400 |
| Negatiivne ID-filter | `ToolService` | `IncorrectInputException("<param>: peab olema 0 või positiivne täisarv")` | 400 |
| `status` ∉ {`A`,`U`,`0`}, sh `""` | `ToolService` | `IncorrectInputException("status: lubatud väärtused on A, U ja 0")` | 400 |
| Andmebaasi tõrge | `ToolService` `catch (DataAccessException)` | `InternalServerErrorException(TOOLS_LOADING_FAILED)` | 500 `INTERNAL_SERVER_ERROR` |
| Olematu positiivne ID, vastuolulised linn/linnaosa, tühi tulemus, üle viimase lehe | — | erindit ei visata | 200 tühja `tools` loendiga |

- Valideerimine toimub **enne** `try` plokki, et `IncorrectInputException` ei satuks 500 püüdmisse.
- `PrimaryKeyNotFoundException`/`getValid<Entiteet>By` ei kasutata — task keelab filtrite puhul 404.
- Kõik veateated on taski tabelist sõna-sõnalt. `RestExceptionHandler.handleException` (üldine 500) jääb varuvariandiks; SQL-i ega stack trace'i vastusesse ei lähe.

## Testid

1. **`backend/src/test/java/ee/toolrental/service/ToolServiceTest.java`** (ühiktest, Mockito, nagu `DistrictServiceTest`)
   - Kõik parameetrid `null` → repositooriumi kutsutakse `(0, 0, 0, "A", PageRequest.of(0, 12))`.
   - `pageNumber="2"` → `PageRequest.of(1, 12)`; vastuses `pageNumber=2`.
   - Iga 400-juhtum: `""`, `"abc"`, `"1.5"`, `"99999999999"`, `"-1"` ID-filtritele, `"0"` lehenumbrile/suurusele, `status` = `""`, `"a"`, `"X"` — kontrolli täpset sõnumit ja et repositooriumi **ei kutsuta**.
   - `status="0"` ja `status="U"` antakse repositooriumile edasi muutmata.
   - `DataAccessException` repositooriumist → `InternalServerErrorException` õige sõnumiga.
   - Tühi `Page` → `totalElements=0`, `totalPages=0`, `tools=[]`.
   - Ülisuur nihe (`pageNumber="200000000"`) → repositooriumi kutsutakse `PageRequest.of(0, 12)`-ga, vastuses `tools=[]`, koguarvud sellest päringust, `pageNumber=200000000`.

2. **`backend/src/test/java/ee/toolrental/controller/tool/ToolControllerTest.java`** (standalone MockMvc + `RestExceptionHandler`, nagu `DistrictControllerTest`)
   - 200 JSON kuju: metaandmed + `tools[0]` täpselt seitse välja; `null` väljad (`description`, `imageData`, `cityName`, `districtName`) on JSON-is olemas (`jsonPath(...).value(nullValue())` + `.exists()`).
   - Parameetrid jõuavad service'isse stringidena muutmata (puuduv → `null`, tühi → `""`).
   - `IncorrectInputException` → 400 `INCORRECT_INPUT` õige sõnumiga; `InternalServerErrorException` → 500.

3. **`backend/src/test/java/ee/toolrental/controller/tool/ToolPublicAccessTest.java`** — anonüümne `GET /api/tools` → 200; muster nagu `CategoryPublicAccessTest` (päris `SecurityConfig` + `@MockitoBean AppUserOidcService` + OAuth test-property'd).

4. **`backend/src/test/java/ee/toolrental/persistence/tool/ToolRepositoryTest.java`** (`@DataJpaTest(properties = "spring.sql.init.mode=never")` + `@AutoConfigureTestDatabase(replace = NONE)`, kohalik `vali_it` + impordiandmed, rollback — nagu `DistrictRepositoryTest`)
   - Taski kontrolltabel 1:1 (`PageRequest.of(0, 12)`): vaikimisi `A` → 7 / 1 leht / `1,3,4,5,6,7,8`; `status=0` → 8 / 1 leht; `U` → `[2]`; `categoryId=2&A` → `[1]`; `cityId=1&districtId=2&A` → `[3,4,8]`; `cityId=2&0` → tühi.
   - Lehekülgjaotus väikese lehega (`pageSize=6`): `A` leht 1 → `1,3,4,5,6,7`, leht 2 → ainult `8`, `totalPages=2`.
   - Vastuoluline `cityId=2&districtId=1` ja olematu `categoryId=999` → tühi.
   - Üle viimase lehe (`PageRequest.of(1, 12)`) → tühi `content`, `totalElements=7`, `totalPages=1`.
   - Tööriista 1 `imageData` baidid == `tool_image.id=1` baidid.
   - **Testis loodud andmetega** (impordifaili ei muudeta, rollback): (a) tööriistale lisapilt `is_main=false` → tööriist ei kordu, `totalElements` ei kasva, pilt on endiselt põhipilt; (b) uus tööriist ainult lisapildiga → `imageData = null`; (c) uus `AppUser` ilma profiilita + tema tööriist `description = null` → filtrita tulemuses olemas, `cityName`/`districtName` `null`; `cityId=1` korral puudub.

Käivita: `./gradlew test --tests "ee.toolrental.*Tool*"` ja seejärel kogu `./gradlew test` (WSL-ist vt backend/CLAUDE.md „Käivitamine WSL-ist“).

## Otsused (kasutajaga kinnitatud)

1. Vastuse väli on **`description`** (tööriista kirjeldus), mitte `categoryDescription` — task-fail parandatud.
2. Query-parameetrid võetakse kontrolleris vastu **`String`-ina** ja parsitakse `ToolService`-s, et tühi väärtus annaks 400.
3. Vaikimisi **`pageSize = 12`**, ülempiiri pole. Viimasest lehest suurem `pageNumber` (sh ülisuur) → 200, `tools: []`, tegelikud koguarvud.
4. `DistrictPublicAccessTest` viiakse `CategoryPublicAccessTest` mustrile, et kogu testikomplekt käivituks; `DevSecurityConfig` jääb puutumata.
5. backend/CLAUDE.md mainib `ErrorResponse` enumi, mida koodis pole. Otsus: järgitakse olemasolevat koodi (String `errorCode` erindiklassis, teated service'i konstantidena); enumi ei looda ja CLAUDE.md-d ei muudeta.
