# Juhend: GET /api/cities

**Taski fail:** `docs/tasks/backend/ToolsView/Linnade-nimekirja-paring.md`
**Kontroller:** uus kontroller `controller/<ressurss>/` alla (nimi on sinu otsus)
**Implementeerimise voog:** RestController → Service → Repository → Service → Mapper → RestController

---

## Sissejuhatus

See endpoint tagastab kõik linnad tabelist `city`, et ToolsView otsinguvormi linna rippmenüü saaks täidetud. Päringul pole sisendit ja see ei muuda andmeid, seega on see klassikaline „viiteandmete loendi“ GET-päring. Harjutuse käigus lood projekti **esimese** kontrolleri, service'i, DTO ja mapperi ning õpid, kuidas entiteetide list DTO-de listiks teisendada ja kuidas tulemus kindlas järjekorras tagastada.

> **Mis on praegu olemas?** Base-pakett on `ee.toolrental`. Olemas on `persistence/city/City.java` (väljad `id`, `cityName`) ja tühi `CityRepository` (laiendab `JpaRepository`-t). Kaustu `controller/` ja `service/` **veel pole** — need tuleb luua.

---

## Samm 1 — RestController

### Mida teha?

Kontrollerit veel ei ole, seega lood esimese kontrolleri klassi. Mõtle, millise **ressursi** alla see endpoint kuulub, ja loo selle jaoks alampakett `controller/` alla (projekti konventsioon: igal domeenialal oma alampakett, kuhu tulevad ka selle DTO-d).

- Kaust: `backend/src/main/java/ee/toolrental/controller/`
- Loo IntelliJ'ga: paremklõps paketil → New → Package, seejärel New → Java Class

Vajalikud klassiannotatsioonid:

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KontrolleriKlass {
    // ...
}
```

### Meetodi loomine

Alusta meetodist **ilma mappingannotatsioonideta**:

```java
public void meetodiNimi() {
    // tühi meetod esialgu
}
```

> **Mõtle:** Mis on selle meetodi hea nimi? Endpoint tagastab *kõik* kirjed — kuidas seda nimes väljendada?
> Sellel endpointil sisendparameetreid pole — vaata taskifaili „Sisend“ sektsiooni.

Seejärel lisa:
1. **Mappingannotatsioon** — `@GetMapping` koos õige teega (taskifailist)
2. **Swagger annotatsioonid** — `@Operation` (summary peab kirjeldama täpselt seda, mida endpoint teeb) ja `@ApiResponses`

> **Mõtle:** Milliseid vastuskoode taskifail ette näeb? Vaata „Veaolukorrad“ sektsiooni — sellel endpointil ei ole 404/400 ärivigu.

### Service klassi ettevalmistus

Kaust `backend/src/main/java/ee/toolrental/service/` puudub — loo pakett ja sinna service klass (vali nimi ressursi järgi):

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {
    // ...
}
```

Lisa service muutuja kontrolleri klassi ja kutsu service meetodit:

```java
private final TeenusKlass teenuseMuutuja;

public void meetodiNimi() {
    teenuseMuutuja.meetodiNimi();
}
```

> **IntelliJ vihje:** Kui `teenuseMuutuja.meetodiNimi()` on punasega alla joonitud,
> vajuta **Alt+Enter** → **"Create method in TeenusKlass"**.

---

## Samm 2 — Service ja repository päring

### Mida teha?

Service meetod ei saa sisendit — tema ülesanne on pärida tabelist `city` **kõik** read. Ava service klass ja mine äsja loodud meetodisse.

Alusta kirjutama repositooriumi muutuja nime:

```java
public void meetodiNimi() {
    entiteetRep  // <- kirjuta algus siia
}
```

> **IntelliJ vihje:** Kirjuta muutuja nime algus ja IntelliJ pakub vastavat repositooriumi.
> Vajuta **Tab** → repositoorium lisatakse klassiväljana.

**Küsi endalt:** Kas `JpaRepository` pakub valmis meetodit, mis tagastab kõik read?

> **Aga järjekord!** Taskifail nõuab järjestust `city.id ASC`. Kas baasmeetod garanteerib mingit järjekorda? (Vihje: SQL-is ilma `ORDER BY`-ta järjekorda ei garanteerita.)
> Sul on kaks teed: kasutada baasmeetodi varianti, mis võtab sortimise parameetri, **või** luua repository'sse oma päring (vt „Uue meetodi loomine JPA Buddy abil“ Samm 5-s). Projekti tava on eelistada lühikese, selge nimega oma meetodit, mis ütleb, mida ta tagastab.

