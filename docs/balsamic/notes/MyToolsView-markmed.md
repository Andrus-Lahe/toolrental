# Minu tööriistad — lehekülg 11 — API ettepanek

Staatus: ettepanek, mitte olemasoleva API kirjeldus. Kolme loendi tähendus on kasutaja täpsustatud; endpoint, DTO-d, frontend rada, kuupäeva- ja staatusfiltrid ning allpool pakutud veakoodid ei ole veel projektis rakendatud ega OpenAPI/Jira kirjeldustega kinnitatud. Allolevad sildid on arutamiseks mõeldud kavandid.

Vaate viide: kasutaja tähistus „pg 11”. Repositooriumi `Laenukas.pdf` sisaldab kolme PDF-lehte; „Minu tööriistad” asub kolmanda lehe koondvaates.

## Kasutaja täpsustatud loendite tähendus

- „Minu renditud tööriistad” (`rentedTools`): minu enda tööriistad, mille olen teistele kasutajatele välja rentinud.
- „Minu broneeringud” (`bookings`): teiste kasutajate tööriistad, mille olen broneerinud.
- „Minu tööriistad rendiks” (`ownedTools`): minu enda sisestatud tööriistad.

## Pakutav lahendus

Vaate avamisel üks `GET /api/users/me/tools` päring. See tagastab profiilikaardi andmed ja kõik kolm loendit. `me` määratakse serveris autentitud kasutaja põhjal; brauser ei saada `userId` parameetrit. Ka administraator näeb siin ainult enda andmeid. Päring ei muuda andmeid.

Vastus: `MyToolsResponseDto.java`. `rentedTools` ja `bookings` sisaldavad `MyToolBookingDto.java` objekte; `ownedTools` sisaldab `MyToolCardDto.java` objekte. Kõik on kavandatavad DTO-d.

### Pakutavad loendite reeglid

- `rentedTools`: broneeringuga seotud `tool.owner_id` vastab kasutajale ja `booking.renter_id` on teine kasutaja; broneeringu staatus on `C` ning `start_date <= täna <= end_date`.
- `bookings`: `booking.renter_id` vastab kasutajale ja seotud `tool.owner_id` on teine kasutaja; staatus on `P` või `C` ning `end_date >= täna`. Siia kuuluvad ka käimasolevad kinnitatud broneeringud. Möödunud ning `R` staatusega taotlused ei kuulu sellesse loendisse.
- `ownedTools`: `tool.owner_id` vastab kasutajale. Tagastatakse nii `A` kui `U` staatusega tööriistad.
- Broneeringuloendites on üks kaart broneeringu kohta. Sama tööriista mitu broneeringut eristab `bookingId`. Oma tööriistade loendis on üks kaart tööriista kohta.
- Välja renditud enda tööriist võib olla korraga nii `rentedTools` kui `ownedTools` loendis: välja rentimine ei muuda omanikku.
- Kuupäevapiirid arvestatakse serveris `Europe/Tallinn` ajavööndis. Staatusetõlgendus on ettepanek: `P` = ootel, `C` = kinnitatud, `R` = tagasi lükatud.
- Broneeringud järjestatakse `start_date`, seejärel `booking.id` järgi kasvavalt. Oma tööriistad järjestatakse `created_at`, seejärel `tool.id` järgi kahanevalt.
- Tühjad loendid tagastatakse kujul `[]`, HTTP 200. Esimeses versioonis tagastatakse kõik sobivad kirjed, lehekülgjaotust ei ole.
- Pildiks valitakse `tool_image.is_main = true` kirje. `imageData` on olemasoleva pildi Base64; peapildi puudumisel `null` ja vaates kasutatakse kohatäitjat. Näidisandmete pildid on SVG-d; päris piltide MIME-tüübi edastamine vajab eraldi kokkulepet.

### Näidise alus

SQL-faili `3_import.sql` kasutaja Liis Kask (`userId = 3`), kuupäev **23.09.2026**. Tema enda tööriistadele ei ole ühtegi käimasolevat kinnitatud broneeringut: Hekikääride taotlus (`bookingId = 3`, rentija `userId = 1`) on staatusega `R`, seega `rentedTools` on tühi. Tema tehtud Redeli broneering (`bookingId = 2`) lõppes 21.09.2026 ega kuulu pakutud kuupäevafiltri järgi `bookings` loendisse. Akutrelli taotlus (`bookingId = 1`) on ootel ja kuulub `bookings` loendisse. Tema enda tööriistad on Tolmuimeja, Hekikäärid ja Projektor. Vastuses on need lisamise aja järgi kahanevalt. Pildistringid on tuletatud sama SQL-faili tegelikest SVG-andmetest.

