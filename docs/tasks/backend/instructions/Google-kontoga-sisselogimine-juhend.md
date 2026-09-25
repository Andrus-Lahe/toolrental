# Juhend: GET /api/me (Google kontoga sisselogimine)

**Taski fail:** `docs/tasks/backend/GoogleLoginView/Google-kontoga-sisselogimine.md`
**Alus (märkmed):** `docs/balsamic/notes/googlega_login.md`
**Kontroller:** uus kontroller `controller/<ressurss>/` alla (nimi on sinu otsus)
**Implementeerimise voog:** Eeldused (Security + OAuth) → RestController → Service → Repository → Service → Mapper → RestController

---

## Sissejuhatus

See endpoint vastab frontendi küsimusele „kes on praegu sisse logitud ja kas tal on profiil?“. Enne kui `GET /api/me` saab midagi vastata, peab backend oskama Google'iga sisse logida ja sisse logitud kasutaja sessiooni meelde jätta — seda teeb Spring Security. Selle harjutuse käigus õpid, kuidas sessioonist tulev kasutaja jõuab kontrollerisse (`@AuthenticationPrincipal`), ja kordad tavalist GET-voogu: kontroller → service → repository → mapper.

See task on ka eeldus issue #33 (`GET/PUT /api/users/me/profile`) jaoks: seal kasutad sama principal'i ja samu entiteete.

> **Mis on praegu olemas?** Backendis on ainult `infrastructure/` kaust (veakäsitlus). Controller-, service- ja persistence-kaustu veel pole. Base-pakett on `ee.toolrental` (vt `ToolRentalApplication.java`).

---

## Samm 0 — Eeldused: Spring Security ja Google OAuth

### Mida teha?

Enne endpointi on vaja kolme asja: sõltuvust, seadistust ja „liimi“, mis seob Google'i konto meie `app_user` tabeliga. Kõik vajalik on kirjas failis `docs/balsamic/notes/googlega_login.md` — **loe see jaotiste kaupa läbi ja kirjuta kood ise**, mitte ära kopeeri pimesi. Iga klassi juures küsi endalt: *miks see siin on?*

1. **Google Cloud Console** (juhendi jaotis 1) — sul on vaja `GOOGLE_CLIENT_ID` ja `GOOGLE_CLIENT_SECRET`. Redirect URI on `http://localhost:5173/login/oauth2/code/google`.
2. **Sõltuvus** (jaotis 2) — lisa `build.gradle`-isse OAuth client starter. Pane tähele: Spring Boot 4-s on starteri nimi teistsugune kui vanades õpetustes.
   > **IntelliJ vihje:** Pärast `build.gradle` muutmist ilmub paremale ülesse elevandi ikoon — vajuta sellele (**Load Gradle Changes**), muidu IntelliJ ei tunne uusi klasse.
3. **Seadistus** (jaotis 3) — `application.properties`-isse viited keskkonnamuutujatele. **Saladust ennast faili ei kirjutata!** Keskkonnamuutujad pane IntelliJ-s: Run → Edit Configurations → Environment variables.
4. **Vite proxy** (jaotis 6) — frontendi `vite.config.js`-is peab lisaks `/api`-le minema backendi ka OAuth ja logout teed.

### Entiteedid andmebaasist (JPA Buddy)

`GET /api/me` vajab tabeleid `app_user`, `role` ja `profile`. `profile` viitab `location`-ile, see omakorda `district`-ile ja see `city`-le — seega JPA vajab kogu seda ketti.

> **JPA Buddy vihje:** Paremklõps paketil → New → JPA Buddy → **JPA Entities from DB**. Vali tabelid, mis ahelas vaja on. Iga entiteet oma alampaketti `persistence/<entiteet>/`.

> **Mõtle:** Kas kõik seosed peavad olema `EAGER`? Mis juhtub, kui iga päring laeb kogu ketti kaasa? Vali teadlikult.

Pärast genereerimist kontrolli iga entiteet üle: tabeli nimi, veerunimed, `@ManyToOne` seosed, `nullable`/`length` väärtused (võrdle `docs/database/2_create.sql`-iga).

### Principal ja kasutaja sidumine

Loe `googlega_login.md` jaotis 4. Seal on kaks klassi, mis lähevad `infrastructure/security/` alla:
- **principal** — Google'i kasutaja objekt, mis kannab lisaks meie enda `app_user.id` väärtust;
- **OIDC teenus** — leiab või loob `app_user` rea `google_sub` järgi, keelab blokeeritud kasutaja ja annab rolli.

