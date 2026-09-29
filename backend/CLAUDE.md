# CLAUDE.md

See fail annab juhiseid Claude Code'ile (claude.ai/code) selles repositooriumis töötamiseks.

**Keel:** Kõik uued kanded sellesse faili kirjutatakse eesti keeles.

## Käsud

```bash
# Ehitamine
./gradlew build

# Käivitamine (nõuab lokaalset PostgreSQL-i)
./gradlew bootRun

# Testide käivitamine
./gradlew test

# Ühe testiklassi käivitamine
./gradlew test --tests "ee.minuprojekt.MinuprojektApplicationTests"

# Ainult kompileerimine (käivitab ka MapStructi annotatsiooni töötluse)
./gradlew compileJava
```

## Andmebaasi seadistamine

PostgreSQL peab töötama `localhost`-is järgmiste seadetega:
- Andmebaas: `vali_it`
- Kasutajanimi: `postgres`
- Parool: `student123`
- Mugav DB url: `jdbc:postgresql://localhost:5432/vali_it`

Käivita skriptid järjekorras kaustast `docs/database`:
1. `1_reset_database.sql` — kustutab ja loob uuesti `minu_projekt` skeema
2. `2_create.sql` — loob kõik tabelid
3. `3_import.sql` — lisab algandmed

Kõik tabelid asuvad `minu_projekt` skeemas.

## Käivitamine WSL-ist (Claude Code)

IntelliJ-s käivitades pole seda vaja. Kui backend käivitatakse WSL-i terminalist, näiteks Claude Code'ist, tuleb arvestada kolme asjaga. Ühtegi projekti faili selleks muuta ei tohi, kõik antakse ette keskkonnamuutujatena ainult selle käsu jaoks.

**JAVA_HOME** viitab Windowsi JDK-le (`/mnt/c/Users/opilane/.jdks/...`) ja `./gradlew` annab vea `JAVA_HOME is set to an invalid directory`. Kasuta WSL-i Java 21:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

**Andmebaas:** WSL2 NAT-võrgurežiimis ei jõua `localhost:5432` Windowsis jooksva PostgreSQL-ini. Kasuta Windowsi hosti IP-d, mis on WSL-i vaikelüüs:

```bash
WIN_HOST=$(ip route show default | awk '{print $3}')   # nt 172.17.80.1
pg_isready -h "$WIN_HOST" -p 5432                        # kontroll
export SPRING_DATASOURCE_URL="jdbc:p6spy:postgresql://$WIN_HOST:5432/vali_it"
```

**Google OAuth:** `GOOGLE_CLIENT_ID` ja `GOOGLE_CLIENT_SECRET` peavad olema keskkonnamuutujatena olemas. Õpilase masinas on need IntelliJ run-konfiguratsioonis (`.idea/workspace.xml`), mitte WSL-i keskkonnas. Ära kirjuta neid repositooriumi faili ega väljundisse.

Seejärel käivita: `./gradlew bootRun`. Backend jookseb pordil 8080 ja Windowsi brauser jõuab selleni aadressil `localhost:8080`. Kui IntelliJ-s jookseb samal ajal teine backend, tekib pordikonflikt, seega käivita korraga ainult üks.

## Arhitektuur

Tegemist on Spring Boot 4.x / Java 21 REST backendiga. Frontend on eraldi Vue 3 SPA (ei ole selles repos).

### Kihtide struktuur

```
controller/       REST endpointid + päringu/vastuse DTOd
service/          Äriloogika
persistence/      JPA entiteedid, repositooriumid, MapStructi mapperid
infrastructure/   Läbivad komponendid: erindi tüübid, veakoodid, globaalne erindite käsitleja
```

Igal domeenialal on oma alampakk `controller/`-is koos DTOdega, teenusklass ja persistence pakk.

### Olulised konventsioonid

**DTOd vs entiteedid** — Kontrollerid näevad ainult DTOsid. MapStructi mapperid (liidesed annotatsiooniga `@Mapper`) teisendavad DTOd JPA entiteetideks ja vastupidi. Genereeritud mapperi implementatsioonid tekivad kausta `src/main/generated/`.

**Veakäsitlus** — Teenustest visatakse kohandatud erindeid (`DataNotFoundException`, `ForbiddenException`, `PrimaryKeyNotFoundException`), mille püüab kinni `RestExceptionHandler` (`@ControllerAdvice`). Kõik äriveateated ja numbrilised veakoodid on koondatud `ErrorResponse` enumi.

**Muutujate nimetamine** — Muutuja nimi peab peegeldama täistüüpi: `EntityDetailDto entityDetailDto`, mitte `EntityDetailDto dto`.

**Meetodi nimetamine** — `getX()` lubab kindlat tagastust. Kui meetod sisaldab tingimislikku loogikat ja muteerib DTO-d, kasuta `handle`-prefiksit ja anna DTO parameeter sisse: `handleAddImageData(EntityDetailDto entityDetailDto, Integer entityId)`.

**Entiteedi otsing ID järgi** — `repository.findById()` kasutamine `orElseThrow`-ga peab olema `public getValid<Entiteet>By(Integer <entiteet>Id)` meetodis vastava service klassi all (nt `getValidEntityBy(Integer entityId)` `AppUserService`-s).

**SQL päringud** — Kohandatud päringud on JPQL, kirjutatud otse Spring Data repositooriumi liidesele `@Query` annotatsiooniga. Vajadusel kasutab repositoorium konstruktori avaldist otse DTOsse projekteerimiseks.

**Repositooriumi meetodi nimetamine** — Meetodi nimi peab mainima, mida ta tagastab (subjekti), mitte jääma geneeriliseks: `findFilteredEntitiesBy(...)` tagastab `List<Entity>`, `findUserBy(...)` tagastab `User`. Väldi kujundeid nagu `findFilteredBy(...)`, kust pole näha, mida meetod tagastab.

**Jagatud DTO-d** — DTO klass, mida kasutab rohkem kui üks ressursipakett (nt mitme kontrolleri/mapperi vahel jagatud), ei kuulu ühegi üksiku ressursi `dto/` paketti, vaid paketti `controller/common/dto/` (nt `SharedEntityDto`).

**SQL logimine** — P6Spy on seadistatud (`spy.properties`), nii et täielik parameetritega SQL kuvatakse konsoolis. Selle saab keelata, lülitades `application.properties`-is tagasi kommenteeritud tavaliste PostgreSQL seadetele.

### REST API

Baastee: `/api`. Swagger UI on saadaval aadressil `/swagger-ui.html`.

Üksikute teenuste kirjeldused (URL, sisend, väljund, veaolukorrad, vastuvõtu kriteeriumid) ei ole selles failis — need dokumenteeritakse taski failidena kaustas `docs/tasks/backend/`.
