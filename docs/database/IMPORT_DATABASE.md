# Täidetud andmebaasi lisamine IntelliJ-s

Eesmärk: ühendus **`jdbc:postgresql://localhost:5432/database`**.

Imporditav fail: [database-with-data.sql](database-with-data.sql). See sisaldab nii tabelite struktuuri kui ka andmeid. Algset `create_toolrental_database.sql.sql` faili ei ole muudetud.

Vaja on töötavat **PostgreSQL 17 või uuemat** ning IntelliJ **Database** tööriistaakent. Kasuta oma arvuti PostgreSQL-i kasutajanime ja parooli.

## 1. Ühenda IntelliJ PostgreSQL-iga

1. Ava **View → Tool Windows → Database**.
2. Vajuta **+ → Data Source → PostgreSQL**.
3. Sisesta:

   | Väli | Väärtus |
   |---|---|
   | Host | `localhost` |
   | Port | `5432` |
   | Database | `postgres` |
   | User | `postgres` või sinu kasutaja |
   | Password | sinu PostgreSQL-i parool |

4. Kui kuvatakse **Download missing driver files**, laadi draiver alla.
5. Vajuta **Test Connection**, seejärel **OK**.

## 2. Loo andmebaas `database`

Ühenduse loomine ei loo uut andmebaasi. Ühenduse nimi IntelliJ paneelis ei muuda andmebaasi nime.

1. Tee ühendusel paremklõps → **New → Query Console**.
2. Käivita eraldi järgmine käsk: aseta kursor käsule ja vajuta **Ctrl+Enter**.

```sql
CREATE DATABASE database;
```

Kui teade ütleb, et `database` on juba olemas, ära loo seda uuesti. Impordiks peab see olema tühi. Olemasolevate tabelite või andmete korral ära jätka importi ega kustuta neid selle juhendi järgi.

## 3. Ühenda just andmebaasiga `database`

1. Vali ühendus Database paneelis ja vajuta **Shift+Enter**.
2. Muuda välja **Database** väärtuseks `database`.
3. Kontrolli, et URL on **`jdbc:postgresql://localhost:5432/database`**.
4. Vajuta **Test Connection → Apply → OK**.
5. Ava sellele ühendusele uus **Query Console** ja käivita:

```sql
SELECT current_database();
```

Tulemus peab olema **`database`**, mitte `postgres` ega `toolrental`.

Kontrolli, et skeemas pole tabeleid:

```sql
SELECT tablename
FROM pg_tables
WHERE schemaname = 'public';
```

Enne esimest importi peab tulemus olema tühi.

## 4. Impordi SQL-fail

1. Leia projekti fail **`docs/database/database-with-data.sql`**.
2. Tee Database paneelis ühendusel `database` paremklõps → **SQL Scripts → Run SQL Script…**.
3. Vali see SQL-fail. Kontrolli, et sihtühendus kasutab andmebaasi **`database`**. Käivita kogu fail.

Kui seda menüüvalikut ei ole, kopeeri faili kogu sisu `database` ühenduse uude Query Console’i. Vali **Ctrl+A → Ctrl+Enter** ning vajadusel kõigi valitud lausete käivitamine.

Fail sisaldab `BEGIN` ja `COMMIT` käske: käivita fail tervikuna. Tabelite loomine ei näita päringutulemuste tabelit; täitmise teadetes ei tohi olla vigu. Vea korral käivita samas konsoolis `ROLLBACK;` ja vaata esimest veateadet.

Ära käivita enne importi `setup-database.ps1`, `create_toolrental_database.sql.sql` ega `seed-marko.sql`: eksport sisaldab juba kõike vajalikku. Import on mõeldud ühekordseks käivitamiseks tühjas baasis.

## 5. Kuva tabelid ja kontrolli andmeid

Kui ühenduse kõrval on **No schemas selected**, vajuta sellele, märgi **public** ja kinnita valik. Vajuta **Refresh** ning ava **public → Tables**.

Seal peab olema **10 tabelit**. Kontrolli Query Console’is:

```sql
SELECT u.first_name, u.last_name, u.role_id, r.role_name
FROM public.app_user u
JOIN public.role r ON r.id = u.role_id
ORDER BY u.first_name;
```

Oodatud rollid:

| Kasutaja | role_id | role_name |
|---|---|---|
| Marko Tamm | 1 | admin |
| Liis Kask | 2 | customer |

Andmebaasis on 2 kasutajat, 4 tööriista, 4 SVG-pilti ja 1 kinnitatud broneering: Liis rendib Marko redelit **18.–21.09.2026**. Marko aadress on praeguste andmete järgi **Teddre 28**.

## 6. Backendi ühendus

Failis `backend/src/main/resources/application.properties` kasuta:

```properties
spring.datasource.url=jdbc:p6spy:postgresql://localhost:5432/database
spring.datasource.username=postgres
spring.datasource.password=SINU_POSTGRESQL_PAROOL
```

Asenda kasutajanimi ja parool enda omadega. IntelliJ ühenduse URL-is ei kasutata `p6spy` osa; backendis on see olemas projekti SQL-logimise tõttu.

Testkasutajate `google_sub` väärtused on väljamõeldud. Need ei võimalda Google’iga sisse logida ning broneeringuga pole seotud päris Google Calendar sündmust.

IntelliJ menüüde juhised: [PostgreSQL ühendus](https://www.jetbrains.com/help/idea/postgresql.html), [SQL-faili import](https://www.jetbrains.com/help/idea/import-data.html), [ühenduse seadistamine](https://www.jetbrains.com/help/idea/managing-data-sources.html).
