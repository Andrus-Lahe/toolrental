# Tööriista detailide päring — implementatsiooni plaan

**Seotud task:** [Tooriista-detailide-paring.md](./Tooriista-detailide-paring.md)

**Teenus:** `GET /api/tools/{toolId}` (avalik)

## Hetkeseis (mis on juba olemas)

**Persistence:**

- `backend/src/main/java/ee/toolrental/persistence/tool/Tool.java` — entiteet: `id`, `owner` (`AppUser`, LAZY), `category` (`Category`, LAZY), `name`, `description`, `status`.
- `backend/src/main/java/ee/toolrental/persistence/category/Category.java` — väli `categoryName`.
- `backend/src/main/java/ee/toolrental/persistence/toolimage/ToolImage.java` — `tool`, `imageData` (`byte[]`, bytea), `main`.
- `backend/src/main/java/ee/toolrental/persistence/tool/ToolRepository.java` — `JpaRepository<Tool, Integer>` (`findById` olemas). Lisaks `findToolForBookingBy` (**PESSIMISTIC_WRITE lukuga**, ainult broneerimiseks), `findAvailableOwnerToolsBy`, `findToolListRowsBy`.
- `backend/src/main/java/ee/toolrental/persistence/toolimage/ToolImageRepository.java` — ainult `findMainToolImagesByToolIds(Collection<Integer>)`. **Ühe tööriista põhipildi päring puudub.**
- `backend/src/main/java/ee/toolrental/persistence/tool/ToolMapper.java` — `toTool`, `toMyToolCardDto(Tool, byte[])`, `toToolListItemDto`, `default String toBase64(byte[])`. **Detaili mapping puudub**; `toBase64` on taaskasutatav.

**Service:**

- `backend/src/main/java/ee/toolrental/service/ToolService.java` — `createTool`, `getTools`; sõltuvused `ToolRepository`, `ToolImageRepository`, `ToolMapper` on juba olemas (konstruktorit muuta pole vaja). **`getValidToolBy` ja detaili meetod puuduvad.**
- `backend/src/main/java/ee/toolrental/service/BookingService.java:150` — `getValidToolBy(Integer)` on olemas, kuid kasutab lukustavat `findToolForBookingBy` päringut → lugemiseks **ei sobi**.

**Controller:**

- `backend/src/main/java/ee/toolrental/controller/tool/ToolController.java` — ainult `POST /api/tools`. **`GET /api/tools/{toolId}` puudub.**
- `backend/src/main/java/ee/toolrental/controller/tool/ToosListController.java` — `GET /api/tools` (nimekiri).
- `ToolDetailResponse` DTO **puudub**.

**Infrastructure:**

- `PrimaryKeyNotFoundException("toolId", toolId)` → 404 `PRIMARY_KEY_NOT_FOUND`, teade `Ei leidnud primary keyd 'toolId' väärtusega: 123` — täpselt taski kuju.
- `RestExceptionHandler.handleTypeMismatch` **käsitleb juba** path variable'i tüübiviga: `/api/tools/abc` → 400 `INCORRECT_INPUT`, `toolId: peab olema Integer-tüüpi täisarv`. Taski väide „praegune `RestExceptionHandler` ei käsitle“ on aegunud.
- `InternalServerErrorException` → 500 `INTERNAL_SERVER_ERROR` (sama muster nagu `getTools`).
- `SecurityConfig`: `GET /api/tools/**` on juba `permitAll()` — muudatust pole vaja.

**Testide eeskujud:** `ToolListControllerTest` (standalone MockMvc + `RestExceptionHandler`), `ToolPublicAccessTest` (`@WebMvcTest(ToolController.class)` + `SecurityConfig`), `ToolListServiceTest` (mockitud repo + päris `Mappers.getMapper`), `ToolRepositoryTest` (`@DataJpaTest` kohaliku `vali_it` andmebaasiga).

**Frontend sõltuvus:** `frontend/src/api-services/ToolService.js` `sendGetToolDetailsRequest(toolId)` ja `BookingFormView.vue` kasutavad juba seda endpointi (`toolName`, `ownerId`, `status`).

## Merge-konfliktide analüüs (teised harud)

- `git fetch` järel ükski remote haru (`ANDRUS-*`, `JAROSLAV-AI-MODULE`, `KERSTI`) pole `origin/master`-ist ees → avatud paralleeltööd backendi tool-failides pole.
- `KERSTI-BE-47-TOOLDETAILVIEW` on `origin/master`-ist **1 commit maas** — see muudab ainult `frontend/src/components/common/ImagePicker.vue`. Konflikti pole; soovitatav `git merge origin/master` enne alustamist.
- Tööpuus on endiselt CRLF-ainult muudatused (kasutaja parandab ise) — neid ei commitita. Uusi faile ega muudetud faile kirjutame LF-iga, nagu repositooriumis.
- Muudetavad jagatud failid: `ToolController.java`, `ToolService.java`, `ToolMapper.java`, `ToolImageRepository.java`, `ToolPublicAccessTest.java`. Lisa meetodid **olemasolevate järele**, olemasolevaid ei muuda → minimaalne konfliktirisk.
- `BookingService.getValidToolBy` jäetakse puutumata (broneeringu kood, teise tööga konfliktioht).