PDF-i korduvad „Akutrell” kaardid ja kasutaja nimi/e-post on visuaalsed kohatäitjad, mitte API näidisandmed.

## Vaate märkmete kavand

Komponendi nimi ja frontend rada on samuti ettepanekud. Teiste vaadete täpseid radu ei ole olemasolevas router'is kokku lepitud.

```text
Roll: Customer / Admin (sisse logitud kasutaja)
Failinimi: MyToolsView.vue
Frontend rada: /my-tools

Vaatega seotud lisainfo:
Vaate avamisel laaditakse GET /api/users/me/tools abil kasutaja nimi, e-post ning kolm tööriistaloendit; tühja loendi juures kuvatakse „Tööriistu ei ole”.
„Minu renditud tööriistad” näitab minu teistele välja renditud tööriistu; „Minu broneeringud” minu broneeritud teiste kasutajate tööriistu; „Minu tööriistad rendiks” minu enda sisestatud tööriistu.
„Muuda profiili” ja „Lisa uus tööriist” avavad vastava vormivaate; „Vaata detaile” avab broneeringu puhul bookingId ning oma tööriista puhul toolId alusel detailvaate.
Need nupud teevad siin ainult navigeerimise; sihtvaadete API-kutsed kirjeldatakse nende siltidel. Sisse logitud kasutajale kuvatakse päises „Logi välja”.
```

## API märkmete kavand — GET /api/users/me/tools

Allolevad veateated on **uue API käitumise ettepanek**, mitte koodist leitud endpoint'i vead. Enne lõpliku Balsamiq sildina kasutamist tuleb leping kokku leppida.

