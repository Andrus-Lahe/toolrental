# Community Tool Rental — lahtised küsimused ja vastuolud

Uuendatud: 24.09.2026

Analüüs põhineb praegusel töökoopial, sh olemasolevatel commit’imata Git-muudatustel dokumentides. Kontrollitud allikad:

- [SQL skeem](../database/2_create.sql), [algandmed](../database/3_import.sql) ja [lähtestamine](../database/1_reset_database.sql).
- [Backend](../../backend/src/main/java/ee/toolrental), [seadistus](../../backend/src/main/resources/application.properties), [build](../../backend/build.gradle) ja [juhised](../../backend/CLAUDE.md).
- [Frontend](../../frontend/src), [router](../../frontend/src/router/index.js) ja [Vite seadistus](../../frontend/vite.config.js).
- [Avalehe](../balsamic/notes/HomeView-markmed.md), [otsingu](../balsamic/notes/ToolsView-markmed.md), [minu tööriistade](../balsamic/notes/MyToolsView-markmed.md) ja [taotluse kinnituse](../balsamic/notes/BookingConfirmationView-markmed.md) märkmed.
- [Praeguse maketi PDF](../balsamic/notes/Laenukas.pdf), 14 lehekülge.

Backendis on käivitusklass ja veakäsitluse infrastruktuur, kuid puuduvad domeeni controller'id, service'id, Entityd, repository'd ja mapperid. Frontendis on toorikvaated `/` ja `/test`. Balsamiqi API kirjeldused ei ole töötavad endpoint'id; `MyToolsView` märkmetes on suur osa lepingust sõnaselgelt ettepanek.

Lahendatud küsimused on eemaldatud. Osaliselt lahendatud küsimustes on alles ainult lahtine osa või tegelik allikate vastuolu. Varasemad ID-d on säilitatud, uued leiud algavad Q76-st. Puuduv teostus üksi ei tähenda lahtist tooteotsust. See nimekiri ei anna luba muuta skeemi ega äriloogikat.

## Prioriteedid

| Prioriteet | Tähendus |
|---|---|
| P0 — esmalt lahendada | Mõjutab arhitektuuri, andmete korrektsust või põhiprotsessi läbimist. Lahendada enne vastava osa teostamist. |
| P1 — täpsustada MVP ülesannetes | Vajalik üheselt mõistetava käitumise ja kasutajaliidese jaoks. Lahendada vastava ülesande alguses. |

P1 ei tähenda, et küsimuse võib ignoreerida. Kõigi punktide lahendamine ei nõua uue funktsiooni loomist: sobiv otsus võib olla ka „MVP-s ei toetata”.

## Kõige olulisemad otsused

1. Täpsustada praeguse SQL-i ja makettide põhjal staatuste tähendus ja kuupäevapiirid.
2. Määrata blokeerivad staatused, ootel taotluse aegumine ja samaaegsete päringute kaitse.
3. Täpsustada Google'iga autentimise, registreerimise ja rakenduse sessiooni turvaline seos.
4. Otsustada kalendriintegratsiooni ja kasutajateavituste ulatus ning tõrgete käsitlus.
5. Otsustada makettides nähtavate sõnumite, hinna ja tööriista saadavusperioodi MVP ulatus.
6. Ühtlustada andmebaasi skeemi valik skriptides, rakenduses ja käivitusjuhises.

## P0 — esmalt lahendatavad küsimused

### B. Rendiperioodi ja saadavuse põhireeglid

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q05 | Lõppkuupäev | Kas see kuulub rendiperioodi sisse? Kas samal päeval võib alata järgmine rent? |
| Q06 | Kestuse arvutamine | Kas 21.–23. september tähendab kahte või kolme päeva? Reegel peab vastama perioodi piiridele. |
| Q10 | Staatuste tähendus ja blokeerimine | SQL lubab ainult `P`, `C`, `R`; PDF lk 4 määrab `P` ootel ja `C` kinnitatud, `MyToolsView` ettepanek tõlgendab `R` tagasilükkamisena. PDF lk 4 jätab kattuvuse piirangu eraldi kokkuleppeks. Kinnitada staatuste tähendused ja mõju saadavusele. |
| Q07 | Tänane päev ja minevik | Kas rent võib alata täna? Kas minevikus algav rent on selgesõnaliselt keelatud? |
| Q08 | Ajavöönd | `Europe/Tallinn` on `MyToolsView` märkmetes alles ettepanek. Kinnitada kogu rakenduse tänase päeva, aegumise ja ajatemplite ajavöönd. |
| Q11 | Korduskontroll kinnitamisel | Mida kontrollida uuesti, kui tööriista saadavus või kasutaja staatus muutus või rendi alguskuupäev saabus? |