> **Mõtle:** Miks otsitakse kasutajat `sub` järgi, mitte e-posti järgi? (Vastus on juhendis — leia see üles.)

Repository meetod, mis otsib kasutajat Google'i identifikaatori järgi, tuleb sul endal teha (vt „Uue meetodi loomine JPA Buddy abil“ Samm 4-s). Mõtle, mis tüüpi tulemus see peaks olema, kui kasutajat veel pole.

### Turvaseadistus

Loe `googlega_login.md` jaotis 5. Konfiguratsiooniklass määrab:
- millised teed on avalikud (vaata, millised GET-teed juhendis juba kirjas on);
- kuhu suunatakse pärast õnnestunud / ebaõnnestunud sisselogimist;
- et sisse logimata API päring saab **401**, mitte suunamist Google'i lehele.

> **Otsuse koht — CSRF:** Taskifail ütleb „hoia CSRF-kaitse alles“, aga märkmefail `googlega_login.md` (meie alus) lülitab selle õppeprojekti lihtsuse huvides välja ja selgitab miks. Enne kirjutamist räägi see oma tiimiga läbi või küsi minult — ära otsusta vaikides.

### Kontroll enne edasiliikumist

- [ ] `./gradlew compileJava` õnnestub.
- [ ] Rakendus käivitub (keskkonnamuutujad on seadistatud).
- [ ] `http://localhost:8080/swagger-ui/index.html` avaneb ilma sisselogimiseta.
- [ ] Sisse logimata `GET http://localhost:8080/api/...` suvaline kaitstud tee annab **401**.

---

## Samm 1 — RestController

### Mida teha?

Nüüd loome endpointi `GET /api/me`. Kontrollerit pole veel ühtegi, seega lood esimese.

Kontrolli esmalt:
- Kaust: `backend/src/main/java/ee/toolrental/controller/`
- Kui **puudub** → loo pakett ja alampakett (mõtle hea ressursinimi — see endpoint puudutab sisse logitud kasutajat)
- Loo klass IntelliJ'ga (File → New → Java Class)

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KontrolleriKlass {
    // ...
}
```

### Meetodi loomine

Alusta ilma annotatsioonideta:

```java
public void meetodiNimi() {
    // tühi meetod esialgu
}
```

> **Mõtle:** Sellel endpointil pole path- ega query-parameetreid. Kust tuleb siis info, *kelle* andmeid tagastada?
> Vihje: `googlega_login.md` jaotis 7 näitab annotatsiooni, millega Spring annab kontrollerile sessiooni kasutaja.

Seejärel lisa:
1. **Mappingannotatsioon** — `@GetMapping` (tee taskifailist)
2. **Principal parameeter** — sessiooni kasutaja (mitte `@PathVariable` ega `@RequestParam`!)
3. **Swagger annotatsioonid** — `@Operation` ja `@ApiResponses` (200 ja 401)

```java
@GetMapping("/mingi/rada")
@Operation(summary = "Lühikokkuvõte")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Kirjeldus")})
public void meetodiNimi(@SessiooniAnnotatsioon PrincipaliTüüp principal) {
}
```

### Service klassi ettevalmistus

Kontrolli kausta `backend/src/main/java/ee/toolrental/service/`. Kui puudub → loo:

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {
    // ...
}
```

Lisa kontrollerisse service muutuja ja kutsu service meetod välja. Mõtle: **mida** principal'ist service'ile kaasa anda? Kas terve principal või ainult need väärtused, mida service tegelikult vajab? (Vaata DTO välju taskifailist — üks neist ei tule andmebaasist.)

```java
teenuseMuutuja.meetodiNimi(väärtus1, väärtus2);
```

> **IntelliJ vihje:** Punane joon service meetodi all → **Alt+Enter** → **"Create method in TeenusKlass"**.

---

## Samm 2 — Service ja esimene repository päring

### Mida teha?

Service meetod saab sisse sisse logitud kasutaja ID. Esimene samm on leida see kasutaja andmebaasist.

