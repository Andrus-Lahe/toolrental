# AI-lahenduse ülevaade

Toolrentali ainus AI-funktsioon on vestlusrobot „Abimees Pets“: backendis `POST /api/ai/ask` ja frontendis `ChatbotWidget.vue`. Selles dokumendis on loetletud kasutusel olevad AI-võtted ning põhjendused, miks LLM-kohtunikku, `ProblemDetail`'it, chat memory't ja `SimpleLoggerAdvisor`'it ei kasutatud.

Seotud dokumendid:

- [Backend task](../tasks/backend/ChatbotWidget/Tooriistade-otsing-vestlusrobotiga.md) ja [implementatsiooni plaan](../tasks/backend/ChatbotWidget/Tooriistade-otsing-vestlusrobotiga-IMPLEMENTATSIOON.md)
- [Frontend task](../tasks/frontend/ChatbotWidget/Tooriistade-otsing-vestlusrobotiga.md)
- [ChatbotWidget märkmed](../balsamic/notes/ChatbotWidget-markmed.md)

## Kasutusel olevad AI-võtted

| Teema | Kuidas tehtud | Kus |
|---|---|---|
| Mudel ja raamistik | Spring AI 2.0.1 ja Google Gemini (`gemini-3.5-flash-lite`), `ChatClient` fluent API | `build.gradle`, `application.properties` |
| Kahe-sammuline kutse | 1) intent: küsimusest tuvastatakse linn, linnaosa, kategooria ja tööriista nimi; 2) vastus sõnastatakse leitud tööriistade põhjal | `ToolAskService` |
| Grounding | Linnade, linnaosade ja kategooriate nimed tulevad andmebaasist promptisse ning mudeli vastus kontrollitakse nende vastu (`canonicalName()`). Tööriista nimi (`toolName`) on vaba tekst: seda kasutatakse ainult siis, kui selle 5 esimest tähte on küsimuses ja see sisaldub mõne andmebaasis oleva tööriista nimes. Nii ei riku mudeli välja mõeldud nimi (nt „Koristusvahendid“ küsimusele „Soovin Kristiines koristada“) otsingut (`groundedToolName()`). Tööriistad otsib SQL, mitte AI. `tools` list kliendile tuleb andmebaasist. | `ToolAskService`, `AvailableToolRepository` |
| System + user rollid | Juhised on `.system()` sõnumis, andmed `.user()` sõnumis. Kasutaja tekst on märgendis `<question>`, tööriistad märgendis `<tools>`, ja prompt ütleb, et nende sisu on andmed, mitte juhised. | `ToolAskService`, `prompts/*.md` |
| Vastuse toon | Pets vastab viisakalt, soojalt ja mänguliselt: kõige rohkem üks mänguline lause (tööriista väike iseloom või rõõmus soov), mitte sunnitud nali. See ei puuduta kasutajat ega lisa fakte. Kaks erinevat näidet näitavad tooni. Esimene katse „üks nali igas vastuses“ andis halbu nalju ja asendati. Fikseeritud tekstid (erijuhud, veateated, tervitus) jäävad tõsiseks. | `prompts/tool-search-answer.md` |
| Struktureeritud väljund | `.entity(ToolSearchIntent.class, spec -> spec.useProviderStructuredOutput())`, mis kasutab Gemini enda JSON-skeemi. Skeemis on väljad tüübiga `string`, seega tagastab Gemini puuduva väärtuse kord `""`, kord tekstina `"null"`. `normalizeIntentValue()` muudab mõlemad päris `null`'iks. | `ToolSearchIntent`, `ToolAskService` |
| Chain of thought | `ToolSearchIntent` esimene väli on `reasoning` (`@JsonPropertyOrder`). Mudel kirjutab põhjenduse enne otsust. Kliendile seda ei saadeta, see logitakse `debug` tasemel koos tuvastatud `city`, `district`, `category` ja `toolName` väärtustega. | `ToolSearchIntent` |
| Few-shot / one-shot | Intent-promptis on 6 näidet (Kristiine, Tartu + Aiatööd, Tallinn → `city`, Kristiine + akutrell → `toolName`, Kristiine + koristada → `category`, kontakti küsimine → `false`), vastuse promptis 2 näidet | `prompts/tool-search-intent.md`, `prompts/tool-search-answer.md` |
| Promptid failides | Laetakse käivitumisel kujul `classpath:prompts/*.md`, puuduv fail annab vea kohe. `.param()` ei kasutata, seega few-shot JSON-i `{}` ei rikuta. | `ToolAskService` konstruktor |
| Parameetrid | `thinkingLevel=MINIMAL` ja `maxOutputTokens` (300 intent-kutsel, 400 vastuse kutsel) seatakse igale kutsele. Väärtused tulevad `toolrental.ai.*` seadetest. Temperatuur on teadlikult vaikeväärtusel. | `application.properties` |
| Sisendi turvalisus | `@NotBlank` ja `@Size(max = 500)`. `RegexJailbreakAdvisor`: NFKC-normaliseerimine, nähtamatute märkide eemaldamine, eesti- ja ingliskeelsed jailbreak-mustrid. Blokeeritud küsimus annab 400 `QUESTION_REJECTED` ilma põhjust avaldamata ja Gemini kutset ei tehta. | `ToolAskRequest`, `RegexJailbreakAdvisor`, `GuardrailViolationException`, `RestExceptionHandler` |
| Väljundi turvalisus | `maxOutputTokens`. `getSafeAnswer()` asendab üle 1000 märgi pikkuse vastuse või vastuse, milles on e-post, telefon või URL, tööriistade nimekirjaga. Frontend kuvab vastuse tekstina (`{{ }}`), mitte `v-html`'iga. | `ToolAskService`, `ChatbotWidget.vue` |
| Andmete minimeerimine | AI ega klient ei näe e-posti ega telefoni. `AvailableTool` sisaldab ainult viit avalikku välja. SQL kasutab parameetreid ja loenditest valitud nimesid. | `AvailableTool`, `AvailableToolRepository` |
| Ligipääs | Ainult sisse logitud kasutaja. Muidu tuleb 401 `AUTHENTICATION_REQUIRED` ja vestlusakent ei kuvata. | `SecurityConfig`, `ChatbotWidget.vue` |
| Mõõdikud | `AiMetricsAdvisor` logib iga kutse tokenid, kestuse, mudeli ja `finishReason`'i. Küsimust ega vastust ei logita. | `AiMetricsAdvisor` |
| Testid | `RegexJailbreakAdvisorTest` (15), `AiMetricsAdvisorTest` (2), `ToolAskServiceTest` (19, mockitud `ChatModel` ja päris `ChatClient`), `ToolAskControllerTest` | `backend/src/test` |