### C. Broneeringu elutsükkel

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q13 | Ootel taotluse kehtivusaeg | Kui kaua võib `P` taotlus vastuseta jääda? Kui Q10 otsusega blokeerib see kuupäevi, tuleb määrata ka blokeeringu vabastamise reegel. |
| Q15 | Vastuseta taotlus rendi alguses | Kas seda võib veel kinnitada või suletakse see automaatselt? |
| Q24 | Lubatud staatusemuutused | PDF kirjeldab `P → C`; tagasilükkamise `R` tõlgendus on märkmetes ettepanek. Kinnitada lubatud üleminekud, otsustajad ning korduva otsuse vastus koos Q10-ga. |

### D. Samaaegsed tegevused ja osalised tõrked

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q25 | Kaks samaaegset taotlust | Kui kattuvad broneeringud keelatakse, milline mehhanism tagab selle samaaegsel loomisel ja kinnitamisel? Blokeerivad staatused sõltuvad Q10 otsusest. |
| Q26 | Samaaegne kinnitamine ja tagasilükkamine | Kuidas töödeldakse otsuseid kahest vahekaardist või korduvat otsusepäringut? |
| Q27 | Kordussaatmine pärast vastuse kadumist | Kui taotlus loodi, kuid vastus ei jõudnud brauserisse, kuidas eristada korduspäringut uuest taotlusest? |
| Q28 | Tehingu piirid | Millised muudatused salvestatakse koos ja millal loetakse toiming lõpetatuks? |
| Q31 | Teavituse saatmise tõrge | Kinnituse märkmed lubavad kasutajat otsusest teavitada, kuid kanal vajab täpsustamist (Q70). Mis juhtub saatmise tõrkel: kas broneering säilib, kes kordab saatmist ja mida näeb kasutaja? |
| Q32 | Backendi taaskäivitumine | Kuidas avastatakse sammude vahel pooleli jäänud toimingud ja jätkatakse neid? |

### E. Registreerimise ja autentimise põhiprotsess

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q41 | Lõpetamata registreerimine | PDF lk 3 näeb ette `app_user` ja `profile` loomise ühes tehingus pärast vormi täitmist. Lahtine on Google'iga autenditud, kuid registreerimata kasutaja ajutine olek ning käitumine olemasoleva profiilita `app_user` korral; SQL lubab sellist kirjet. |
| Q42 | Rakenduse sessioon | PDF lk 3 kirjeldab `userId` ja `roleName` salvestamist `sessionStorage`-isse, kuid see ei kirjelda serveri kontrollitavat autentimist. Puuduvad sessiooni või tokeni leping, kehtivusaeg ja väljalogimise mehhanism; ka koodis pole autentimiskihti. |

### F. Kalendriintegratsiooni ulatus

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q33 | Kalendriintegratsiooni ulatus | SQL-is on `booking.google_event_id`, kuid praegune rakenduskood ega vaadete märkmed ei määra selle kasutamist. Kas Google Calendar kuulub MVP-sse? Kui jah, tuleb kirjeldada sündmuse loomise hetk, sihtkalender, õigused ja tõrgete käsitlus. |

## P1 — täpsustused vastavate MVP ülesannete jaoks

