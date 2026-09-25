# Kategooria lisamine — implementatsiooni plaan

**Seotud task:** [Kategooria-lisamine.md](./Kategooria-lisamine.md)

**Teenus:** `POST /api/admin/categories`

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/PrimaryKeyNotFoundException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/DataNotFoundException.java`

Rakenduse pakett on ee.toolrental. Handler katab @Valid väljavead ning kohandatud 403/404; ApiError errorCode ja message on String. ForbiddenException konstruktor võtab (message, errorCode). Puuduvad controller/service/persistence klassid ja backend testid; valmis analoogset endpoint’i ei leitud. `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` sisaldab JPA, validation, MapStructi ja testisõltuvusi. `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` ning `3_import.sql` määravad skeemi ja näidisandmed. Teiste taskide olemasolu ei tähenda nende koodi olemasolu.

## Puuduv/muudetav

Vajalikud on allpool loetletud entiteedid/repositooriumid, DTO/mapper, AdminCategoryService, AdminCategoryController, täpne vealeping ja testid. Ühised klassid luuakse üks kord; enne teostamist kontrolli uuesti paralleelsete taskide tehtud muudatusi. SQL skeemi automaatset muutmist see plaan ei nõua.

## Sammud

1. **Persistence entiteedid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/category/Category.java`

Kaardista lähteülesande tabelid ja FK-d JPA-s; ära kasuta cascade REMOVE viiteandmete või jagatud seoste suhtes. FK mudelid, mida teised taskid juba loovad, taaskasuta.

2. **Repositooriumid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/category/CategoryRepository.java`

CategoryRepository.existsCategoryByCategoryName(String categoryName). Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/common/dto/CategoryRequestDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/category/CategoryMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/AdminCategoryService.java`

AdminCategoryService.addCategory(request): nimi NotBlank/Size100, description Size255 nullable, sequence NotNull Integer. Täpne tõstutundlik duplikaadikontroll, CATEGORY_UNAVAILABLE. Salvesta ainult category, mitte pilti. DB unikaalsusvõistlus tõlgi samaks veaks pärast rollback’i. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/admin/AdminCategoryController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `POST /api/admin/categories` ning lähteülesande 200 keha. Admin endpoint nõuab admin rolli; klient ei saa sessiooni actorId-d asendada. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/KategoorialisamineServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/admin/KategoorialisamineControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Kasutaja pole sisse logitud. | 401 Unauthorized | tühi (Spring Security) |
| Sisse logitud kasutaja roll pole `admin`. | 403 Forbidden | tühi (Spring Security) |
| Sama nimega kategooria on juba olemas (nt `"Aiatööd"`). | 403 Forbidden | `{"errorCode":"CATEGORY_UNAVAILABLE","message":"Sellise nimega kategooria on juba olemas"}` |
| `categoryName` puudub, on tühi või liiga pikk; `sequence` puudub; `description` on liiga pikk. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"<väli>: <valideerimise teade>"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kategooria lisamine ebaõnnestus. Palun proovi hiljem uuesti."}` |

- `CATEGORY_UNAVAILABLE` on uus kood. Seda visatakse olemasoleva `ForbiddenException` klassiga.
- 400 tuleb `@Valid` + `@NotBlank`/`@Size`/`@NotNull` kaudu olemasolevast `handleMethodArgumentNotValid` handlerist.
- Nime võrdlus on täpne (tõstutundlik), nagu andmebaasi `UNIQUE` piirang.

## Testid

Uus Talvetööd, olemasolev Aiatööd, null description, piirid, puuduva sequence viga, samaaegne duplikaat.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] `POST /api/admin/categories` on olemas ja kättesaadav ainult `admin` rollile.
- [ ] Näites toodud body annab 200 tühja body'ga ning `category` tabelis on uus rida sama nime, kirjelduse ja järjekorraga.
- [ ] `description: null` või puuduv `description` on lubatud.
- [ ] Olemasolev nimi (`"Aiatööd"`) annab 403 `CATEGORY_UNAVAILABLE` ja uut rida ei lisata.
- [ ] Puuduv/tühi `categoryName`, puuduv `sequence` ja liiga pikad väljad annavad 400 `INCORRECT_INPUT`.
- [ ] Uus kategooria on näha nii `GET /api/admin/categories` kui ka `GET /api/categories` vastuses.
- [ ] Sisse logimata kasutaja saab 401 ja mitte-admin 403.
- [ ] Automaattestid katavad eduka lisamise, `null` kirjelduse, duplikaatnime, valideerimisvead, 500 juhtumi ja rollipõhise ligipääsu.

## Avatud küsimused

Lähteülesande põhileping on plaani jaoks piisav; uut ärikäitumist ei lisata.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
