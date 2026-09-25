# Linnade nimekirja päring — implementatsiooni plaan

**Seotud task:** [Linnade-nimekirja-paring.md](./Linnade-nimekirja-paring.md)

**Teenus:** `GET /api/cities`

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

Kaardista lähteülesande tabelid ja FK-d JPA-s; ära kasuta cascade REMOVE viiteandmete või jagatud seoste suhtes. FK mudelid, mida teised taskid juba loovad, taaskasuta.

2. **Repositooriumid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/city/CityRepository.java`

CityRepository.findAllCitiesByOrderByIdAsc(). Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/city/dto/CityDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/city/CityMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/CityService.java`

CityService.getCities(): tagasta id/nimi, [] tühjal tabelil. ReadOnly, lehekülgjaotust pole. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/city/CityController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `GET /api/cities` ning lähteülesande 200 keha. Lugemisteenus on avalik. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/LinnadenimekirjaparingServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/city/LinnadenimekirjaparingControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

PDF-is on „Veateated: —”. Allolev 500 on taski tehniline täpsustus. Sisendita avaliku loendi jaoks ei lisata 400/401/403/404 ärivigu.

| Olukord | Status code | Response body |
|---|---|---|
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Linnade laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

Kasuta olemasoleva `ApiError` kuju: `message` ja `errorCode` on String-väljad. Praegune `RestExceptionHandler` ei taga veel seda 500 lepingut. SQL-i ega stack trace'i kliendile ei tagastata.

## Testid

Kõik kolm impordilinna õiges järjekorras, tühi loend, avalik ligipääs.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] `GET /api/cities` on kättesaadav ka ilma sisselogimiseta ega vaja päringu sisendeid.
- [ ] Vastus on HTTP 200 ja massiiv, mille elementidel on ainult `cityId` ning `cityName`.
- [ ] Tagastatakse kõik tabeli `city` kirjed järjestuses `city.id ASC`.
- [ ] Impordiandmetega vastab tulemus eespool toodud 3 kirjega JSON-ile.
- [ ] Tööriistadeta viitekirjed säilivad vastuses; tühja tabeli korral on vastus `[]`.
- [ ] Andmebaasi tõrkel tagastatakse kirjeldatud 500 `ApiError`; tehnilisi detaile ei avaldata.
- [ ] Päring ei muuda andmeid ega lisa paralleelset endpoint'i sama funktsiooni jaoks.
- [ ] Automaattestid kontrollivad avalikku ligipääsu, DTO kuju, täielikku loendit, sortimist, tühja tulemust ja 500 vastust. Sortimise testandmed peavad eristama nõutud järjekorda juhuslikust sisestamisjärjekorrast.

## Avatud küsimused

Lähteülesande põhileping on plaani jaoks piisav; uut ärikäitumist ei lisata.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