### G. Renditaotluse lisareeglid

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q09 | Perioodi piirangud | Kas kehtib minimaalne või maksimaalne kestus ja kui kaugele ette võib broneerida? Ka piirangute puudumine tuleb fikseerida. |
| Q18 | Rentniku tühistamine | Kas võib tagasi võtta `P` taotluse või tühistada `C` broneeringu? Milliste piirangutega? |
| Q19 | Omaniku tühistamine | Kas pärast kinnitamist võib broneeringu tühistada ja kuidas see erineb taotluse tagasilükkamisest? |
| Q20 | Kuupäevade muutmine | Kas olemasolevat taotlust võib muuta või tuleb see tühistada ja uus luua? |
| Q21 | Enda tööriista rentimine | PDF lk 4 kirjeldab broneerijat kasutajana, kes pole tööriista omanik, kuid sama lehe märkus jätab backendis oma tööriista rentimise piirangu eraldi kokkuleppeks. Kinnitada serveri kontroll ja veavastus. |
| Q22 | Korduv taotlus ja piirangud | Kas pärast tagasilükkamist võib kohe samale perioodile uuesti taotleda? Kas aktiivsete taotluste arv on piiratud? |

### H. Õigused, profiil ja asukoht

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q45 | `B` mõju | Kas keelatud on ainult sisselogimine või ka taotlused, kinnitamine ja tööriistade avaldamine? Mis saab olemasolevatest broneeringutest? |
| Q46 | `admin` õigused | Millised tegevused on administraatorile lubatud ja kas ta võib otsustada omaniku asemel? |
| Q47 | Andmete nähtavus | Kes võib avada broneeringu detailid? PDF lk 4 näitab omaniku kontakte ja lk 8 rentniku kontakte. Millal ning kellele avaldatakse telefon, e-mail ja täpne aadress? |
| Q48 | Külalise juurdepääsu vastuolu | `HomeView` märkmed nõuavad kategoorialt edasi minnes sisselogimist ja PDF avaleht ütleb, et sirvimiseks peab sisse logima; `ToolsView` märkmed lubavad külalist ning avalikku otsingut. Kinnitada ühtne ligipääs kataloogile, detailidele ja saadavusele. |
| Q43 | Kontakt-e-maili muutmine | Registreerimise PDF kirjeldab aadressikonflikti veana `EMAIL_ALREADY_EXISTS`. Lahtine on hilisem muutmine, kinnitamise vajadus ja muutmise API konfliktikäitumine. |
| Q44 | Andmete uuendamine sisselogimisel | Kas nimi ja e-mail kirjutatakse korduval sisselogimisel Google'i andmetega üle? |
| Q49 | Omaniku aadressi muutmine | Kas muutub ka olemasolevate broneeringute üleandmiskoht? Kas varasemad kokkulepped säilitatakse? |
| Q50 | Jagatud location-kirjed | Kui mitu profiili viitab samale aadressile, kas muutmisel uuendatakse ühist kirjet või luuakse eraldi kirje? |
| Q51 | Loendite haldamine ja koordinaadid | Linnad, linnaosad ja kategooriad on SQL algandmetes; nende lugemise API-d on kirjeldatud otsingu märkmetes. Lahtine on hilisem haldamine ning `lat`/`lng` allikas ja vajadus MVP-s; skeem lubab NULL-i. |

### I. Tööriistad, kataloog ja pildid

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q52 | `U` tähendus | Kas omanik määrab staatuse käsitsi või süsteem rentimise ajal? Kuidas suhestub see kuupäevapõhise saadavusega? |
| Q53 | Taotlustega tööriista sulgemine | Mis juhtub `P` ja `C` kirjetega, kui omanik määrab tööriista `U` olekusse? |
| Q54 | Tööriista muutmine | Milliseid välju võib aktiivsete broneeringute ajal muuta ja kas osapooled peaksid nägema varasemaid andmeid? |
| Q55 | Tööriista kustutamine | `booking.tool_id` välisvõtmel puudub kustutamise erireegel (vaikimisi `NO ACTION`), piltidel on `ON DELETE CASCADE`. Kuidas eemaldada ajalooga tööriist: keelata kustutamine, peita või arhiveerida? |
| Q56 | Kasutaja kustutamine | Kas see kuulub MVP-sse ja kuidas käsitletakse seotud tööriistu, profiili ning broneeringuid? |
| Q57 | Piltide piirangud | Millised failivormingud, suurused ja piltide arv on lubatud? Kas vähemalt üks pilt on kohustuslik? |
| Q58 | Põhipildi puudumine ja asendamine | SQL tagab kõige rohkem ühe põhipildi, mitte selle olemasolu. `MyToolsView` ettepanek kasutab puudumisel `null` ja kohatäitjat. Kinnitada see kõigis vaadetes ning määrata põhipildi kustutamise ja vahetamise reegel. |
| Q59 | Kataloogi allesjäänud täpsustused | Kategooria-, linna- ja linnaosafiltrid ning lehekülgjaotus on `ToolsView` märkmetes kirjeldatud. Lahtised on sortimine, tekstiotsingu MVP ulatus, lehe suuruse piirid ja vigaste filtrite käsitlus. `status="A"` on sõnastatud võimaliku vaikeväärtusena: kas `U` on avalikus otsingus lubatud? |

