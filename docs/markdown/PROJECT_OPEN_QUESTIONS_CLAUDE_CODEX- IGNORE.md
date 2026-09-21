# Community Tool Rental — lahtised küsimused ja vastuolud

Koostatud: 18.09.2026

Alus: [CODEX_PROJECT_CONTEXT.md](../../../../Desktop/Tool_Rental/CODEX_PROJECT_CONTEXT.md) ja senised kasutajaga sõlmitud kokkulepped.

Dokument koondab 75 varem tuvastatud küsimust kategooriate ja prioriteedi järgi. Küsimuste ID-d vastavad varasema loendi numbritele; need on jälgitavuse jaoks säilitatud, kuigi järjekord on muutunud.

Kirjelduse puudumine ei tähenda, et lahendus puudub koodist. Selle analüüsi käigus ei kontrollitud rakenduse koodi ega kehtivat SQL-faili. Küsimused ei ole kinnitatud muudatusettepanekud ega luba skeemi või äriloogikat muuta.

## Prioriteedid

| Prioriteet | Tähendus |
|---|---|
| P0 — esmalt lahendada | Mõjutab arhitektuuri, andmete korrektsust või põhiprotsessi läbimist. Lahendada enne vastava osa teostamist. |
| P1 — täpsustada MVP ülesannetes | Vajalik üheselt mõistetava käitumise ja kasutajaliidese jaoks. Lahendada vastava ülesande alguses. |

P1 ei tähenda, et küsimuse võib ignoreerida. Kõigi punktide lahendamine ei nõua uue funktsiooni loomist: sobiv otsus võib olla ka „MVP-s ei toetata”.

## Kõige olulisemad otsused

1. Viia Entity/DTO kirjeldus kooskõlla kasutaja mapperi piiri reegliga.
2. Kontrollida lõpliku SQL-i teadaolevaid vastuolusid ja kooskõlastada vajalikud parandused.
3. Fikseerida kuupäevade piirid, ühepäevase rendi tähendus ja blokeerivad staatused.
4. Määrata `PENDING` aegumine ning tagastuse ja lõpetamise staatused.
5. Tagada, et samaaegsed päringud ei loo kattuvaid aktiivseid broneeringuid.
6. Määrata kinnitamise käitumine Calendar API, e-maili või andmebaasi osalise tõrke korral.
7. Kirjeldada esmane registreerimine, kohustusliku profiili täitmine ja rakenduse sessioon.
8. Valida Google Calendari sihtkalender ja juurdepääsu korraldus.

## P0 — esmalt lahendatavad küsimused

### A. Arhitektuur ja dokumentide vastuolud

Seotud kontekstifaili peatükid: 3, 5, 21, 33, 38 ja 39.

| ID | Teema | Vastuolu või vajalik täpsustus |
|---|---|---|
| Q01 | Entity/DTO piir | Peatükk 3 lubab Entity kasutamist Service-kihis, kuid kasutaja otsene reegel nõuab mapperi piirist väljaspool DTO-sid. Dokument tuleb selle reegliga kooskõlla viia. |
| Q04 | Lõpliku SQL-i toimivus | Skeem on kuulutatud lõplikuks, kuid märgitud on võimalikud vead: staatuste vaikeväärtuste tähesuurus, `char(1)` pikkade staatuste jaoks ning CHECK-viited puuduvatele väljadele. Kontrollimata on nende esinemine kehtivas failis ja kooskõlastatud parandus. |
| Q02 | `PENDING` blokeerib perioodi | Kontekstifailis blokeerib `PENDING` kuupäevad. Varasemas kuupäevavaliku dokumentatsioonis pakuti vastupidist. Praeguse konteksti järgi kehtib blokeerimine; varasem dokumentatsioon tuleb sellega kooskõlla viia. |
| Q03 | Ühepäevane rent | Kontekstifail nõuab `start_date < end_date`. Varasem, kinnitamata ettepanek lubas võrdseid kuupäevi. Dokumentide vastuolu tuleb lahendada koos perioodi piiride tähendusega. |

### B. Rendiperioodi ja saadavuse põhireeglid

Seotud peatükid: 21 ja 33.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q05 | Lõppkuupäev | Kas see kuulub rendiperioodi sisse? Kas samal päeval võib alata järgmine rent? |
| Q06 | Kestuse arvutamine | Kas 21.–23. september tähendab kahte või kolme päeva? Reegel peab vastama perioodi piiridele. |
| Q10 | Blokeerivate staatuste täpne loend | „Vähemalt `PENDING`, `CONFIRMED`, `RETURN_PENDING`” ei ole täielik reegel. Määrata kõigi kuue staatuse mõju saadavusele. |
| Q07 | Tänane päev ja minevik | Kas rent võib alata täna? Kas minevikus algav rent on selgesõnaliselt keelatud? |
| Q08 | Ajavöönd | Millise ajavööndi järgi määratakse tänane päev ja tähtaja möödumine? |
| Q11 | Korduskontroll kinnitamisel | Mida kontrollida uuesti, kui tööriista saadavus või kasutaja staatus muutus või rendi alguskuupäev saabus? |
| Q12 | Hilinenud tagastus | Kas blokeeritakse ainult algne periood või ka järgnevad päevad? Mis saab järgmisest kinnitatud rendist? |

