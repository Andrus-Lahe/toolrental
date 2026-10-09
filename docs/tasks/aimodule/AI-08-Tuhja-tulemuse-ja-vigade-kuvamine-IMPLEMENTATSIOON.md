# AI-08 — Tühja tulemuse ja vigade kuvamine — implementatsiooni plaan

**Seotud task:** [AI-08-Tuhja-tulemuse-ja-vigade-kuvamine.md](./AI-08-Tuhja-tulemuse-ja-vigade-kuvamine.md)

## Hetkeseis (mis on juba olemas)

- `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/AiSearchView.vue` — kuvab praegu 401 puhul sisselogimise teadet ning ülejäänud vead üldise veateatena; vastuse `answer`-i puudumine tähendab, et tööriistaloendit ei kuvata.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/RestExceptionHandler.java` — valideerimisvead saavad `400 INCORRECT_INPUT`; ootamatu viga saab `500 INTERNAL_SERVER_ERROR` olemasoleva `ApiError` kujul.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/infrastructure/security/SecurityConfig.java` — autentimata päring saab `401`; AI POST puhul body't ei kirjutata.
- `/mnt/c/Users/opilane/IdeaProjects/toolrental/backend/src/main/java/ee/toolrental/aimodule/ToolAskService.java` — praegune tühitulemus on 200 vabatekstilise `answer`-iga ja tühja `tools` massiiviga; uus AI-06 vastus jätab `answer` välja.

Test-kaustu ei uuritud kasutaja juhise järgi.

## Puuduv/muudetav

- Frontend ei erista veel selgesõnaliselt tühja `tools` massiivi edukast tulemusest, kus on kirjeid.
- AI-04/AI-05 uute LLM/SQL veatüüpide API staatused pole määratud.
- Tühja loendi ja tehnilise vea UI seisund peab töötama ilma `answer` väljata.

## Sammud

1. **Fikseeri backend veastaatuste leping** — taskifailid: `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-04-Loomuliku-keele-kusimusest-SQL-paringuni.md` ja `/mnt/c/Users/opilane/IdeaProjects/toolrental/docs/tasks/aimodule/AI-05-SQL-i-kontroll-ja-kaivitamine.md`.
   - Määra mittetoetatava küsimuse ja vigase LLM SQL vastuse `status`/`errorCode` AI-08 UI vajadusega kooskõlas.
   - Ära avalda SQL-i ega algset DB/stack trace detaili.

2. **Käsitle edukat tühitulemust** — fail: `/mnt/c/Users/opilane/IdeaProjects/toolrental/frontend/src/views/AiSearchView.vue`.
   - Kui `response.data.tools` on tühi massiiv, kuva tühitulemuse teade.
   - Ära kuva samal ajal vanast otsingust jäänud tulemusi.

3. **Käsitle API vigu** — sama fail.
   - Säilita `401` sisselogimisteade/link.
   - `400` puhul näita ainult ohutut, kasutajale mõeldud `ApiError.message` väärtust, kui backend leping seda lubab.
   - Võrgu- ja serverivea puhul kuva üldine uuesti proovimise juhis.
   - Ära käsitle error response'i `tools: []` eduka vastusena.

4. **Taasta päringu olek kõigil harudel** — sama fail.
   - Hoia `busy` vabastamine `finally` harus.
   - Uue päringu alguses puhasta vana tulemus ja veateade.

## Veakäsitlus

Backend kasutab olemasolevat `ApiError` kuju ning ei saada kliendile SQL-i või andmebaasi tehnilisi detaile. Frontend eristab tühja HTTP 200 tulemust staatuskoodiga veast. 401 auth-entrypoint võib jääda tühja body'ga; UI määrab sisselogimise oleku statuskoodi järgi.

## Testid

Hilisem UI/API kontroll peab katma tühja 200 vastuse, tulemusi sisaldava 200 vastuse, 401, 400, 500, võrgovea ning `busy` taastumise. Testkaustu ei loetud ega teste käivitatud.

## Avatud küsimused

- AI/SQL tõrgete konkreetne status ja `errorCode` tuleb fikseerida AI-04/AI-05 teostuse eel.
- AI-01 asukohata tulemuse otsus mõjutab tühitulemuse asemel nähtavat kirjet.