### J. Frontend ja API lepingud

PDF lk 4 näitab algus- ja lõppkuupäeva kalendreid; toimivat kuupäevakomponenti koodis veel ei ole.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q60 | Saadavuse pärimine | Milline on endpoint, hõivatud perioodide vorming, päringuvahemiku suurus ja andmete uuendamine kuu vahetamisel? |
| Q65 | DTO lepingute lõpetamine | Kategooriate, otsingu ja asukoha DTO-d on märkmetes, registreerimise ning booking-create näited PDF-is. `MyToolsView` DTO-d on ettepanekud. Ühtlustada ülejäänud detaili-, muutmise-, otsuse- ja saadavuse lepingud, kohustuslikkus, avalikud kontaktandmed ning pildi MIME-tüüp; domeeni DTO-klasse veel pole. |
| Q67 | Endpointide ja navigeerimise vastuolud | Märkmetes on osa API-sid kirjeldatud, kuid koodis domeeni endpoint'e pole. `POST /auth/google` ei kuulu Vite olemasoleva `/api` proksi alla. PDF lk 4 suunab eduka taotluse järel `/my-bookings`, kinnituse märkmed pakuvad `/booking-confirmation`. Fikseerida autentimise, omaniku otsuse, saadavuse ja isiklike loendite lõplikud rajad. |
| Q68 | API vealepingu katvus | `ApiError` kuju on olemas: String-väljad `message` ja `errorCode`; handler katab kohandatud 403/404 ning esimese väljevea 400 `INCORRECT_INPUT`. Lahtised on autentimise 401, konflikti 409, korduva otsuse ning muude vigade ühtne kuju. Valideerimise handler eeldab vähemalt üht väljeviga: määrata ka objektitaseme valideerimise käsitlus. |
| Q61 | Vormi käitumine | Mis juhtub alguse muutmisel, sobimatu lõpu korral, laadimise ajal ja saadavuse päringu tõrke korral? |
| Q62 | Aegunud saadavusinfo | Mida teeb UI, kui kuupäevad muutuvad valimise ja saatmise vahel hõivatuks: säilitab valiku, tühjendab selle või pakub uut perioodi? |
| Q63 | Isiklike vaadete tegevused | Kolme loendi tähendus on `MyToolsView` märkmetes kasutajaga täpsustatud. Lahtised on pakutud kuupäeva- ja staatusfiltrite kinnitamine, saabunud ootel taotluste asukoht ning omaniku otsuste ja ajaloo kuvamine. Neid ei kata ainult käimasolevate väljarenditud tööriistade loend. |
| Q64 | Tagasipöördumine pärast sisselogimist | Kas kasutaja naaseb pärast sisselogimist algselt avatud vaatesse ja kas kuupäevavalik säilib? |
| Q69 | UI keel ja ligipääsetavus | Maketid ja teated on valdavalt eestikeelsed; toorikus on ka ingliskeelseid silte. Fikseerida kasutajaliidese ja teavituste ühtne keel, kuupäevavorming, mobiilne käitumine ning kalendri juhtimine klaviatuuriga. |