### C. Broneeringu elutsükkel

Seotud peatükid: 20–25.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q13 | `PENDING` kehtivusaeg | Kui kaua võib omanik vastamata jätta? Praegune kirjeldus võimaldab kuupäevi määramata ajaks kinni hoida. |
| Q14 | `EXPIRED` | Millised taotlused aeguvad, millal ja milline mehhanism staatust muudab? |
| Q15 | Vastuseta taotlus rendi alguses | Kas seda võib veel kinnitada või suletakse see automaatselt? |
| Q16 | `RETURN_PENDING` | Mida staatus tähendab ja milline tegevus viib broneeringu sellesse olekusse? |
| Q17 | `COMPLETED` | Kes kinnitab lõpetamise: omanik, rentnik või süsteem? Kas lõppkuupäeva saabumisest piisab? |
| Q24 | Täielik üleminekute skeem | Kirjeldatud on ainult `PENDING → CONFIRMED` ja `PENDING → CANCELLED`. Määrata ülejäänud lubatud üleminekud ning nende käivitajad. |
| Q74 | MVP piir staatuste osas | Kas tagastus ja aegumine tuleb praegu teostada või säilitatakse staatuseväärtused tuleviku jaoks? Otsus peab sobima `PENDING` kuupäevade blokeerimise reegliga. |

### D. Samaaegsed tegevused ja osalised tõrked

Seotud peatükid: 21, 24–26 ja 31.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q25 | Kaks samaaegset taotlust | Milline mehhanism takistab kahel päringul korraga vaba perioodi kontrollida ja kattuvaid `PENDING` kirjeid luua? |
| Q26 | Samaaegne kinnitamine ja tagasilükkamine | Kuidas töödeldakse otsuseid kahest vahekaardist või korduvat otsusepäringut? |
| Q27 | Kordussaatmine pärast vastuse kadumist | Kui taotlus loodi, kuid vastus ei jõudnud brauserisse, kuidas eristada korduspäringut uuest taotlusest? |
| Q28 | Tehingu piirid | Millised muudatused salvestatakse koos ja millal loetakse toiming lõpetatuks? |
| Q29 | `CONFIRMED`, kuid sündmus puudub | Mis juhtub, kui staatus salvestati, kuid Calendar sündmust ei loodud? |
| Q30 | Sündmus loodud, ID salvestamata | Kuidas taastada seos ja vältida korduskatsel teise sündmuse loomist? |
| Q31 | E-mail jäi saatmata | Kas broneering säilib, kes kordab saatmist ja mida näeb kasutaja? |
| Q32 | Backendi taaskäivitumine | Kuidas avastatakse sammude vahel pooleli jäänud toimingud ja jätkatakse neid? |

### E. Registreerimise ja autentimise põhiprotsess

Seotud peatükid: 8–10 ja 13.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q40 | Esmane registreerimine | Millal sisestab kasutaja profiili jaoks vajaliku telefoni ja kohustusliku aadressi? Google Sign-In flow jätab selle sammu vahele. |
| Q41 | Lõpetamata profiil | Kas `app_user` võib eksisteerida ilma `profile` kirjeta ja mida kasutaja sel ajal teha tohib? `profile.user_id` unikaalsus ei taga profiili olemasolu igal kasutajal. |
| Q42 | Rakenduse sessioon | Kuidas säilitatakse pärast Google tokeni kontrolli sisselogitud olek, kui kaua see kehtib ja kuidas toimub väljalogimine? |

### F. Google Calendari ühendamise eeldused

Seotud peatükid: 24 ja 27.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q33 | Sihtkalender | Kas sündmused luuakse rakenduse ühises kalendris, omaniku kalendris või rentniku kalendris? |
| Q34 | Calendar API juurdepääs | Kuidas saab backend vajalikud õigused ja autentimisandmed? Kuidas on see seotud Google Sign-In protsessiga? |
| Q35 | Juurdepääsu puudumine | Kas rentimine toimib ühendatud kalendrita? Mis juhtub juurdepääsust keeldumise või selle tühistamise korral? |
| Q37 | Perioodi esitamine sündmuses | Kas sündmus on kogupäevane või sisaldab üleandmise ja tagastamise kellaaega? Kuidas teisendatakse rendiperiood sündmuse alguseks ja lõpuks? |

