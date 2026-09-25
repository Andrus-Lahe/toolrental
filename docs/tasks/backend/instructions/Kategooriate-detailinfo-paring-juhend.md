# Juhend: GET /api/categories/detailed-info

**Taski fail:** [Kategooriate-detailinfo-paring.md](../HomeView/Kategooriate-detailinfo-paring.md)
**Kontroller:** `CategoryController.java`
**Implementeerimise voog:** RestController → Service → Repository → Service → Mapper → RestController

## Sissejuhatus

Endpoint annab avalehele kategooriate nimed, kirjeldused ja pildid. Harjutus ühendab HTTP päringu, andmebaasist lugemise ja DTO-ks teisendamise. Baaspakett on `ee.toolrental`; juhendi koostamisel vastavad controller/service/persistence klassid puuduvad.

[Implementatsiooniplaan](../HomeView/Kategooriate-detailinfo-paring-IMPLEMENTATSIOON.md) on tehniline abimaterjal. Õppimise järjekord algab kontrollerist. Selle GET ülesande jaoks pole vaja plaani üldiseid sisendi valideerimise, admini request DTO ega muutvate päringute samme.

## Samm 1 — RestController

### Mida teha?

Loo IntelliJ Project paneelis pakett `ee.toolrental.controller.category` ja sellesse `CategoryController` (New → Java Class). Kontrolli package-rida. See klass võtab vastu veebipäringu; andmebaasi lugemine ei kuulu controllerisse.