Kui repository meetod tagastab tulemuse, **pane see kohe muutujasse** („Meetodi palve“).

---

## Samm 3 — DTO klass

### Mida teha?

Nüüd, kui entiteetide list on käes, loo väljundi DTO. Taskifail ütleb täpselt: DTO sisaldab ainult kahte välja — linna ID ja linna nime — ning nende nimed on JSON-is kirjas („Väljund“ sektsioon).

> **Tähelepanu:** DTO väljade nimed **ei kattu** entiteedi väljadega. Vaata taskifaili JSON-näidist ja võrdle `City.java` väljadega — kus on erinevus?

DTO asub kontrolleri alampaketi `dto/` kaustas (nt `controller.ressurss.dto`).

**Kasuta JPA Buddy abi:**

1. Paremklõps `City` klassil → New → DTO
2. Kontrolli valikud:
    - **Package** → sinu kontrolleri alampaketi `dto` pakett
    - **DTO class name** → taskifail annab nime ette
    - **MapStruct Interface** → loo uus plussmärgiga (vt Samm 4 — mõtle, kuhu paketti mapper kuulub)
    - **Mutable** → jäta märgituks
3. Peale loomist kontrolli DTO üle ja nimeta väljad ümber taskifaili järgi

---

## Samm 4 — Mapper

### Mida teha?

Entiteetide list tuleb teisendada DTO-de listiks.

> **Kuhu mapper käib?** Vaata `backend/CLAUDE.md` kihtide struktuuri — seal on kirjas, millises kihis MapStructi mapperid asuvad.

Ava mapper interface. JPA Buddy loob vaikimisi mitu meetodit — eemalda mittevajalikud.

Nimeta meetodid konventsiooni järgi:

```java
// Ühele DTO-le — @Mapping annotatsioonid käivad SIIA
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);

// Lista DTO listiks — annotatsioonideta, MapStruct genereerib ise
List<TagastatavDtoTüüp> toDtoKlassiNimid(List<EntiteetTüüp> entiteedid);
```

> **Levinud segadus:** `@Mapping` annotatsioonid ei tööta list-meetodil. Need käivad alati **üksiku objekti** meetodile; list-meetod (mitmuses nimi!) jääb tühjaks signatuuriks ja MapStruct kutsub üksiku-objekti meetodit iga elemendi kohta.

Lisa `@Mapping` annotatsioonid:

```java
@Mapping(source = "", target = "")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

> **IntelliJ vihje:** Kliki `target = ""` jutumärkide vahele → vajuta **Ctrl+Space**.
> IntelliJ näitab kõiki DTO välju — nii saad teha iga välja jaoks oma `@Mapping` rea.

> **Projekti tava:** Kaardista **iga** target-väli eksplitsiitselt — ka siis, kui nimi kattuks. `build.gradle`-is on `unmappedTargetPolicy=IGNORE`, mis tähendab, et unustatud väli **ei anna kompileerimisviga**, vaid jääb lihtsalt `null`-iks. Seega kontrolli ise!

### Service meetodi lõpetamine

Kutsu mapperi list-meetod service'is välja ja tagasta tulemus:

```java
public void meetodiNimi() {
    List<EntiteetTüüp> entiteedid = entiteetRepository.meetodiNimi();
    return mapperMuutuja.toDtoKlassiNimid(entiteedid);
}
```

> **IntelliJ vihje:** Tagastustüüp on veel `void` — vajuta **Alt+Enter** punasel joonel → IntelliJ parandab tagastustüübi.

> **Mõtle:** Mis juhtub, kui tabel on tühi? Taskifail nõuab `[]`, mitte `null`-i ega 404-t. Kas su praegune kood juba täidab selle nõude?

---

## Samm 5 — Repository (kui otsustasid oma päringu kasuks)

### Uue meetodi loomine JPA Buddy abil

Kui Samm 2-s otsustasid sortimise jaoks oma päringu teha, mine `CityRepository` faili:

1. Ava JPA Buddy paneel (paremklõps repository klassis → JPA Buddy)
2. Vali **Query**
3. Meetodi tüüp: **Find collection**
4. **Wrap type**: `List<EntiteetKlass>`
5. Query conditioneid pole vaja — tahame kõiki ridu
6. **Order By Attributes** — lisa sortimine taskifaili nõude järgi
7. Anna meetodile lühike nimi, mis ütleb, **mida** ta tagastab (vt `backend/CLAUDE.md` „Repositooriumi meetodi nimetamine“)

Pärast loomist vaata genereeritud `@Query` üle — kas `ORDER BY` on sees ja õiges suunas?

---

## Samm 6 — tagasi RestController'isse

Täienda kontrolleri meetodit — lisa `return` lause:

```java
public void meetodiNimi() {
    teenuseMuutuja.meetodiNimi();  // <- enne: tulemus kasutamata
}
```

> **IntelliJ vihje:** Lisa `return` → **Alt+Enter** → "Change return type".

Tulemus:

```java
public TagastatavTüüp meetodiNimi() {
    return teenuseMuutuja.meetodiNimi();
}
```

### Avalik ligipääs

Taskifail nõuab, et endpoint töötab **ilma sisselogimiseta**. Projektis on `spring-boot-starter-security-oauth2-client` sõltuvus — kui eraldi Security konfiguratsiooni (`SecurityFilterChain`) pole, kaitseb Spring Boot vaikimisi **kõiki** endpoint'e.

> **Mõtle:** Kas projektis on juba Security konfiguratsioon (vt Google-sisselogimise task)? Kui on, kontrolli, et `/api/cities` oleks seal avalikuks lubatud. Kui pole, arutame koos, kuidas sellega praegu toimida.

---

## Samm 7 — kood ilusaks (refactor)

### Make it work → Make it beautiful

See endpoint on lihtne — tõenäoliselt pole midagi ekstraktida. Vaata siiski üle:

- Kas muutujate nimed peegeldavad täistüüpi (vt `backend/CLAUDE.md` „Muutujate nimetamine“)?
- Kas `@Operation` summary kirjeldab täpselt, mida endpoint teeb?
- Kas list-mapperi meetodi nimi on mitmuses?

**Extract Method IntelliJ'ga** (kui vaja): märgi koodilõik → paremklõps → Refactor → Extract Method. Vaata üle, kas helper vajab tervet objekti või ainult üht välja.

### Meetodite järjekord

1. `public` meetodid enne
2. `private` meetodid pärast
3. Väljakutsumise hierarhia järgi — peameetod üleval, helperid all

---

## Täiendav: 500 veaolukord

Taskifail kirjeldab andmebaasi tõrke korral 500 vastust kindla `ApiError` kujuga (`errorCode: "INTERNAL_SERVER_ERROR"` + eestikeelne sõnum). Praegune `RestExceptionHandler` seda ei kata. Kui põhiosa töötab, mõtle:

> Milline exception'i tüüp tekib, kui andmebaasipäring ebaõnnestub? Kuidas lisada `RestExceptionHandler`-isse uus `@ExceptionHandler`, mis tagastab 500 ja ei paljasta SQL-i ega stack trace'i? Vaata olemasolevaid handlereid samas failis eeskujuks.

---

## Kokkuvõte ja kontrollnimekiri

- [ ] RestController klass on olemas `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor` annotatsiooniga, õiges alampaketis
- [ ] Kontrolleri meetodil on `@GetMapping`, `@Operation` ja `@ApiResponses`
- [ ] Service klass on olemas `@Service`, `@RequiredArgsConstructor` annotatsiooniga
- [ ] Tulemus on sorteeritud `id` järgi kasvavalt
- [ ] Mapper interface on olemas `@Mapper(componentModel = "spring")` annotatsiooniga, õiges kihis
- [ ] Kõik `@Mapping` annotatsioonid on täidetud — ükski DTO väli ei ole kaardistamata
- [ ] List-meetod on mitmuses ja ilma `@Mapping` annotatsioonideta
- [ ] DTO-l on täpselt kaks välja, nimed vastavad taskifaili JSON-ile
- [ ] Endpoint on avalikult kättesaadav (Security)
- [ ] Meetodite järjekord: `public` enne, `private` pärast
- [ ] Kood kompileerub ja endpoint on Swagger UI-s nähtav

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`)
> ja kontrolli, et vastus vastab taskifailis toodud 3 linnaga JSON-ile.