## Puuduv/muudetav

| Fail | Tegevus |
|---|---|
| `persistence/toolimage/ToolImageRepository.java` | Lisa `findMainToolImageBy(Integer toolId)`. |
| `controller/tool/dto/ToolDetailResponse.java` | Uus DTO. |
| `persistence/tool/ToolMapper.java` | Lisa `toToolDetailResponse(Tool tool, byte[] imageData)`. |
| `service/ToolService.java` | Lisa `getToolDetail(Integer toolId)` ja `getValidToolBy(Integer toolId)`. |
| `controller/tool/ToolController.java` | Lisa `GET /tools/{toolId}`. |
| Testid | Uued `ToolDetailControllerTest`, `ToolDetailServiceTest`, `ToolImageRepositoryTest`; täienda `ToolPublicAccessTest`. |

## Sammud

1. **Põhipildi päring** — fail: `backend/src/main/java/ee/toolrental/persistence/toolimage/ToolImageRepository.java`
   - Lisa olemasoleva meetodi järele JPQL päring (tööriistal on kuni üks põhipilt, `tool_image_one_main_unique`):

   ```java
   @Query("select i from ToolImage i where i.tool.id = :toolId and i.main = true")
   Optional<ToolImage> findMainToolImageBy(@Param("toolId") Integer toolId);
   ```

2. **Response DTO** — fail: `backend/src/main/java/ee/toolrental/controller/tool/dto/ToolDetailResponse.java`
   - Nimi mockupi järgi (`ToolDetailResponse`), sama stiil nagu `ToolsResponse`: `@Data @AllArgsConstructor @NoArgsConstructor implements Serializable`.
   - Väljad järjekorras: `Integer toolId`, `Integer ownerId`, `String toolName`, `String categoryName`, `String description`, `String imageData`, `String status` (välja nimi vt „Otsused“ p 1).
   - Kasutab ainult `tool` ressurss → kuulub `controller/tool/dto/`, mitte `controller/common/dto/`.

3. **Mapper** — fail: `backend/src/main/java/ee/toolrental/persistence/tool/ToolMapper.java`
   - Lisa `toToolListItemDtos` järele:

   ```java
   @Mapping(target = "toolId", source = "tool.id")
   @Mapping(target = "ownerId", source = "tool.owner.id")
   @Mapping(target = "toolName", source = "tool.name")
   @Mapping(target = "categoryName", source = "tool.category.categoryName")
   @Mapping(target = "description", source = "tool.description")
   @Mapping(target = "imageData", expression = "java(toBase64(imageData))")
   @Mapping(target = "status", source = "tool.status")
   ToolDetailResponse toToolDetailResponse(Tool tool, byte[] imageData);
   ```

   - `toBase64` kasutab `Base64.getEncoder()` ilma `data:` prefiksita (sama kuju nagu nimekirjas); **mitte** `StringBytesConverter`.