## Miks neid nelja ei kasutatud

### 1. LLM-kohtunik (LLM-as-a-judge)

**Mis see on:** eraldi LLM-kutse, mis hindab enne ja pärast põhikutset, kas sisend on rünnak ja kas väljund on turvaline.

**Miks mitte praegu:**

- **Kulu ja kiirus:** iga küsimus teeb juba 2 Gemini kutset. Kohtunik lisaks mõlemale sisendi- ja väljundikontrolli, kokku kuni 4 lisakutset. Vastus oleks märgatavalt aeglasem ja mitu korda kallim.
- **Praegune kaitse katab peamise riski:** regex püüab teadaolevad mustrid ja intent-kutse ise tagastab tööriistaväliste küsimuste korral `toolSearch=false`. Väljundi kontroll ja `tools` list andmebaasist piiravad kahju ka siis, kui midagi läbi pääseb.
- **Mittedeterministlik:** kohtunik on samuti LLM, keda saab teoreetiliselt manipuleerida, ja seda on raskem testida kui regexit.

**Jääb avatuks:** kaudne prompt injection. Tööriista nime kirjutab kasutaja ja see läheb teise kutse sisse märgendis `<tools>`. Regex kontrollib ainult küsimust. Praegu kaitsevad märgendid, prompti juhis ja `getSafeAnswer()`, kuid tähendust mõistev kontroll puudub. Seetõttu on kohtunik backend taskis kirjas järgmise taskina.

### 2. `ProblemDetail`

**Mis see on:** Spring'i standardne RFC 9457 veavastus (`type`, `title`, `status`, `detail`).

**Miks mitte:** projektil on juba ühtne veavastus `ApiError` (`message`, `errorCode`), mida tagastab üks `RestExceptionHandler`. Frontend loeb kõikjal `error.response.data.message`. Teine veaformaat ainult AI jaoks tähendaks, et frontend peab vigu kahel eri viisil lugema. Seetõttu on `GuardrailViolationException` tehtud nagu projekti teised erindid ja seda käsitleb sama handler.

### 3. Chat memory (`MessageChatMemoryAdvisor`)

**Mis see on:** server hoiab vestluse ajalugu ja saadab varasemad sõnumid igale uuele kutsele kaasa.

**Miks mitte:**

- **Toote otsus:** vestlus on ainult frontendi mälus ja kaob lehe värskendamisel. Iga küsimus on iseseisev tööriistaotsing.
- **Lisatöö ja riskid:** serveri mälu vajaks vestluse ID-d, selle sidumist sisse logitud kasutajaga (et keegi ei saaks teise inimese vestlust lugeda), mälu puhastamist ja piiranguid.
- **Turvalisus ja kulu:** ajalugu paneks iga kutse prompti rohkem kasutaja kirjutatud teksti. See suurendab injection'i pinda ja tokenite arvu.

**Hind:** järelküsimus nagu „aga Tartus?“ ei tea, mida enne küsiti. Kui seda on vaja, on see eraldi task.

### 4. `SimpleLoggerAdvisor`

**Mis see on:** Spring AI advisor, mis logib iga kutse päringu ja vastuse täies mahus.

**Miks mitte:**

- **Isikuandmed:** logisse jõuaksid kasutajate küsimused ja AI vastused täistekstina, sealhulgas see, mida kasutaja ise kirjutas.
- **Logi maht:** iga kutse logiks ka kogu system-prompti koos few-shot näidetega.

**Asendus:** `AiMetricsAdvisor` logib ainult mõõdikud (tokenid, kestus, mudel, `finishReason`). `reasoning` logitakse `debug` tasemel. Arenduse ajal võib `SimpleLoggerAdvisor`'i ajutiselt lisada, aga koodi seda ei jäetud.
