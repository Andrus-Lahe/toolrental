# Valitud linna linnaosade päring — implementatsiooni plaan

**Seotud task:** [Valitud-linna-linnaosade-paring.md](./Valitud-linna-linnaosade-paring.md)

**Teenus:** `GET /api/cities/{cityId}/districts`

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/PrimaryKeyNotFoundException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/DataNotFoundException.java`

Rakenduse pakett on ee.toolrental. Handler katab @Valid väljavead ning kohandatud 403/404; ApiError errorCode ja message on String. ForbiddenException konstruktor võtab (message, errorCode). Puuduvad controller/service/persistence klassid ja backend testid; valmis analoogset endpoint’i ei leitud. `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` sisaldab JPA, validation, MapStructi ja testisõltuvusi. `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` ning `3_import.sql` määravad skeemi ja näidisandmed. Teiste taskide olemasolu ei tähenda nende koodi olemasolu.

## Puuduv/muudetav

Vajalikud on allpool loetletud entiteedid/repositooriumid, DTO/mapper, CityService, CityController, täpne vealeping ja testid. Ühised klassid luuakse üks kord; enne teostamist kontrolli uuesti paralleelsete taskide tehtud muudatusi. SQL skeemi automaatset muutmist see plaan ei nõua.

## Sammud

1. **Persistence entiteedid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/city/City.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/district/District.java`

Kaardista lähteülesande tabelid ja FK-d JPA-s; ära kasuta cascade REMOVE viiteandmete või jagatud seoste suhtes. FK mudelid, mida teised taskid juba loovad, taaskasuta.

2. **Repositooriumid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/city/CityRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/district/DistrictRepository.java`

DistrictRepository.findDistrictsByCityIdOrderByIdAsc(Integer cityId). Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/city/dto/DistrictDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/city/CityMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/CityService.java`

CityService.getValidCityBy(Integer cityId) teeb findById/orElseThrow; seejärel getDistricts(Integer cityId). Puuduv linn 404, olemasolev tühi linn []. 0 pole siin filtriväärtus. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/city/CityController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `GET /api/cities/{cityId}/districts` ning lähteülesande 200 keha. Lugemisteenus on avalik. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/ValitudlinnalinnaosadeparingServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/city/ValitudlinnalinnaosadeparingControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

404 leping pärineb PDF-ist; 400 ja 500 on sisendi ning tehnilise tõrke käsitlemise täpsustused. Vastuse kuju on olemasolev `ApiError` (`message`, `errorCode`).

| Olukord | Status code | Response body |
|---|---|---|
| Linn `cityId = 123` puudub. Muu puuduva ID puhul kasutada teates tegelikku väärtust. | 404 Not Found | `{"errorCode":"PRIMARY_KEY_NOT_FOUND","message":"Ei leidnud primary keyd 'cityId' väärtusega: 123"}` |
| `cityId` pole täisarv või ei mahu Java Integer vahemikku. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"cityId: peab olema Integer-tüüpi täisarv"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Linnaosade laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

404 saab anda olemasoleva `PrimaryKeyNotFoundException` kaudu. Path variable'i teisendamise 400 ja ühtne 500 kuju tuleb teostamisel tagada; praegune väljade valideerimise handler ei taga neid automaatselt. SQL-i ega stack trace'i ei tagastata.

## Testid

Tallinna kaheksa linnaosa, teise linna kirjed puuduvad; olemasolev tühi linn versus puuduv linn, vigane path Integer.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] Avalik `GET /api/cities/{cityId}/districts` võtab vastu Integer-tüüpi linna ID.
- [ ] HTTP 200 vastus on massiiv ainult väljadega `districtId` ja `districtName`.
- [ ] Vastuses on ainult valitud linna linnaosad, järjestuses `district.id ASC`.
- [ ] Impordi Tallinn annab 8, Tartu 18 ja Pärnu 7 linnaosa; teiste linnade kirjeid vastuses pole.
- [ ] Olemasoleva linna tühi loend annab 200 ja `[]`; puuduv linn annab 404 ning päringu ID-ga `PRIMARY_KEY_NOT_FOUND` teate.
- [ ] `cityId = 123` annab PDF-is kirjeldatud täpse veavastuse.
- [ ] Vigane arvuvorming annab kirjeldatud 400 ja andmebaasi tõrge kirjeldatud 500 vastuse.
- [ ] Päring ei sõltu tööriistade või profiilide olemasolust ega muuda andmeid.
- [ ] Automaattestid katavad avaliku ligipääsu, DTO kuju, eri linnade filtreerimise, järjestuse, tööriistadeta linnaosa, olemasoleva linna tühja loendi ning 400/404/500 juhtumid.

## Avatud küsimused

Lähteülesande põhileping on plaani jaoks piisav; uut ärikäitumist ei lisata.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