### K. Kasutajateavitused

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q70 | Teavituste kanal ja sisu | `BookingConfirmationView` märkmed lubavad otsusest teatada „sõnumi teel”; PDF näitab sõnumivaateid. Kas teavitus kuvatakse rakenduses, saadetakse e-mailiga või mõlemat? Määrata saajad, saatmise hetked ja kohustuslik sisu koos Q79-ga. |

### L. MVP ulatus ja vastuvõtt

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q73 | Hind ja maksmine | PDF lk 2 näitab „Hind: 5 €/päev”, kuid SQL-is ja kataloogi DTO-s pole hinnavälja. Kas hind on maketi kohatäitja, arveldatakse väljaspool rakendust või peab hinnastamine kuuluma MVP-sse? |
| Q75 | Valmisoleku kriteeriumid | Backendis puuduvad testide lähtefailid ja frontendi package.json-is testikäsk. Määrata vastuvõtustsenaariumid: registreerimine, õigused, perioodid, samaaegsed päringud ning MVP-sse valitud teavituste ja integratsioonide tõrked. |

## Analüüsis lisandunud küsimused

| ID | Prioriteet | Teema | Tõend ja vajalik otsus |
|---|---|---|---|
| Q76 | P0 | Andmebaasi skeemi valik | `1_reset_database.sql` loob `tool_rental`, kuid `2_create.sql` ja `3_import.sql` kasutavad kvalifitseerimata nimesid ega määra `search_path`-i. Rakenduse seadistuses pole skeemi valikut; `backend/CLAUDE.md` räägib `minu_projekt` skeemist. Määrata ühtne, korratav seadistus, et tulemus ei sõltuks ühenduse välisest `search_path`-ist. Tegelikku andmebaasi ei kontrollitud. |
| Q77 | P1 | `updated_at` uuendamine | `profile`, `tool` ja `booking` kasutavad ainult `DEFAULT current_timestamp`; uuendamise triggerit ega rakendusloogikat pole. Määrata, milline kiht muudatuste ajatemplid uuendab. |
| Q78 | P0 | Google-identiteet registreerimisel | PDF lk 3 saadab `googleSub` kliendi JSON-is ja kirjeldab Google tokeni valideerimist. Määrata, kuidas registreerimispäring seotakse serveris kontrollitud Google kasutajaga; kliendi `googleSub`, `userId` või `roleName` ei saa olla serveri autentimistõend. |
| Q79 | P0 | Sõnumid ja `comment` | PDF lk 5–8 näitab kasutajate sõnumeid ja postkasti, kuid skeemis pole sõnumitabelit. PDF lk 4 saadab rentniku teksti väljas `ownerMessage`; lk 8 näitab ka omaniku sõnumit laenajale. Otsustada sõnumite MVP ulatus ja mõlema osapoole tekstide salvestamine, et need üksteist üle ei kirjutaks. |
| Q80 | P0 | Tööriista saadavusperiood | PDF lk 12 „Lisa uus tööriist” sisaldab saadavuse algus- ja lõppkuupäeva; `tool` tabelis on ainult `A/U` staatus. Kas omanik määrab lubatud rendiperioodi või eemaldatakse need väljad MVP maketist? Kuidas seostub see bookingute blokeeritud kuupäevadega? |
| Q81 | P1 | Maketiviite ajakohasus | `MyToolsView` märkmete väide praeguse `Laenukas.pdf` kolmest lehest on aegunud: failis on 14 ning „Minu tööriistad” asub lk 11. Uuendada viidet, et kirjeldus osutaks õigele vaatele. |

## Otsuste fikseerimine ja kontrolli piirid

Küsimuse lahendamisel uuendada seotud projektidokumenti ning eemaldada lahendatud rida sellest nimekirjast. Osalise lahenduse korral jätta alles ainult lahtine osa. Maketi ettepanekut ei käsitleta automaatselt kinnitatud otsuse ega töötava API-na.

Analüüs oli staatiline: loetud on lähtekood, seadistused, SQL, Markdown ja makettide PDF-tekst. Andmebaasi skripte, rakendust, build'i ega teste ei käivitatud; käitusaja toimivust ei väideta. Muudetud on ainult käesolevat faili.