4. **Service** — fail: `backend/src/main/java/ee/toolrental/service/ToolService.java`
   - Konstant: `private static final String TOOL_LOADING_FAILED = "Tööriista laadimine ebaõnnestus. Palun proovi hiljem uuesti.";`
   - Lisa pärast `getTools` blokki (meetodid väljakutsumise järjekorras):

   ```java
   @Transactional(readOnly = true)
   public ToolDetailResponse getToolDetail(Integer toolId) {
       try {
           Tool tool = getValidToolBy(toolId);
           byte[] imageData = toolImageRepository.findMainToolImageBy(toolId)
                   .map(ToolImage::getImageData)
                   .orElse(null);
           return toolMapper.toToolDetailResponse(tool, imageData);
       } catch (DataAccessException exception) {
           log.error("Tööriista laadimine ebaõnnestus (toolId={})", toolId, exception);
           throw new InternalServerErrorException(TOOL_LOADING_FAILED);
       }
   }

   public Tool getValidToolBy(Integer toolId) {
       return toolRepository.findById(toolId)
               .orElseThrow(() -> new PrimaryKeyNotFoundException("toolId", toolId));
   }
   ```

   - `getValidToolBy` vastab backend/CLAUDE.md reeglile „Entiteedi otsing ID järgi“ (`findById` + `orElseThrow` service'is). Lukku ei kasutata — päring ainult loeb.
   - `@Transactional(readOnly = true)` tagab, et LAZY `owner` ja `category` laaditakse mapperis sama transaktsiooni sees.
   - `PrimaryKeyNotFoundException` ei ole `DataAccessException` → läbib `catch`-i muutmata (404).

5. **Controller** — fail: `backend/src/main/java/ee/toolrental/controller/tool/ToolController.java`
   - Lisa `createTool` järele:

   ```java
   @GetMapping("/tools/{toolId}")
   @Operation(summary = "Tööriista detailide päring",
           description = "Avalik. Tagastab tööriista andmed koos põhipildiga (is_main = true) Base64 kujul.")
   @ApiResponses(value = {
           @ApiResponse(responseCode = "200", description = "OK"),
           @ApiResponse(responseCode = "400", description = "toolId pole täisarv (INCORRECT_INPUT)"),
           @ApiResponse(responseCode = "404", description = "Tööriista ei leitud (PRIMARY_KEY_NOT_FOUND)"),
           @ApiResponse(responseCode = "500", description = "Tööriista laadimine ebaõnnestus")})
   public ToolDetailResponse getToolDetail(@PathVariable Integer toolId) {
       return toolService.getToolDetail(toolId);
   }
   ```

   - Controller ei sisalda loogikat; `@PathVariable Integer` tüübiviga → `handleTypeMismatch` → 400.

## Veakäsitlus

| Olukord | Kus | Kuidas |
|---|---|---|
| Olematu `toolId` | `ToolService.getValidToolBy` | `PrimaryKeyNotFoundException("toolId", toolId)` → `RestExceptionHandler` → 404 `PRIMARY_KEY_NOT_FOUND`. |
| `toolId` pole täisarv (`abc`, `1.5`, üle Integer piiri) | Spring path variable konversioon | `MethodArgumentTypeMismatchException` → olemasolev `handleTypeMismatch` → 400 `INCORRECT_INPUT`, `toolId: peab olema Integer-tüüpi täisarv`. Uut koodi pole vaja. |
| Andmebaasi viga | `ToolService.getToolDetail` | `catch (DataAccessException)` → logi + `InternalServerErrorException(TOOL_LOADING_FAILED)` → 500. SQL-i ega stack trace'i vastusesse ei lähe. |
| `status = 'U'` | — | Pole viga, 200. |
| Põhipilt puudub | — | `imageData: null` (Jackson ei jäta null-välju välja, vt `ToolListControllerTest`). |

## Testid

1. **`backend/src/test/java/ee/toolrental/controller/tool/ToolDetailControllerTest.java`** (uus; `ToolListControllerTest` mustris, standalone MockMvc + `RestExceptionHandler`)
   - 200: täpselt 7 välja (`$.length()` = 7), väärtused `toolId=1`, `ownerId=1`, `Akutrell`, `Ehitustööd`, `description`, `imageData`, `status=A`.
   - 200: `description` ja `imageData` `null` on JSON-is olemas (`nullValue()`).
   - 200: `status=U`.
   - 404: service viskab `PrimaryKeyNotFoundException("toolId", 123)` → `errorCode`, täpne `message`.
   - 400: `GET /api/tools/abc` → `toolId: peab olema Integer-tüüpi täisarv`; service'it ei kutsuta (`verifyNoInteractions`).
   - 500: `InternalServerErrorException` → `INTERNAL_SERVER_ERROR` + taski teade.

2. **`backend/src/test/java/ee/toolrental/controller/tool/ToolPublicAccessTest.java`** (täienda)
   - `guestCanReadToolDetail`: sisse logimata `GET /api/tools/1` → 200.

3. **`backend/src/test/java/ee/toolrental/service/ToolDetailServiceTest.java`** (uus; `ToolListServiceTest` mustris — mock repod, päris `Mappers.getMapper(ToolMapper.class)`)
   - Olemas tööriist + põhipilt → kõik väljad õiged, `imageData` dekodeerub täpselt algbaitideks.
   - Põhipilt puudub → `imageData == null`.
   - `description == null` → `null`.
   - `status = "U"` → tagastatakse.
   - `findById` tühi → `PrimaryKeyNotFoundException`, `toolImageRepository`-t ei kutsuta.
   - `findById` viskab `DataAccessResourceFailureException` → `InternalServerErrorException` õige teatega.
   - Ainult `findById`/`findMainToolImageBy` — `save*` meetodeid ei kutsuta (päring ei muuda andmeid).

4. **`backend/src/test/java/ee/toolrental/persistence/toolimage/ToolImageRepositoryTest.java`** (uus; `ToolRepositoryTest` mustris, `@DataJpaTest` + kohalik `vali_it` + `3_import.sql`)
   - `findMainToolImageBy(1)` → `tool_image.id = 1`, `main = true`.
   - Tööriist, millel on ainult `is_main = false` pilt või pilt puudub (luua testis, rollback) → `Optional.empty()`.

Käivitamine WSL-ist: vt backend/CLAUDE.md „Käivitamine WSL-ist“ (`JAVA_HOME`, `SPRING_DATASOURCE_URL` Windowsi hosti IP-ga). `./gradlew test --tests "ee.toolrental.*ToolDetail*"` ja `--tests "*ToolImageRepositoryTest"`.

## Otsused (kasutajaga kinnitatud)

1. **Kirjelduse välja nimi on `description`** (nagu ToolsView nimekirjas, taski JSON näites ja Balsamiq märkmetes). Backend taski väljade tabel ja vastuvõtu kriteerium on parandatud.
2. **400 käsitlust ei lisata** — olemasolev `handleTypeMismatch` katab selle. Parandatud ainult taski lause veaolukordade all.
3. **`BookingService.getValidToolBy`** jääb selles taskis muutmata; kasutaja tegeleb sellega hiljem ise.
