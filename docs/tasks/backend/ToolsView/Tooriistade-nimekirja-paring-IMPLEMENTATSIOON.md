# Tööriistade nimekirja päring — implementatsiooni plaan

**Seotud task:** [Tooriistade-nimekirja-paring.md](./Tooriistade-nimekirja-paring.md)

**Teenus:** `GET /api/tools`

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/PrimaryKeyNotFoundException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/DataNotFoundException.java`

Rakenduse pakett on ee.toolrental. Handler katab @Valid väljavead ning kohandatud 403/404; ApiError errorCode ja message on String. ForbiddenException konstruktor võtab (message, errorCode). Puuduvad controller/service/persistence klassid ja backend testid; valmis analoogset endpoint’i ei leitud. `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` sisaldab JPA, validation, MapStructi ja testisõltuvusi. `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` ning `3_import.sql` määravad skeemi ja näidisandmed. Teiste taskide olemasolu ei tähenda nende koodi olemasolu.

## Puuduv/muudetav

Vajalikud on allpool loetletud entiteedid/repositooriumid, DTO/mapper, ToolService, ToolController, täpne vealeping ja testid. Ühised klassid luuakse üks kord; enne teostamist kontrolli uuesti paralleelsete taskide tehtud muudatusi. SQL skeemi automaatset muutmist see plaan ei nõua.

## Sammud

1. **Persistence entiteedid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/Tool.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/toolimage/ToolImage.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUser.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/Profile.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/location/Location.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/district/District.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/city/City.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/category/Category.java`

Kaardista lähteülesande tabelid ja FK-d JPA-s; ära kasuta cascade REMOVE viiteandmete või jagatud seoste suhtes. FK mudelid, mida teised taskid juba loovad, taaskasuta.

2. **Repositooriumid**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/ToolRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/toolimage/ToolImageRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUserRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/ProfileRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/location/LocationRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/district/DistrictRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/city/CityRepository.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/category/CategoryRepository.java`

ToolRepository.findFilteredToolsBy(categoryId, cityId, districtId, status, Pageable): JPQL AND-filtrid, LEFT JOIN omaniku profiili/aadressiga, eraldi korrektne countQuery, id ASC. Kohandatud päringud JPQL @Query abil; meetodinimi nimetab tagastatava subjekti.

3. **DTO ja mapper**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/tool/dto/ToolsResponse.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/tool/dto/ToolListItemDto.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/ToolMapper.java`

Kasuta täpselt lähteülesande DTO välju/tüüpe ja @Valid piiranguid; controller ei väljasta entiteete. MapStruct mapper on liides, genereeritud implementatsiooni ei muudeta. CategoryRequestDto jaguneb admin controlleri ja category mapperi vahel, seega common/dto; muu DTO paigutus hinnata tegeliku ressursiülese kasutuse järgi. Praegu olemasolevaid DTO-sid ümber tõsta pole.

4. **Service**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/ToolService.java`

ToolService.getTools(...): vaikeväärtused 0/0/0/A/1/6. Erista puuduv ja tühi parameeter. Valideeri Integer piirid, ID>=0, page>=1, size>=1, status A/U/0. API 1-põhine page teisenda Pageable 0-põhiseks. Peapilt Base64, null-väljad säilivad. Olematu positiivne filter või vastuolu annab tühja 200, mitte 404. Üle viimase lehe säilita tegelikud koguarvud. findById/orElseThrow on vastava ressursi public getValid<Entity>By(Integer id) meetodis. Muutujanimed peegeldavad täistüüpi; tingimuslik DTO muutmine käib handle-prefiksiga abimeetodis.

5. **Controller ja ligipääs**

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/tool/ToolController.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`

Seo täpselt `GET /api/tools` ning lähteülesande 200 keha. Lugemisteenus on avalik. Muutvate sessioonipäringute CSRF-leping tuleb ühendada OAuth taskiga. SecurityConfig/principal pole veel teostatud.

6. **Testid** — `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/TooriistadenimekirjaparingServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/tool/TooriistadenimekirjaparingControllerTest.java` (nimed on ettepanekud). Loo service ühiktestid ja HTTP lepingut kontrollivad testid; tehingu/JPQL/lukustuse käitumist kontrolli PostgreSQL integratsiooniga. Käivita sihttestid, seejärel vajalik `./gradlew test` ja build.

## Veakäsitlus