Mõtle: **millisest tabelist** tulevad `firstName`, `lastName` ja `roleName`? (Taskifaili „Väljund“ tabelis on veerg „Allikas“.)

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    entiteetRep  // <- kirjuta algus siia
}
```

> **IntelliJ vihje:** Kirjuta repositooriumi nime algus → **Tab** → repositoorium lisatakse klassiväljana.

**Küsi endalt:** Kas JPA valmis `findById()` sobib? Mis tüüpi see tagastab?

> **Projekti konventsioon (`backend/CLAUDE.md`):** `findById().orElseThrow(...)` kirjutatakse **`public getValid<Entiteet>By(Integer <entiteet>Id)`** meetodisse vastava entiteedi service klassis. Mõtle, kas see meetod kuulub samasse service'isse või eraldi entiteedi service'isse — ja millise olemasoleva exception'iga see puudumise korral lõpeb (vaata `infrastructure/exception/`).

Kui meetod tagastab midagi — **pane tulemus kohe muutujasse** (vt „Meetodi palve“ allpool).

---

## Samm 3 — DTO klass

### Mida teha?

Kasutaja on käes — loo nüüd väljundi DTO. Taskifaili „Väljund“ näitab kuut välja. Pane tähele, et need tulevad **kolmest erinevast kohast**: kasutaja entiteedist, rolli entiteedist ja profiilist / sessioonist.

Kontrolli, kas DTO on olemas: `backend/src/main/java/ee/toolrental/controller/.../dto/`

**Kui puudub** → JPA Buddy:
1. Paremklõps kasutaja entiteedil → New → DTO
2. **Package** → sinu kontrolleri alampaketi `dto`
3. **DTO class name** → mõistlik nimi (taskifailis on nimi olemas)
4. **MapStruct Interface** → loo uus plussmärgiga
5. Vali väljad; seotud entiteedi puhul (roll) vali **Flat**

> **Mitme allikaga DTO:** Mõni väli (profiili olemasolu, e-post) ei tule kasutaja entiteedist üldse. Lisa need DTO-le käsitsi taskifaili näidise järgi. Kontrolli ka tüüpe: kas `hasProfile` on `Boolean` või `boolean`? Mis vahe neil JSON-is on?

---

## Samm 4 — Mapper ja ülejäänud andmete kogumine

### Mapper

Ava JPA Buddy loodud mapper. Eemalda mittevajalikud meetodid, jäta üks — entiteedist DTO-ks. Nimeta see konventsiooni järgi:

```java
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

Lisa `@Mapping` annotatsioonid:

```java
@Mapping(source = "", target = "")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

> **IntelliJ vihje:** Kliki `target = ""` jutumärkide vahele → **Ctrl+Space**. Näed kõiki DTO välju — tee igaühele oma rida.

Täida kõik read. Välja, mida kasutaja entiteet ei kata, märgi `ignore = true`:

```java
@Mapping(source = "seotudObjekt.väli", target = "dtoVäli")
@Mapping(source = "tavaveerg", target = "samaNimiDtos")
@Mapping(ignore = true, target = "väliMuustAllikast")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

> **Mõtle:** Millised kaks välja jäävad `ignore = true`? Kust need hiljem service'is tulevad?

### Profiil — Optional!

Nüüd on vaja teada, kas kasutajal on profiil. Profiili tabelis on `user_id` — kasutajal võib profiil **puududa** (esimene sisselogimine).

- Loo profiili repositooriumisse päring JPA Buddy abil (vt allpool).
- Mõtle: kas tulemus peaks olema `Optional<...>`?
- **Otsusta teadlikult:** profiili puudumine ei ole viga (taskifail: „Profiili puudumine ei tohi põhjustada `/api/me` viga“). Seega `orElseThrow` siia ei sobi. Mis sobib?

Kaks ülejäänud välja täidad service meetodis pärast mappimist:
- profiili olemasolu;
- e-post — kui profiil on olemas, siis ühest kohast, kui ei ole, siis teisest (taskifaili tabel ütleb, kust).

> **Konventsioon:** Kui meetod sisaldab tingimuslikku loogikat ja muudab DTO-d, kasuta `handle`-prefiksit ja anna DTO parameetrina sisse (`handle...(DtoTüüp dto, ...)`).

