# Taotluse saatmise kinnitus — lehekülg 9

Allikas: kasutaja antud `Laenukas(toorik).pdf`, PDF-i lehekülg 9.
Vormistus: `balsamiq-markmete-struktuur.md` ja `skill-uus-balsamic-silt`.

Komponendi nimi `BookingConfirmationView.vue` ja frontend rada `/booking-confirmation` on ettepanekud; neid ei ole olemasolevas Vue router'is veel määratud. Allolev silt kirjeldab kavandatavat käitumist.

## Vaate märkmed

```text
Roll: Customer / Admin (sisse logitud taotluse saatja)
Failinimi: BookingConfirmationView.vue
Frontend rada: /booking-confirmation

Vaatega seotud lisainfo:
Vaatele suunatakse pärast laenutamise taotluse edukat saatmist; ebaõnnestunud saatmisel kinnitusvaadet ei avata.
Kuvatakse teade: „Sinu laenutamise taotlus on edukalt saadetud tööriista omanikule ja ootab omaniku kinnitust. Teavitame sõnumi teel, kui taotlus on kinnitatud või tagasi lükatud.”
Teade kinnitab taotluse saatmist; tööriista omanik ei ole taotlust veel kinnitanud.
Vaate sisu on staatiline: selle avamine ega värskendamine ei saada uut taotlust ega vaja eraldi API-kutset.
```

## API märkmed

Selle vaate staatilise kinnitusteksti jaoks API märkmete kasti ei lisata. Taotluse loomise API-kutse kuulub taotluse saatmise vormi märkmetesse. Omaniku otsuse ja sõnumiga teavitamise API-kutsed kuuluvad vastavate tegevuste märkmetesse; kinnitusvaade neid ei käivita.

Päise ühine navigeerimine ja „Logi välja” tegevus kirjeldatakse ühise päisekomponendi juures. Sõnumiga teavitamine on PDF-is kirjeldatud nõue; see märge ei kinnita selle funktsiooni olemasolu backend'is.