Muudetavad failid:

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`

Koonda ärikoodid/sõnumid enumisse olemasolevat String lepingut säilitades. PRIMARY_KEY_NOT_FOUND kasutab PrimaryKeyNotFoundException; 403 ärivead ForbiddenException(message, errorCode). HTTP query/path teisendus ja vale JSON/kuupäev vajavad eraldi handlerit. Olemasolev getFieldErrors().getFirst() ei toeta tühja väljavigade loendiga global viga: ristvälja valideerimine seo konkreetse väljaga või paranda handleri fallback. Ühtne 500 pole praegu tagatud. Lähteülesande täpne vealeping:

PDF-is on „Veateated: —”, kuid päringul on filtreerimise ja lehekülgjaotuse sisendid. Allolevad 400 ja 500 vastused on taski tehnilised täpsustused. Veavastus järgib olemasolevat `ApiError` kuju: String-väljad `errorCode` ja `message`.

| Olukord | Status code | Response body |
|---|---|---|
| Arvuline parameeter on tühi, pole täisarv või ei mahu Integer vahemikku; näites `pageNumber`. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"pageNumber: peab olema Integer-tüüpi täisarv"}`; teise parameetri puhul kasutada selle nime. |
| `pageNumber < 1` või `pageSize < 1`. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"pageNumber: peab olema vähemalt 1"}` või sama teade väljaga `pageSize`. |
| ID-filter on negatiivne; näites `categoryId`. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"categoryId: peab olema 0 või positiivne täisarv"}`; kasutada vastava filtri nime. |
| `status` pole `A`, `U` ega `0`, sh tühi string. | 400 Bad Request | `{"errorCode":"INCORRECT_INPUT","message":"status: lubatud väärtused on A, U ja 0"}` |
| Andmebaasipäring ebaõnnestub ootamatult. | 500 Internal Server Error | `{"errorCode":"INTERNAL_SERVER_ERROR","message":"Tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti."}` |

Avalik päring ei nõua 401/403 vastuseid. Puuduv positiivne filtri-ID, omavahel sobimatud asukohafiltrid, tühi tulemus ja vahemikust väljas positiivne lehenumber ei ole 404 vead. Query teisendusvigade ja 500 JSON kuju tuleb teostamisel tagada; olemasolev request body väljade handler neid automaatselt ei kata. SQL-i ja stack trace'i ei väljastata.

## Testid

Impordi A=7, U=1, 0=8; AND-filtrid, profiilita omanik filtrita/asukohafiltriga, ainult is_main, count ei dubleeru, pagination piirid ja vigased/tühjad parameetrid.

Lähteülesande vastuvõtukriteeriumidest tuletatav kontrollnimekiri (kontrolli iga punkti, mitte ainult 200 staatust):

- [ ] `GET /api/tools` on avalik, ilma body ja path variable'ita; tagastab HTTP 200 ja `ToolsResponse`.
- [ ] Vaikeväärtused on `categoryId=0`, `cityId=0`, `districtId=0`, `status=A`, `pageNumber=1`, `pageSize=6`.
- [ ] ID-filtri `0` tähendab filtri puudumist; `status=0` lubab A/U tööriistad. Filtrid toimivad eraldi ja AND-kombinatsioonina.
- [ ] Tulemused on `tool.id ASC` järjekorras; lehenumber on API-s 1-põhine. Loendus vastab samadele filtritele nagu kaardipäring.
- [ ] Vastus sisaldab täpselt kirjeldatud metaandmeid ning tööriista seitset välja; kontakti-, Google- ja täpseid aadressiandmeid ei väljastata.
- [ ] Põhipilt on ainult `is_main=true` kirjest ja Base64 dekodeerimisel vastavad baidid andmebaasi väärtusele. Lisapilte ei valita asemele.
- [ ] Pildita tööriist säilib `imageData: null` väärtusega ning NULL-kirjeldus säilib JSON-is.
- [ ] Profiilita omaniku sobiv tööriist säilib ilma asukohafiltrita, asukohanimed on NULL. Positiivse asukohafiltri korral see ei sobitu.
- [ ] Tööriist ei kordu mitme pildi tõttu ning lehekülje koguarv loendab erinevaid tööriistu.
- [ ] Impordiandmetega vastavad vaikepäring, status=0, status=U ja filtrinäited eespool toodud kontrolltabelile; vaikimisi teisel lehel on ainult Projektor (8).
- [ ] Puuduv filtri-ID ja vastuoluline linn/linnaosa annavad tühja loendi. `totalElements=0` korral on `totalPages=0`.
- [ ] Viimasest lehest suurem positiivne lehenumber annab tühja `tools` loendi, säilitades tegeliku koguarvu ja lehtede arvu.
- [ ] Vigased sisendid annavad kirjeldatud 400 ja andmebaasi tõrge 500 `ApiError` vastuse.
- [ ] Automaattestid katavad avaliku ligipääsu, DTO/JSON kuju, kõik vaikeväärtused ja filtrid, järjekorra ning lehekülgjaotuse (sh viimane, tühi ja üle piiri leht).
- [ ] Andmebaasipõhised testid katavad põhipildi valiku mitme pildi seast, pildita tööriista, profiilita omaniku, erinevate linnade/linnaosade filtreerimise ning koguarvu õigsuse. Eraldi testid katavad 400/500 vastused.
- [ ] Päring ei muuda andmeid; SQL-i ega impordifaili ei muudeta testistsenaariumide loomiseks.

## Avatud küsimused

Ära kasuta INNER JOIN-i profiilita tööriista eemaldamiseks. Väga suure page*size nihke Integer ületäitumist käsitle ohutult; sobivast tulemustest kaugel leht annab lepingu järgi tühja tulemuse.

backend/CLAUDE.md kirjeldab numbrilisi ErrorResponse koode, kuid tegelik ApiError kasutab String koodi ja ErrorResponse enum puudub. Säilita tegelik leping; ära tee numbrilist migratsiooni. Struktuuridokumendi ee.minuprojekt on näidis, kasutada ee.toolrental. OAuth/ühisklasside sõltuvused tuleb realiseerida või taaskasutada, mitte eeldada neid valmis olevaks. See dokument ei muuda tootmiskoodi ega tõenda testide läbimist.
