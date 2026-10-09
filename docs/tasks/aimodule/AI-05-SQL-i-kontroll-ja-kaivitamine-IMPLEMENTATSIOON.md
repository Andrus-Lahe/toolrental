# AI-05 — SQL-i kontroll ja käivitamine — implementatsiooni plaan

**Seotud task:** [AI-05-SQL-i-kontroll-ja-kaivitamine.md](./AI-05-SQL-i-kontroll-ja-kaivitamine.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — kutsub mudelit ja kasutab pärast intenti `AvailableToolRepository`-t; praegu mudeli koostatud SQL-i ei käivita.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableToolRepository.java` — kasutab `NamedParameterJdbcTemplate`-i, aga koostab ise staatilise päringu; LLM-i SQL-i valideerimise kihti pole.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskController.java` — endpoint delegeerib töö service kihile.
- AI-02 vaade ja AI-04 SQL string on eeltingimused.

Test-kaustu ei uuritud kasutaja juhise järgi.

## Puuduv/muudetav

- SQL-i valider puudub.
- Andmebaasi päring peab piirduma AI-02 vaatega; praegune repository loeb baastabeleid.
- SQL täitmise tulemus tuleb siduda hilisema AI-06 JSON DTO-ga.

## Sammud

1. **Eralda SQL-i kontroll komponendiks** — uus fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlValidator.java`
   - Kontrolli AI-05 lubatud piiri: ainult `SELECT`, ainult AI-02 piiratud vaade, vajalikud väljundveerud.
   - Tagasta kontrolli ebaõnnestumisel kontrollitud rakenduse erind; ära käivita osaliselt sobivat päringut.

2. **Lisa piiratud päringu käivitamine** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/AvailableToolRepository.java` või uus `ToolSearchQueryRepository.java`.
   - Kasuta Spring JDBC olemasolevat konfiguratsiooni.
   - Käivita SQL ainult pärast validaatori edukat tulemust.
   - Projektsiooni tulemus peab saama täita AI-01 lepingus määratud tööriistaväljad.

3. **Ühenda pipeline teenuses** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java`.
   - Järjekord: AI-04 genereerib → validator kontrollib → repository käivitab → service annab struktureeritud tulemuse edasi.
   - Ära lisa siin teist LLM-i kutset vastuse proosaks vormistamiseks.

4. **Ühenda API veakäsitlusega** — failid: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` ja vajadusel `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/exception/`.
   - Kasuta olemasolevat `ApiError` lepingut; SQL-i, DB sõnumit ega stack trace'i ei tagastata.
   - Konkreetne staatus/sõnum kooskõlasta AI-08 taskiga.

## Veakäsitlus

Kontrolli tagasilükkamine ei tohi langeda tühja otsingutulemusena. DB vea puhul tagasta AI-08 järgi üldine serverivastus. Kasutajale ei tagastata mudeli SQL-i ega andmebaasi tehnilist sõnumit.

## Testid

Hilisem teostus peab kontrollima lubatud päringu teostamist ning mitte-SELECT, keelatud vaate/tabeli ja puuduva vajaliku projektsiooni tagasilükkamist enne JDBC käivitust. Testkaustu ei loetud ega teste käivitatud.

## Avatud küsimused

- Kas validator kasutab SQL parserit või muud kontrollimehhanismi, otsustada teostuse ajal AI-05 fikseeritud andmepiiri rikkumata.
- Täpne runtime erindiklass ja HTTP staatus tuleb kokku leppida AI-08-ga.