## P1 — täpsustused vastavate MVP ülesannete jaoks

### G. Renditaotluse lisareeglid

Seotud peatükid: 20–25, 32 ja 33.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q09 | Perioodi piirangud | Kas kehtib minimaalne või maksimaalne kestus ja kui kaugele ette võib broneerida? Ka piirangute puudumine tuleb fikseerida. |
| Q18 | Rentniku tühistamine | Kas võib tagasi võtta `PENDING` taotluse või tühistada `CONFIRMED` broneeringu? Milliste piirangutega? |
| Q19 | Omaniku tühistamine | Kas pärast kinnitamist võib broneeringu tühistada ja kuidas see erineb taotluse tagasilükkamisest? |
| Q20 | Kuupäevade muutmine | Kas olemasolevat taotlust võib muuta või tuleb see tühistada ja uus luua? |
| Q21 | Enda tööriista rentimine | Oma taotluse kinnitamise keeld on kirjas, kuid oma tööriistale taotluse loomise keeld pole selgelt määratud. |
| Q22 | Korduv taotlus ja piirangud | Kas pärast tagasilükkamist võib kohe samale perioodile uuesti taotleda? Kas aktiivsete taotluste arv on piiratud? |
| Q23 | Tegelik üleandmine | Kas tööriista üleandmine tuleb registreerida või piisab kinnitamisest ja hilisemast tagastusest? |

### H. Õigused, profiil ja asukoht

Seotud peatükid: 7–14, 29 ja 32.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q45 | `BLOCKED` mõju | Kas keelatud on ainult sisselogimine või ka taotlused, kinnitamine ja tööriistade avaldamine? Mis saab olemasolevatest broneeringutest? |
| Q46 | `ADMIN` õigused | Millised tegevused on administraatorile lubatud ja kas ta võib otsustada omaniku asemel? |
| Q47 | Andmete nähtavus | Kes võib avada `/bookings/{id}`? Millal näeb rentnik omaniku telefoni, e-maili ja täpset aadressi? |
| Q48 | Külalise juurdepääs | Kas ilma sisselogimiseta võib vaadata kataloogi, detailvaadet ja vabu kuupäevi? |
| Q43 | Kontakt-e-mail | Kas seda võib Google'ist sõltumatult muuta, kas see vajab kinnitamist ja mida teha aadressikonflikti korral? |
| Q44 | Andmete uuendamine sisselogimisel | Kas nimi ja e-mail kirjutatakse korduval sisselogimisel Google'i andmetega üle? |
| Q49 | Omaniku aadressi muutmine | Kas muutub ka olemasolevate broneeringute üleandmiskoht? Kas varasemad kokkulepped säilitatakse? |
| Q50 | Jagatud location-kirjed | Kui mitu profiili viitab samale aadressile, kas muutmisel uuendatakse ühist kirjet või luuakse eraldi kirje? |
| Q51 | Loendid ja koordinaadid | Kes haldab linnu, linnaosi ja kategooriaid? Kust tulevad `lat`/`lng` ja kas neid on MVP-s vaja? |

### I. Tööriistad, kataloog ja pildid

Seotud peatükid: 16–17 ja 28.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q52 | `UNAVAILABLE` tähendus | Kas omanik määrab staatuse käsitsi või süsteem rentimise ajal? Kuidas suhestub see kuupäevapõhise saadavusega? |
| Q53 | Taotlustega tööriista sulgemine | Mis juhtub `PENDING` ja `CONFIRMED` kirjetega, kui omanik määrab tööriista `UNAVAILABLE` olekusse? |
| Q54 | Tööriista muutmine | Milliseid välju võib aktiivsete broneeringute ajal muuta ja kas osapooled peaksid nägema varasemaid andmeid? |
| Q55 | Tööriista kustutamine | Kuidas eemaldada tööriist, mille füüsilise kustutamise ajaloolised booking-kirjed `RESTRICT` kaudu keelavad? Arhiveerimine või peitmine pole määratud. |
| Q56 | Kasutaja kustutamine | Kas see kuulub MVP-sse ja kuidas käsitletakse seotud tööriistu, profiili ning broneeringuid? |
| Q57 | Piltide piirangud | Millised failivormingud, suurused ja piltide arv on lubatud? Kas vähemalt üks pilt on kohustuslik? |
| Q58 | Põhipildi valik | Kas tööriist võib olla põhipildita? Milline pilt saab põhipildiks pärast senise kustutamist? |
| Q59 | Kataloogi käitumine | Millised filtrid, otsing, sortimine ja lehekülgedeks jaotamine on kohustuslikud? Kas kättesaamatud tööriistad on nähtavad? |

