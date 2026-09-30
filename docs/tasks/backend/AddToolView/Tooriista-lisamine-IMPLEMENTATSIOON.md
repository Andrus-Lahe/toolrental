# Tööriista lisamine — implementatsiooni plaan

**Seotud task:** [Tooriista-lisamine.md](./Tooriista-lisamine.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/ToolRentalApplication.java` — tegelik baaspakett ee.toolrental.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` — 400 väljade vead ning kohandatud 403/404.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ApiError.java` — String errorCode ja message.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/PrimaryKeyNotFoundException.java` — puuduva ID erind.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/ForbiddenException.java` — 403 äriviga.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/util/StringBytesConverter.java` — UTF-8 teisendus, mitte Base64 dekooder.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` — Java 21, Spring Boot, JPA, validation, MapStruct, testisõltuvused.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/database/2_create.sql` — tool/tool_image skeem ja peapildi unikaalsus.

Controller/service/persistence ahel ja vastavad testid puuduvad. OAuth, MyProfile ja kategooriate taskid kirjeldavad sõltuvusi, mitte olemasolevat koodi.

## Puuduv/muudetav

Vajalikud on entiteedid/repositooriumid, request DTO ja mapper, tehinguline loomise service, POST controller, veakäsitlus ning testid. booking ega skeemi saadavuse laiendus ei kuulu töösse.

## Sammud

1. **Entiteedid**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/Tool.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/toolimage/ToolImage.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/category/Category.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/appuser/AppUser.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/Profile.java`.

Loo skeemile vastavad JPA seosed või kasuta sõltuvate taskide käigus looduid. ToolImage viitab Tool-ile; omanik ja kategooria on olemasolevad kirjed. Ära cascade-persist omaniku/kategooria uusi kirjeid ega lisa booking seose kaudu uut renti.

2. **Repositooriumid**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/ToolRepository.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/toolimage/ToolImageRepository.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/category/CategoryRepository.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/profile/ProfileRepository.java`.

Lisa vajalik save ja profiili olemasolu otsing, nt existsProfileByUserId(Integer userId). Kohandatud päringud JPQL-is, meetodinimi nimetab tagastatava subjekti. Ära dubleeri juba teostatud sõltuvusi.

3. **Request ja mapper**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/tool/dto/ToolCreateRequestDto.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/persistence/tool/ToolMapper.java`.

DTO ainult categoryId/name/description/imageData. Valideeri NotNull/Positive, NotBlank ja Size vastavalt taskile. MapStruct kaardistab nime/kirjelduse, ignoreerib ID-d, omanikku, staatust ja seoseid, mille määrab service. DTO on tool ressursi jaoks; ainult tegeliku jagamise korral vii controller/common/dto alla. Genereeritud mapperit ei kirjutata käsitsi.

4. **Service**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/ToolService.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/CategoryService.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/service/AppUserService.java`.

Lisa createTool(Integer ownerId, ToolCreateRequestDto toolCreateRequestDto). ID otsingud findById/orElseThrow asuvad public getValidCategoryBy(Integer categoryId) ja getValidAppUserBy(Integer userId) meetodites vastavas service’is. Kontrolli profiili, dekodeeri Base64 enne salvestust, määra owner/category/status A. Üks Transactional piir hõlmab tool ja tool_image salvestuse. Tühja pildi korral ära loo pildirida. Kasuta täistüüpi kirjeldavaid muutujaid ja tingimuslike DTO mutatsioonide korral handle-prefiksit.

5. **Controller ja turve**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/controller/tool/ToolController.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java`.

POST /api/tools kasutab autentimise taski principal’i ID-d ja @Valid request’i. Controller väljastab 200 tühja body’ga, ei käsitle entiteete. Piira Customer rolliga ja säilita CSRF, ära muuda avalikku GET /api/tools ligipääsu. SecurityConfig/principal on OAuth sõltuvused, praegu puuduvad.

6. **Veakäsitlus**

- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java`.
- Fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/error/ErrorResponse.java`.

Kategooria 404 jaoks PrimaryKeyNotFoundException, profiili puudumisel ForbiddenException PROFILE_REQUIRED. Vigane Base64 teisenda 400 INCORRECT_INPUT-iks. Lisa taski 500 üldvastus. Koonda koodid/sõnumid enumi, säilitades tegeliku String errorCode lepingu, ära migreeri seda kõrvaliselt numbriliseks.

7. **Testid** — failid `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/service/ToolServiceTest.java` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/test/java/ee/toolrental/controller/tool/ToolControllerTest.java`. Lisa vajadusel PostgreSQL integratsioonitest peapildi/rollback'i kontrolliks. Käivita profiiliga Customer testprincipal'iga testid; päris Google kontot pole vaja.

## Veakäsitlus

Järgi seotud taski kõiki 400/401/403/404/500 vastuseid. Roll ja sessioon kontrollitakse enne teenust, profiil on service'i ärireegel. Kõik salvestusvead peavad rollback'ima mõlemad kirjed; võõra ownerId body ei tohi jõuda andmebaasi omaniku väärtuseks. Mitte iga 403 pole PROFILE_REQUIRED. Ära tagasta SQL-i ega stack trace'i.

## Testid

- Pildita, tühja/null/pildiga request, baitide täpne Base64 round-trip ja üks is_main=true kirje.
- Customer sessiooni omanik, võõras body ownerId, autentimata/muu roll, CSRF ja profiilita konto.
- Nime/kirjelduse piirid, tühi nimi, puuduv/olematu kategooria ja vigane Base64.
- Pildi salvestuse tõrke korral puudub ka tool kirje; booking tabeli kirjete arv ei muutu.
- 200 tühja body ja täpsed veavastused. DB transaktsiooni/indeksi toimimist ei tõenda ainult repository mock.
- Pildiformaadid/mahulimiidid lisada testidesse pärast lepingu täpsustamist. Käivita sihttestid ja seejärel asjakohane Gradle build; selle dokumentatsioonitöö käigus rakendust ei ehitata.

## Avatud küsimused

- Kasutaja kinnitas saadavuse perioodi eemaldamise. Uus PDF on lähteallikas; vanu märkmeid selles töös ei muudeta.
- Lubatud pildiformaadid, mahu/piikslite piir ja kuvamise MIME-leping vajavad enne pildifunktsiooni valmimist täpsustamist. Praegune bytea/Base64 leping ei sisalda MIME välja.
- Eduka salvestuse järel Minu tööriistad suunamine on FE taski tehniline ettepanek; /my-tools on sihtvaate märkmetes alles kavand.
- backend/CLAUDE.md numbrilised ErrorResponse koodid ja ee.minuprojekt näidispakett erinevad tegelikust String errorCode/ee.toolrental koodist; plaan järgib tegelikku koodi, säilitades kirjeldatud kihid.
- Enne teostust kontrolli uuesti OAuth/MyProfile/kategooriate failide olemasolu, et mitte luua sama entiteeti või teenust teist korda.