```text
API: GET /api/users/me/tools

MyToolsResponseDto.java
Response (200):
{
  "userId": 3,
  "firstName": "Liis",
  "lastName": "Kask",
  "email": "liis.kask@example.com",
  "rentedTools": [],
  "bookings": [
    {
      "toolId": 1,
      "toolName": "Akutrell",
      "toolStatus": "A",
      "imageData": "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNDAiIGhlaWdodD0iMjQwIiB2aWV3Qm94PSIwIDAgMjQwIDI0MCI+PHJlY3Qgd2lkdGg9IjI0MCIgaGVpZ2h0PSIyNDAiIHJ4PSIxNiIgZmlsbD0iI2YxZjVmOSIvPjxwYXRoIGZpbGw9IiMyNTYzZWIiIGQ9Ik01MCA2MGgxMDV2NDVINTB6IE04NSAxMDVoMzV2NjBIODV6Ii8+PHBhdGggZmlsbD0iIzMzNDE1NSIgZD0iTTE1NSA3MGgyNXYyNWgtMjV6IE0xODAgNzhoMzB2OWgtMzB6IE03MCAxNTVoNjV2MTVINzB6Ii8+PHRleHQgeD0iMTIwIiB5PSIyMTUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZvbnQtZmFtaWx5PSJzYW5zLXNlcmlmIiBmb250LXNpemU9IjIwIiBmaWxsPSIjMGYxNzJhIj5Ba3V0cmVsbDwvdGV4dD48L3N2Zz4=",
      "bookingId": 1,
      "startDate": "2026-10-02",
      "endDate": "2026-10-04",
      "bookingStatus": "P"
    }
  ],
  "ownedTools": [
    {
      "toolId": 8,
      "toolName": "Projektor",
      "toolStatus": "A",
      "imageData": "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNDAiIGhlaWdodD0iMjQwIiB2aWV3Qm94PSIwIDAgMjQwIDI0MCI+PHJlY3Qgd2lkdGg9IjI0MCIgaGVpZ2h0PSIyNDAiIHJ4PSIxNiIgZmlsbD0iI2YxZjVmOSIvPjx0ZXh0IHg9IjEyMCIgeT0iMTI1IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIyMCIgZmlsbD0iIzBmMTcyYSI+UHJvamVrdG9yPC90ZXh0Pjwvc3ZnPg=="
    },
    {
      "toolId": 4,
      "toolName": "Hekikäärid",
      "toolStatus": "A",
      "imageData": "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNDAiIGhlaWdodD0iMjQwIiB2aWV3Qm94PSIwIDAgMjQwIDI0MCI+PHJlY3Qgd2lkdGg9IjI0MCIgaGVpZ2h0PSIyNDAiIHJ4PSIxNiIgZmlsbD0iI2YxZjVmOSIvPjxnIGZpbGw9Im5vbmUiIHN0cm9rZS13aWR0aD0iMTAiPjxwYXRoIHN0cm9rZT0iIzY0NzQ4YiIgZD0iTTY1IDM1bDEwNSAxMzUgTTE3NSAzNUw3MCAxNzAiLz48cGF0aCBzdHJva2U9IiNlYTU4MGMiIGQ9Ik0xNDUgMTQwbDI1IDMwIE05NSAxNDBsLTI1IDMwIi8+PC9nPjxjaXJjbGUgY3g9IjEyMCIgY3k9IjEwNSIgcj0iOSIgZmlsbD0iIzMzNDE1NSIvPjx0ZXh0IHg9IjEyMCIgeT0iMjE1IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIyMCIgZmlsbD0iIzBmMTcyYSI+SGVraWvDpMOkcmlkPC90ZXh0Pjwvc3ZnPg=="
    },
    {
      "toolId": 3,
      "toolName": "Tolmuimeja",
      "toolStatus": "A",
      "imageData": "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNDAiIGhlaWdodD0iMjQwIiB2aWV3Qm94PSIwIDAgMjQwIDI0MCI+PHJlY3Qgd2lkdGg9IjI0MCIgaGVpZ2h0PSIyNDAiIHJ4PSIxNiIgZmlsbD0iI2YxZjVmOSIvPjxwYXRoIGZpbGw9IiMwZDk0ODgiIGQ9Ik02NSA5MGgxMDB2NjBINjV6Ii8+PHBhdGggZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMGQ5NDg4IiBzdHJva2Utd2lkdGg9IjE1IiBkPSJNODUgOTBWNTVoNTV2MzUiLz48cGF0aCBzdHJva2U9IiMzMzQxNTUiIHN0cm9rZS13aWR0aD0iOCIgZD0iTTU1IDE1NWgxMjUgTTE1MCAxMzB2NDUiLz48dGV4dCB4PSIxMjAiIHk9IjIxNSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9InNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMjAiIGZpbGw9IiMwZjE3MmEiPlRvbG11aW1lamE8L3RleHQ+PC9zdmc+"
    }
  ]
}

API teenuse lisainfo:
Kasutaja tuvastatakse serveris autentimise põhjal; request body ja userId parameeter puuduvad.
rentedTools sisaldab minu teistele välja renditud enda tööriistade käimasolevaid kinnitatud broneeringuid; bookings minu tehtud aegumata P/C staatusega broneeringuid teiste kasutajate tööriistadele; ownedTools minu enda sisestatud tööriistu.
Loendid võivad olla tühjad; imageData sisaldab peapildi Base64-andmeid või on null.

Veateated:
HTTP: 401
errorCode: AUTHENTICATION_REQUIRED
message: "Vaate avamiseks logi sisse."

HTTP: 403
errorCode: USER_BLOCKED
message: "Sinu konto on blokeeritud."

HTTP: 404
errorCode: PROFILE_NOT_FOUND
message: "Kasutaja profiili ei leitud."
```

## Veakäsitluse seos olemasoleva projektiga

- `ApiError.java` olemasolev vastusekuju on `message` ja `errorCode`; `errorCode` on praegu String, mitte Java enum.
- `USER_BLOCKED` on uus pakutav kood. Seda saab rakendada olemasoleva `ForbiddenException` kaudu, mille `RestExceptionHandler` teisendab HTTP 403 vastuseks.
- `PROFILE_NOT_FOUND` on uus pakutav kood. Seda saab rakendada olemasoleva `DataNotFoundException` kaudu, mille handler teisendab HTTP 404 vastuseks. Skeem lubab kasutajat ilma profiilita.
- HTTP 401 ja `AUTHENTICATION_REQUIRED` vajavad autentimiskihis eraldi käsitlemist; loetud `RestExceptionHandler` seda praegu ei rakenda.
- Tühi tööriistade või broneeringute loend ei ole viga. API ei võta vastu kliendi määratud ID-d, mistõttu pole siin pakutud `PRIMARY_KEY_NOT_FOUND` viga.

## Enne lõplike siltide kinnitamist

Loendite tähendus on kasutaja täpsustatud. Kokku leppida tuleb veel pakutud kuupäeva- ja staatusfiltrid, API aadress ja DTO-d, frontend rada, detailvaadete sihtradade ja ID-parameetrite kasutus, autentimisviis, veajuhtumid ning pildi MIME-tüübi leping. Seejärel tuleb sama API leping kanda OpenAPI ja Jira kirjeldustesse. See ettepanek ei lisa backend'i ega muuda SQL-faile.