### J. Frontend ja API lepingud

Seotud peatükid: 22 ja 29–34.

Juba kokku lepitud: tööriista lehel on kaks `readonly` kuupäevavälja koos kalendrinuppudega. Hõivatud kuupäevad on hallid ja keelatud, vabad kuupäevad värvilised. Need ei ole lahtised küsimused, kuid kontekstifaili tuleb kokkulepe lisada.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q60 | Saadavuse pärimine | Milline on endpoint, hõivatud perioodide vorming, päringuvahemiku suurus ja andmete uuendamine kuu vahetamisel? |
| Q65 | DTO lepingud | Millised on väljad, kohustuslikkus, kuupäevavorming ning avalike ja isiklike vastuste erinevused? |
| Q66 | Rentniku identiteet | Fikseerida identiteedi saamine autentimise kontekstist ning kliendile lubatud sisendväljad. |
| Q67 | Lõplikud endpoint'id | Omaniku otsuse jaoks on mitu varianti; puuduvad taotluste loendite ja saadavuse lepingud. Kontrollida enne olemasolevaid API-sid. |
| Q68 | API vead | Milline on ühtne JSON, masinloetavad koodid, väljade vead ja vastus juba töödeldud taotluse korduvale otsusele? |
| Q61 | Vormi käitumine | Mis juhtub alguse muutmisel, sobimatu lõpu korral, laadimise ajal ja saadavuse päringu tõrke korral? |
| Q62 | Aegunud saadavusinfo | Mida teeb UI, kui kuupäevad muutuvad valimise ja saatmise vahel hõivatuks: säilitab valiku, tühjendab selle või pakub uut perioodi? |
| Q63 | Kasutaja vaated | Kus asuvad „Minu renditaotlused” ja „Minu tööriistadele saabunud taotlused” ning milliseid staatuseid ja tegevusi need näitavad? |
| Q64 | Tagasipöördumine pärast sisselogimist | Kas kuupäevavalik säilib? Kas omanik naaseb e-mailist avatud taotluse juurde? |
| Q69 | Keel ja ligipääsetavus | Fikseerida UI ja teadete keel, kuupäevavorming, mobiilne käitumine ning kalendri juhtimine klaviatuuriga. |

### K. Calendari sündmused ja e-mailid

Seotud peatükid: 23–27.

Juba määratud: Google Calendarit kasutatakse pärast kinnitamist sündmuse loomiseks, `sendUpdates = none`. Kõik booking'uga seotud kasutajateavitused saadab backend.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q36 | Sündmuse sisu | Millised on pealkiri, kirjeldus, aadress, booking'u link ja osalejad? |
| Q38 | Muudatused pärast kinnitamist | Kui tühistamine või kuupäevade muutmine lubatakse, mida tehakse olemasoleva sündmusega? |
| Q39 | Sündmuse käsitsi muutmine | Kuidas käsitleda sündmuse kustutamist või muutmist Google Calendaris? Tagasisuunalist sünkroonimist pole kirjeldatud ja seda ei tohi automaatselt eeldada. |
| Q70 | E-mailide sisu | Millised andmed on kohustuslikud: tööriist, periood, osapooled, kontaktid ja tagasilükkamise põhjus? |
| Q71 | Saaja aadressi muutumine | Kas kiri saadetakse profiili praegusele aadressile või taotluse loomisel kehtinud aadressile? |
| Q72 | Täiendavad teavitused | Kas aegumise, tagastuse ja tühistamise korral saadetakse e-mail, kui need protsessid kuuluvad MVP-sse? |

### L. MVP ulatus ja vastuvõtt

Seotud peatükid: 1, 36 ja 40.

| ID | Teema | Vajalik otsus |
|---|---|---|
| Q73 | Rendi hind ja maksmine | Kas kasutamine on tasuta, arveldamine toimub väljaspool rakendust või jääb hinnastamine MVP-st välja? Hind ja maksmine pole kirjeldatud. |
| Q75 | Valmisoleku kriteeriumid | Millised vastuvõtustsenaariumid on kohustuslikud, sh samaaegsed päringud ja integratsioonide tõrked? Kuidas kontrollitakse e-maili ning Calendarit testkeskkonnas? |

## Otsuste fikseerimine

Iga küsimuse lahendamisel märkida selle ID juurde otsus, otsuse kuupäev ja vajaduse korral seotud ülesanne. Seejärel uuendada vastavat konteksti- või funktsioonidokumenti, et vastuolulised kirjeldused ei jääks paralleelselt kehtima.

Koodi, SQL-i ja olemasolevat äriloogikat selle nimekirja koostamisel ei muudetud. Rakenduse teste ei käivitatud.
