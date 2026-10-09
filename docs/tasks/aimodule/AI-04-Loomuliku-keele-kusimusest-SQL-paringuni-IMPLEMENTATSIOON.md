# AI-04 — Loomuliku keele küsimusest SQL-päringuni — implementatsiooni plaan

**Seotud task:** [AI-04-Loomuliku-keele-kusimusest-SQL-paringuni.md](./AI-04-Loomuliku-keele-kusimusest-SQL-paringuni.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — loob `ChatClient` abil praegu `ToolSearchIntent` JSON vastuse linnade, linnaosade ja kategooriate nimede põhjal.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchIntent.java` — praegune LLM-i väljund on boolean ja kolm valikulist filtrit.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/resources/application.properties` — Google GenAI mudeli ning API võtme konfiguratsioon on olemas.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` — Spring AI Google GenAI starter on lisatud.
- AI-02 piiratud vaade ja AI-03 `is_available_now` reegel on eeltingimused; neid taskide faile tuleb enne prompti lõplikku seadmist järgida.

## Puuduv/muudetav

- Praegune prompt klassifitseerib küsimust ja eraldab filtrid; see ei koosta SQL-i.
- Promptile pole praegu piiratud vaate skeemi antud.
- SQL teksti kasutus peab olema eraldatud SQL-i kontrollist ja käivitamisest, mis kuuluvad AI-05 taski.

## Sammud

1. **Asenda intent prompt NL→SQL promptiga** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java`
   - Võta system prompti kontekstiks ainult kinnitatud AI-02 vaate nimi/veerud ja AI-03 saadavuse tingimus.
   - Anna user promptis edasi valideeritud kasutaja küsimus.
   - Nõua ühe SQL `SELECT` päringu puhast teksti, ilma Markdowni või proosata.
   - Ära lisa prompti toorandmeid, emaili, telefoninumbrit, kasutajate nimesid ega tabeliskeemi, mida otsing ei vaja.

2. **Asenda intent-väljundi DTO või tekstiväljund** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchIntent.java`
   - `ToolSearchIntent` ei vasta enam NL→SQL väljundile. Eemaldamine või asendamine teha kooskõlastatult AI-05 teenuse API-ga.
   - Ära muuda DTO-d enne, kui `ToolAskService` ja SQL validator on sama lepinguga joondatud.

3. **Hoia mudeli seaded keskkonnakonfiguratsioonis** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/resources/application.properties`
   - Taaskasuta olemasolevat Google GenAI konfiguratsiooni; siinne task ei vaheta mudelit ega lisa uut teenusepakkujat.

## Veakäsitlus

Tühi või mudeliteenuse veavastus ei tohi SQL-i täitmise etappi jõuda. Erindite teisendamine API vastuseks ja mittetoetatava küsimuse UI käitumine kuuluvad AI-08 taski; ära käsitle mudeli suvalist teksti andmebaasi tulemusena.

## Testid

Hilisem kontroll peaks näitama, et prompt kasutab lubatud vaate skeemi, sisaldab kasutaja küsimust, nõuab SQL-väljundit ega sisalda PII-d. Testkaustu ei loetud ega teste käivitatud.

## Avatud küsimused

- Mittetoetatava küsimuse marker tuleb AI-08 UI käitumisega kokku leppida.
- Vaate täpne nimi ja veerud sõltuvad AI-02 taski lõplikust lahendusest.