Uue kontrolleri tehnilist kuju näitab järgmine generiline näide. Asenda näidisnimed enda valikutega.

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NaidisController {
}
```

Loo algul tühi meetod ilma mapping-annotatsioonita. Mõtle taski API tee põhjal sobivale nimele. Taski „Sisend“ jaotise järgi otsusta, kas meetod vajab üldse parameetreid; ära lisa neid lihtsalt näidise jäljendamiseks.

Seejärel lisa `@GetMapping`, `@Operation` ja `@ApiResponses`. Klassi- ja meetoditaseme tee peavad kokku andma taski URL-i. Swagger peab kirjeldama tegelikku 200 vastust ja taskis määratud 500 viga.

### Service klassi ettevalmistus

Loo service klass paketti `ee.toolrental.service`, kasuta `@Service` ja `@RequiredArgsConstructor`. Seo see controllerisse `private final` väljana. Kirjuta service'i meetodi väljakutse ning kasuta punasel väljakutsel **Alt+Enter → Create method**. Selles etapis võib tagastustüüp olla ajutine; lõpus peab see vastama taskile.

**Kontrollpunkt:** kas controller delegeerib töö service'ile, selle asemel et ise andmebaasi lugeda?

## Samm 2 — Service ja esimene repository päring

### Mida teha?

Ava loodud service meetod. Uuri `docs/database/2_create.sql` tabeleid `category` ja `category_image`: milline tabel annab loendi põhielemendid, milline valikulised pildid?

Entiteedid ja repositooriumid puuduvad. Kasuta JPA Buddy andmebaasist entiteetide loomise võimalust või loo JPA klassid käsitsi pakettides `ee.toolrental.persistence.category` ja `ee.toolrental.persistence.categoryimage`. Kontrolli PK-sid, veerutüüpe ning seose kohustuslikkust SQL-i järgi. Ära lase generaatoril skeemi muuta.

Repository liides kuulub sama entiteedi paketti ja laiendab `JpaRepository`-t. Seo see service'isse. Kirjuta välja algus, kasuta **Ctrl+Space**, kinnita sobiv pakkumine **Tab** abil; puuduvat tüüpi saab luua **Alt+Enter** kaudu. IDE pakkumised sõltuvad paigaldatud pluginatest.

**Mõtle:** kas tavaline loendipäring rahuldab taski järjestuse ja pildita kategooriate nõudeid? Vali päringu kuju alles pärast nende kontrollimist.

Kui meetod tagastab andmeid, millega jätkad, pane tulemus kohe muutujasse. Ära jäta tagastusväärtust kasutamata.

## Samm 3 — DTO klass

### Mida teha?

Loo `CategoryDetailedInfoDto` paketti `ee.toolrental.controller.category.dto`. Loe taski väljundit: millised väljad ja tüübid peavad HTTP vastuses olema ning millised andmebaasiväljad sinna ei kuulu?

JPA Buddy puhul vali entiteedil **New → DTO**, määra controlleri DTO-pakett, soovi korral Mutable ja MapStruct Interface. Vaata generaatori tulemus käsitsi üle: pildi väljundkuju ei pruugi kattuda andmebaasi tüübiga. Mitme allika puhul võib DTO koostada taski väljundi järgi käsitsi.

**Kontrollpunkt:** kas DTO kirjeldab ainult taski väljundit ega väljasta tervet JPA entiteeti?

## Samm 4 — Mapper ja ülejäänud andmete kogumine

### Mida teha?

Loo mapper liides entiteedi juurde paketti `ee.toolrental.persistence.category`. Kasuta Springi komponendina töötavat MapStruct mapperit. Eemalda genereeritud meetodid, mida selle päringu jaoks vaja pole.

Kohe pärast üksiku objekti teisendusmeetodi signatuuri loomist lisa tühi mapping-rida. Järgmine näide on täitmata mall, mitte valmis lahendus.

```java
@Mapping(source = "", target = "")
NaidisDto toNaidisDto(NaidisEntity naidisEntity);
```

Aseta kursor `target = ""` jutumärkide vahele ja vajuta **Ctrl+Space**. Koosta iga DTO target-välja jaoks eraldi rida. Igal real peab lõpuks olema sobiv `source` või `ignore = true`; ka sama nimega väljad kaardista eksplitsiitselt.

Loendi korral on vaja nii üksiku objekti kui loendi meetodit. Väljade `@Mapping` annotatsioonid käivad üksiku objekti meetodile; loendi meetod kasutab seda iga elemendi jaoks. Pane üksikmeetodi nimi ainsusesse, loendimeetodi nimi mitmusesse.

**Mõtle:** millised väljundväljad tulevad põhientiteedist, millised vajavad täiendavat seost või teisendust? Kui jätad välja mapperis ignoreerituks, peab olema teadlik otsus, kus selle väärtus hiljem täidetakse.

Pildi töötlemiseks võrdle taskis nõutud esitust olemasoleva `StringBytesConverter` käitumisega. Ära eelda, et baitide muutmine tekstiks annab automaatselt soovitud kodeeringu.

Valikulise seose korral käsitle `Optional`-i teadlikult: kas puudumine on taski järgi viga või lubatud? Ära vali erindi viskamist pelgalt sellepärast, et väärtus puudub.

Service peab lõpuks tagastama DTO-de tulemuse. Kui tagastustüüp on veel `void`, kasuta return-lause juures **Alt+Enter** ja kontrolli pakutud tüüpi.

## Samm 5 — Repository täiendavad päringud

### Mida teha?

Kontrolli, kas valitud päring hangib kogu vajaliku info. Täiendavat repository päringut pole vaja lisada, kui esimene päring juba katab nõuded.

JPA Buddy abil vali **Query → Find collection**, loendi tagastustüüp ning taskile vastav järjestus. Kui sisendparameetreid lisandub, kasuta **Named parameters**; sellel endpoint'il HTTP sisendeid pole. Projekti kohandatud päringud kirjutatakse JPQL-ina `@Query` annotatsiooni sisse.

**Mõtle:** kuidas säilitada kategooria ka siis, kui pildikirjet pole? Kuidas kontrollida järjestust nii, et testandmete ID järjekord ei peidaks viga? Vaata ka tehtavate andmebaasipäringute arvu, mitte ainult JSON-i.

Nimeta repository meetod tagastatava subjekti järgi. Ära kopeeri generaatori liiga pikka nime kontrollimata.

## Samm 6 — Tagasi RestController'isse

### Mida teha?

Tagasta service'i tulemus. Võrdle controlleri ja service'i tagastustüüpe ning taski JSON massiivi. Controller ei tagasta entiteete ega ehita ise pilditeksti.

Veendu, et endpoint on avalik. Kui projektile on selleks ajaks lisatud SecurityConfig, kontrolli selle reegleid; kogu OAuth lahendust ei pea avaliku GET ülesande pärast looma.

Võrdle taski ainsat tehnilist viga olemasoleva `RestExceptionHandler`-iga. Planeeri 500 käsitlus nii, et vastaks täpne ApiError leping ning klient ei saaks SQL-i ega stack trace'i. Tühi tulemus ja puuduv pilt pole ärivead.

## Samm 7 — Kood ilusaks

### Mida teha?

Kui päring töötab, korrasta vastutused ja nimed. Korduva või eraldi tähendusega lõigu puhul kasuta **Refactor → Extract Method**. Kontrolli, kas abimeetod vajab tervet objekti või ainult üht väärtust. Tingimuslikku DTO muutmist kirjeldab projekti `handle`-prefiks.

Pane public meetodid ette ja private abimeetodid nende järele kutsumise järjekorras. Kasuta IntelliJ **Optimize Imports** ja **Reformat Code**. Genereeritud MapStruct implementatsiooni käsitsi ei muudeta.

## Kokkuvõte ja kontrollnimekiri

- [ ] Controlleril on vajalikud Springi annotatsioonid, õige URL ja Swaggeri vastuste kirjeldused.
- [ ] Service vastutab andmete koostamise eest; repository andmete pärimise eest.
- [ ] JPA seosed vastavad SQL skeemile ning kohandatud päringud projekti JPQL konventsioonile.
- [ ] Mapperi iga target-väli on eksplitsiitselt käsitletud; üksiku objekti ja loendi meetodid on eraldi.
- [ ] Vastuses on täpselt taski neli välja ja kõik kategooriad; tööriistade olemasolu ei mõjuta tulemust.
- [ ] Järjestus vastab taskile ka võrdsete järjestusväärtuste korral.
- [ ] Pildiesituse pöördteisendus annab algsed baidid; puuduv pilt/kirjeldus ja tühi loend vastavad taskile.
- [ ] Andmebaasitõrge annab täpse 500 vastuse; lugemine ei muuda andmeid.
- [ ] Kood kompileerub; automaattestid kontrollivad ka servajuhte, mitte ainult 200 vastust.
- [ ] Käivitatud kontrollide tegelikud tulemused on läbi vaadatud.

Käivita backend ja proovi endpoint'i Swaggeris (`http://localhost:8080/swagger-ui/index.html`, kui rakendus töötab pordil 8080). Võrdle tulemust taski ning impordiandmetega. Swaggeri käsitsi katse ei asenda sortimise, puuduva pildi ja veavastuse teste.
