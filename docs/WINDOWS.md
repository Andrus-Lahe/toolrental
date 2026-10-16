# Projekti käivitamine Windowsis

Aktiivne projekt: `C:\Users\jaros\IdeaProjects\toolrental`.
WSL-i koopia `/home/student/projects/toolrental` on eraldi; muudatused ei sünkroonitu automaatselt.

## IntelliJ ja Java

Ava aktiivne projekt. Projekti SDK ja Gradle JVM on `temurin-21`
(`C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot`).
Laadi Gradle projekt uuesti, kui IDE kasutab endiselt eelmisi seadeid.

## Andmebaas

Windowsi PostgreSQL 17 teenus `postgresql-x64-17` töötab pordil **5433**.
Ühendus: `localhost:5433`, andmebaas `vali_it`, skeem `tool_rental`, kasutaja `postgres`.
Parool on backendi failis `src/main/resources/application.properties`.
Porti 5432 kasutab teine server, PostgreSQL 18; selle parool on erinev.

Andmed on imporditud failidest `1_reset_database.sql`, `2_create.sql` ja `3_import_ultimate.sql`.
Tavalisel käivitamisel ei ole vaja skripte uuesti käivitada: reset-fail kustutab skeemi andmed.
IntelliJ Database aknas on ühendus `vali_it (PostgreSQL 17)`; vajadusel sisesta sinna parool.

### Kui tabelid Database aknast kaovad

Tühi Database puu ei tähenda, et andmed on kustutatud. Ava ühenduse
`vali_it (PostgreSQL 17)` menüü **Tools → Manage Shown Schemas…** ja vali
andmebaasi `vali_it` skeem `tool_rental`. Seejärel värskenda ühendust;
vajadusel kasuta **Diagnostics → Force Refresh**. Kontrolli ühenduse seadetes,
et port oleks **5433**, andmebaas `vali_it` ja kasutaja `postgres`.

Windowsi teenus `postgresql-x64-17` peab töötama. Selle andmekataloog on
`C:\Program Files\PostgreSQL\17\data`. Tabelite kuvamise parandamiseks ära käivita
`reset database` konfiguratsiooni: see kustutab olemasoleva skeemi koos andmetega.

## Backend

IntelliJ käivituskonfiguratsioon: `Backend (Gradle)` või olemasolev `backend`.
Lisa valitud konfiguratsiooni keskkonnamuutujad:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GEMINI_API_KEY`
- `MAIL_USERNAME` ja `MAIL_PASSWORD`, kui soovid saata e-kirju.

Google OAuthi ja Gemini võtmed pole praegu seadistatud. Backendi täielik käivitamine vajab neid.
Ära salvesta võtmeid jagatud `.run` failidesse ega Giti. IntelliJ lokaalne `backend`
konfiguratsioon asub ignoreeritud `.idea/workspace.xml` failis.

PowerShellis, kui vajalikud keskkonnamuutujad on juba määratud:

```powershell
cd C:\Users\jaros\IdeaProjects\toolrental\backend
.\gradlew.bat bootRun
```

Backend kasutab porti 8080. OAuthi tagasisuunamise aadress on
`http://localhost:8081/login/oauth2/code/google`.

Kui pärast kontrolleri ümbernimetamist või haru vahetamist tekib `Ambiguous mapping`
ja veas nimetatud vana klassi lähtekoodi enam pole, võib `backend/build` kaustas
olla aegunud `.class` fail. Peata backend, käivita `backend` kaustas
`.\gradlew.bat clean classes` ja seejärel käivita backend uuesti. See puhastab
koostamise väljundi ega muuda andmebaasi.

## Frontend

IntelliJ käivituskonfiguratsioon: `Frontend` (npm, Windowsi Node.js).
Või PowerShellis:

```powershell
cd C:\Users\jaros\IdeaProjects\toolrental\frontend
npm.cmd run dev -- --strictPort
```

Ava `http://localhost:8081`. Käivita sellel pordil korraga ainult üks frontend.

## Koostamine

```powershell
cd C:\Users\jaros\IdeaProjects\toolrental\frontend
npm.cmd ci
npm.cmd run build
cd ..\backend
.\gradlew.bat bootJar -x test
```
