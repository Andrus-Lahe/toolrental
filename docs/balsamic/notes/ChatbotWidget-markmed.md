# ChatbotWidget.vue märkmed

Allikas: [Vestlusrobot.rtf](Vestlusrobot.rtf) (pilt vestlusaknast).

Staatus: kinnitatud 2026-10-09 (roboti nimi "Abimees Pets", ujuv nupp paremas alanurgas, kirjaklambrit pole, 401 annab JSON-vastuse AUTHENTICATION_REQUIRED). Vestlusrobot tehakse ümber hüpikaknaks, mis on nähtav kõigil lehtedel; praegune eraldi leht `AiSearchView.vue` (`/ai-search`) asendatakse sellega. Backendis on `POST /api/ai/ask` juba olemas (`ToolAskController`, `ToolAskService`).

## Erinevus tavalisest vaate sildist

- `ChatbotWidget.vue` on **komponent**, mitte eraldi vaade: tal pole oma rada. See kuvatakse kõigi lehtede peal (`App.vue` kaudu), samamoodi nagu [Header-markmed.md](Header-markmed.md).
- Pildil olev vestlusaken on Elisa teenindusroboti „Annika“ oma ja on mõeldud ainult eeskujuks. Laenukas kasutab oma nime „Abimees Pets“ ja oma tervitusteksti.
- Pildil on sisestusvälja kõrval kirjaklambri ikoon (faili lisamine). `POST /api/ai/ask` võtab vastu ainult teksti, seega ikooni ei kuvata.
- Pildil pole näha, kuidas vestlusaken avatakse. Kokkuleppe järgi on selleks paremas alanurgas ujuv nupp.
- Näide kasutab faili `3_import.sql` andmeid: Kristiine linnaosas (Marko Tamme asukoht) on saadaval tööriist `toolId = 1` Akutrell.

## Vaate märkmed

```text
Roll: Customer, Admin (külastajale vestlusakent ei kuvata)
Failinimi: ChatbotWidget.vue
Frontend rada: — (komponent, kuvatakse kõigi lehtede peal paremas alanurgas)

Vaatega seotud lisainfo:
Sisse logitud kasutajale kuvatakse paremas alanurgas vestlusroboti nupp. Selle vajutamisel avaneb vestlusaken, mille päises on roboti nimi "Abimees Pets" ja sulgemisnupp "X". Aken algab tervitussõnumiga "Tere! Mina olen Abimees Pets. Kuidas saan sind täna aidata?"

Kasutaja kirjutab küsimuse välja "Kirjuta siia..." (kuni 500 märki) ja vajutab saatmisnuppu. Tühja välja korral nupp ei tööta. Küsimus kuvatakse vestluses ja saadetakse backendile POST /api/ai/ask päringuga. Ootamise ajal on saatmisnupp keelatud.

Vastuse answer kuvatakse roboti sõnumina. Kui tools list pole tühi, kuvatakse selle all tööriistade nimed (koos kategooria ja linnaosaga). Nimele vajutades suunatakse kasutaja vaatele /tools/{toolId}.

Vestlus hoitakse ainult frontendi mälus. Lehe värskendamisel see kaob. Kui vestlus on pikem kui aken, kuvatakse allanoole nupp, mis kerib viimase sõnumini.
```

## API märkmed — POST /api/ai/ask

```text
API: POST /api/ai/ask

ToolAskRequest.java
Request body:
{
  "question": "Millised tööriistad on saadaval Kristiines?"
}

ToolAskResponse.java
Response (200):
{
  "answer": "Kristiines on praegu saadaval Akutrell, Muruniiduk, Survepesur ja Matkatelk.",
  "tools": [
    {
      "toolId": 1,
      "name": "Akutrell",
      "category": "Ehitustööd",
      "city": "Tallinn",
      "district": "Kristiine"
    },
    ...
  ]
}

API teenuse lisainfo:
Küsimusest tuvastab AI (Gemini) linna, linnaosa ja kategooria ning otsib saadaval (status = 'A') tööriistu. Tagastatakse kuni 20 tööriista. Kui küsimus ei puuduta tööriistu, linna/linnaosa/kategooriat ei leita või tulemusi pole, tagastatakse ikkagi 200: answer sisaldab selgitavat teksti (nt "Saan aidata ainult saadaval tööriistade otsimisega.") ja tools on tühi list. city ja district võivad olla null, kui omanikul pole asukohta.

Veateated:
HTTP: 401
errorCode: AUTHENTICATION_REQUIRED
message: "Vaate avamiseks logi sisse."

HTTP: 400
errorCode: INCORRECT_INPUT
message: "question: ei tohi olla tühi"

HTTP: 400
errorCode: INCORRECT_INPUT
message: "question: ei tohi olla pikem kui 500 märki"

HTTP: 500
errorCode: INTERNAL_SERVER_ERROR
message: "Toiming ebaõnnestus. Palun proovi hiljem uuesti."
```

## Märkused koodi kohta

- `401` tuleb `SecurityConfig` failist (`anyRequest().authenticated()`). `authenticationEntryPoint` kirjutab JSON-vastuse `AUTHENTICATION_REQUIRED` päringutele `GET /api/users/me/tools` ja `POST /api/ai/ask`.
- `400` teated tulevad failis `ToolAskRequest.java` olevatest `@NotBlank` ja `@Size` `message` väärtustest.
- Kõiki neid juhtumeid kontrollib `ToolAskControllerTest`.
- `500` tuleb üldisest `handleException` handlerist. See juhtub siis, kui Gemini kutse ebaõnnestub või tagastab tühja vastuse (`IllegalStateException`).
