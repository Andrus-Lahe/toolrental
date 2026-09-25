# Kategooriate detailinfo päring — implementatsiooni plaan

**Seotud task:** [Kategooriate-detailinfo-paring.md](./Kategooriate-detailinfo-paring.md)

**Teenus:** `GET /api/categories/detailed-info`

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/PrimaryKeyNotFoundException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/DataNotFoundException.java`

Rakenduse pakett on ee.toolrental. Handler katab @Valid väljavead ning kohandatud 403/404; ApiError errorCode ja message on String. ForbiddenException konstruktor võtab (message, errorCode). Puuduvad controller/service/persistence klassid ja backend testid; valmis analoogset endpoint’i ei leitud. `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` sisaldab JPA, validation, MapStructi ja testisõltuvusi. `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` ning `3_import.sql` määravad skeemi ja näidisandmed. Teiste taskide olemasolu ei tähenda nende koodi olemasolu.

## Puuduv/muudetav

Vajalikud on allpool loetletud entiteedid/repositooriumid, DTO/mapper, CategoryService, CategoryController, täpne vealeping ja testid. Ühised klassid luuakse üks kord; enne teostamist kontrolli uuesti paralleelsete taskide tehtud muudatusi. SQL skeemi automaatset muutmist see plaan ei nõua.

## Sammud

1. **Persistence entiteedid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/category/Category.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/categoryimage/CategoryImage.java`

Kaardista lähteülesande tabelid ja FK-d JPA-s; ära kasuta cascade REMOVE viiteandmete või jagatud seoste suhtes. FK mudelid, mida teised taskid juba loovad, taaskasuta.

2. **Repositooriumid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/category/CategoryRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/categoryimage/CategoryImageRepository.java`

CategoryRepository.findAllCategoriesWithImagesBy(): vasakühendus optional pildiga, sequence ASC/id ASC. Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/category/dto/CategoryDetailedInfoDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/category/CategoryMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/CategoryService.java`

CategoryService.getCategoryDetailedInfos(): mapper id/nimi/kirjeldus, pilt Base64.getEncoder().encodeToString(bytes), pildita null. Mitte UTF-8 StringBytesConverter, mitte suvaline lisapilt. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/category/CategoryController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `GET /api/categories/detailed-info` ning lähteülesande 200 keha. Lugemisteenus on avalik. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/KategooriatedetailinfoparingServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/category/KategooriatedetailinfoparingControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

PDF-is on „Veateated: —“. Allolev 500 leping on taski tehniline täpsustus, mis järgib olemasoleva kategoorialoendi taski veavormingut. Sisendita avalikule päringule ei lisata 400/401/403/404 ärivigu; tühi tulemus ja puuduv kategooriapilt ei ole vead.

| Olukord | Status code | Response body |
|---|---|---|
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Kategooriate laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

Kasuta olemasoleva `ApiError` kuju: `message` ja `errorCode` on stringid. Praegune `RestExceptionHandler` ei taga veel kirjeldatud 500 vastust; selle tagamine kuulub teenuse teostusse. SQL-i ja stack trace'i kliendile ei tagastata.

## Testid

Puuduv pilt/kirjeldus, sortimine, Base64 round-trip ja kõik kategooriad ka tööriistadeta.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] `GET /api/categories/detailed-info` on olemas, sisenditeta ja kättesaadav ka sisse logimata kasutajale.
- [ ] Vastus on HTTP 200 ja JSON massiiv; iga element sisaldab ainult `categoryId`, `categoryName`, `description` ja `imageData`.
- [ ] Andmed loetakse tabelitest `category` ja `category_image`; vastuses on kõik kategooriad, sealhulgas tööriistadeta kategooriad.
- [ ] Järjestus on `category.sequence ASC`, võrdse väärtuse korral `category.id ASC`.
- [ ] Impordiandmetega tagastatakse tabelis toodud neli kategooriat, sh nimi „Muud“; pildid on tegelike imporditud baitide Base64 esitus.
- [ ] Base64 dekodeerimisel saadakse tagasi täpselt andmebaasi pildibaidid; puuduvad data-URL prefiks ja topeltkodeerimine.
- [ ] Puuduva pildikirjega kategooria jääb vastusesse väärtusega `imageData: null`; puuduv kirjeldus annab `description: null`.
- [ ] Tühja kategooriatabeli korral tagastatakse `200` ja `[]`, mitte `404` või `null`.
- [ ] Andmebaasi tõrke korral tagastatakse kirjeldatud 500 `ApiError` ilma tehniliste detailideta.
- [ ] Päring ei muuda andmeid ega muuda olemasoleva `GET /api/categories` lihtloendi kontrakti.
- [ ] Automaattestid kontrollivad avalikku ligipääsu, DTO kuju, andmete täielikkust, pildi Base64 teisendust, sortimist, puuduvat pilti/kirjeldust, tühja tulemust ja 500 vastust. Sortimise testandmed eristavad `sequence` järjekorda sisestamisjärjekorrast ning sisaldavad võrdseid `sequence` väärtusi.

## Avatud küsimused

MIME info skeemis puudub; ära lisa oletuslikku DTO välja.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