### Service meetodi lõpetamine

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    EntiteetTüüp entiteet = ...;
    TagastatavDtoTüüp dto = mapperMuutuja.toDtoKlassiNimi(entiteet);
    // ülejäänud väljad dto-le
    return dto;
}
```

> **IntelliJ vihje:** `return dto;` `void` meetodis → **Alt+Enter** → IntelliJ parandab tagastustüübi.

---

## Samm 5 — Repository (täiendavad päringud)

### Uue meetodi loomine JPA Buddy abil

Seda vajad kahes kohas: kasutaja otsimine Google'i identifikaatori järgi (Samm 0) ja profiili otsimine kasutaja järgi (Samm 4).

1. Ava repository interface → JPA Buddy paneel
2. Vali **Query**
3. Meetodi tüüp: **Find instance** (või mõtle, kas **Exists** oleks siin piisav?)
4. **Wrap type**: `Optional<EntiteetKlass>`
5. Lisa **query condition** — milline veerg?
6. **Advanced** → **Named parameters**

Pärast loomist:
- Asenda ebamäärane parameetrinimi konkreetsega ja muuda ka `@Query`-s.
- **Nimetamine (`backend/CLAUDE.md`):** meetodi nimi peab mainima, mida ta tagastab — nt `findEntiteetBy(...)`, mitte `findBy(...)`.

### Optional käsitlemine

Kui meetod tagastab `Optional<...>`:
- **`orElseThrow(...)`** — kui väärtus on kohustuslik;
- **`isPresent()` / `orElse(...)` / `map(...)`** — kui puudumine on lubatud.

Ära lase `Optional`-il lihtsalt seista.

---

## Samm 6 — tagasi RestController'isse

Lisa kontrolleri meetodisse `return`:

```java
public TagastatavTüüp meetodiNimi(@SessiooniAnnotatsioon PrincipaliTüüp principal) {
    return teenuseMuutuja.meetodiNimi(...);
}
```

> **IntelliJ vihje:** `void` meetodis `return` → **Alt+Enter** → **"Change return type"**.

---

## Samm 7 — kood ilusaks (refactor)

### Make it work → Make it beautiful

**Extract Method IntelliJ'ga:** märgi service'is koodilõik (nt e-posti valik) → paremklõps → Refactor → Extract Method.

> **Tähelepanu:** IntelliJ annab parameetriks sageli terve objekti. Vaata üle, kas helper vajab kogu objekti või ainult üht välja.

Enne:
```java
kontrolliMidagiHelper(dtoObjekt);
```

Pärast (parem — anna edasi ainult vajalik):
```java
kontrolliMidagiHelper(dtoObjekt.getMingiVäli());
```

### Meetodite järjekord

1. `public` meetodid enne
2. `private` meetodid pärast
3. Väljakutsumise hierarhia järgi — peameetod üleval, helperid all

---

## Veaolukorrad (taskifailist)

| Olukord | Mida oodata | Kus see tekib |
|---|---|---|
| Sessioon puudub | 401, tühi body | Turvaseadistus (Samm 0), mitte sinu kood |
| Kasutaja blokeeritud (`status = 'B'`) | Sisselogimine ebaõnnestub → suunamine `/?loginError` | OIDC teenus (Samm 0) |
| Profiil puudub | **Ei ole viga** — 200 ja `hasProfile: false` | Service (Samm 4) |
| Ootamatu andmebaasi tõrge | 500 `INTERNAL_SERVER_ERROR` | Praegune `RestExceptionHandler` seda veel ei käsitle — mõtle, kas lisad |

---

## Meetodi palve

> *„Kui sa kutsud välja mingi meetodi, mis tagastab midagi, ja sa soovid selle infoga midagi edasi teha, siis pane see kohe muutujasse.“*

---

## Kokkuvõte ja kontrollnimekiri

- [ ] OAuth sõltuvus, seadistus (keskkonnamuutujad, mitte saladus failis) ja Vite proxy on paigas
- [ ] Principal kannab `app_user.id` väärtust; OIDC teenus seob kasutaja `google_sub` järgi ja loob uue `customer` kasutaja ilma profiilita
- [ ] Turvaseadistus: avalikud GET-teed, 401 sisse logimata API päringule, success `/`, failure `/?loginError`
- [ ] RestController klass on olemas `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor` annotatsiooniga
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] Kasutaja ID tuleb **ainult** sessioonist
- [ ] Service klass on olemas `@Service`, `@RequiredArgsConstructor` annotatsiooniga
- [ ] `findById().orElseThrow()` on `getValid<Entiteet>By(...)` meetodis
- [ ] Repository meetoditel on `@Query` Named parameters stiilis ja nimi ütleb, mida tagastatakse
- [ ] Mapper on `@Mapper(componentModel = "spring")` ja **iga** DTO väli on `source` või `ignore = true`
- [ ] Profiilita kasutaja saab 200 ja `hasProfile: false`; e-post tuleb siis Google'i sessioonist
- [ ] Meetodite järjekord: `public` enne, `private` pärast
- [ ] Kood kompileerub ja endpoint on Swagger UI-s nähtav

---

> **Järgmine samm:** Testi. `/api/me` vajab sessiooni, seega logi esmalt sisse läbi frontendi (`http://localhost:5173/oauth2/authorization/google`) ja ava siis samas brauseris `http://localhost:5173/api/me`. Demokasutajana (nt admin Marko) sisse logimiseks vaata `googlega_login.md` jaotist 9. Kontrolli, et vastus vastab taskifaili näidisele.
