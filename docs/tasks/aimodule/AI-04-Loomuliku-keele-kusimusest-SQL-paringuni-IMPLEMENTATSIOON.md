# AI-04 — Loomuliku keele küsimusest SQL-päringuni — implementatsiooni plaan

**Seotud task:** [AI-04-Loomuliku-keele-kusimusest-SQL-paringuni.md](./AI-04-Loomuliku-keele-kusimusest-SQL-paringuni.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — olemasolev endpoint kasutab veel intent/filtri voogu; AI-05 ühendab selle uue validaatoriga.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchIntent.java` — olemasoleva endpointi DTO; eemaldamine kuulub AI-05 käitusvoo muutmise juurde.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/resources/application.properties` — Google GenAI mudeli ning API võtme konfiguratsioon on olemas.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/build.gradle` — Spring AI Google GenAI starter on lisatud.
- AI-02 piiratud vaade ja AI-03 `is_available_now` reegel on eeltingimused; neid taskide faile tuleb enne prompti lõplikku seadmist järgida.

## Puuduv/muudetav

- Eraldiseisvat NL→SQL generaatorit pole.
- Praegune intent-põhine endpoint jääb aktiivseks kuni AI-05 lisab SQL-i kontrolli ja käivitamise.
- SQL teksti genereerimine peab olema eraldatud SQL-i kontrollist ja käivitamisest.

## Sammud

1. **Lisa NL→SQL generaator** — uus fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolSearchSqlGenerator.java`
   - Võta system prompti kontekstiks ainult kinnitatud AI-02 vaate nimi/veerud ja AI-03 saadavuse tingimus.
   - Anna user promptis edasi valideeritud kasutaja küsimus.
   - Nõua ühe SQL `SELECT` päringu puhast teksti, ilma Markdowni või proosata.
   - Mittetoetatava küsimuse korral tagasta marker `CANNOT_ANSWER`; ära anna seda edasi SQL-i käivitajale.
   - Ära lisa prompti toorandmeid, emaili, telefoninumbrit, kasutajate nimesid ega tabeliskeemi, mida otsing ei vaja.

2. **Hoia generaator lahus otsingu käitusvoost** — AI-05 muudab hiljem `ToolAskService`-it ning eemaldab `ToolSearchIntent`-i, kui validaator saab SQL-i enne käivitamist kontrollida.

3. **Hoia mudeli seaded keskkonnakonfiguratsioonis** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/resources/application.properties`
   - Taaskasuta olemasolevat Google GenAI konfiguratsiooni; siinne task ei vaheta mudelit ega lisa uut teenusepakkujat.

## Veakäsitlus

Tühi või mudeliteenuse veavastus ei tohi SQL-i täitmise etappi jõuda. Erindite teisendamine API vastuseks ja mittetoetatava küsimuse UI käitumine kuuluvad AI-08 taski; ära käsitle mudeli suvalist teksti andmebaasi tulemusena.

## Testid

`ToolSearchSqlGeneratorTest` kontrollib lubatud vaate ja veergude prompti, kasutaja küsimuse edastamist, puhta SQL-i väljastamist, `CANNOT_ANSWER` markerit ning tühja mudelivastuse tagasilükkamist. Test käivitati lokaalse ChatClient mock'iga; päris mudelit ega andmebaasi generaatori test ei vaja.

## Teostuse piir

SQL-i genereerimine ei tähenda, et väljund oleks usaldatud või käivitamiseks valmis. AI-05 peab markeri eraldama, ülejäänud SQL-i valideerima ning käivitama ainult lubatud tingimustele vastava päringu.
