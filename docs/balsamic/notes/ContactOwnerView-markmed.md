# ContactOwnerView.vue märkmed

## Vaate märkmed

```text
Roll: Kõik rollid (peab olema sisse logitud — kõik kasutajad autendivad Google kontoga)
Failinimi: ContactOwnerView.vue
Frontend rada: /tools/{toolId}/contact

Vaatega seotud lisainfo:
Vaade avatakse tööriista detailvaatelt ("Võta ühendust omanikuga" nupp), toolId tuleb route'i path variable'ist. Nupule "Saada sõnum" vajutades saadetakse Pealkiri ja Sisu koos toolId'ga backendile POST kutsena. Sõnumit andmebaasi ei salvestata — see saadetakse otse e-kirjana tööriista omaniku (tool.owner_id kaudu leitud app_user, google_sub'iga seotud Google konto) e-posti aadressile.

1. Gmaili compose-link (kõige lihtsam, API-t pole vaja)
https://mail.google.com/mail/?view=cm&fs=1&to=saaja@example.com&su=Teema&body=Kirja%20sisu

Javas tuleb parameetrid URL-kodeerida:

java
String url = "https://mail.google.com/mail/?view=cm&fs=1"
    + "&to=" + URLEncoder.encode(to, StandardCharsets.UTF_8)
    + "&su=" + URLEncoder.encode(subject, StandardCharsets.UTF_8)
    + "&body=" + URLEncoder.encode(body, StandardCharsets.UTF_8);
// redirect / avada uues aknas
Plussid: OAuth scope'e ega Google'i verifitseerimist pole vaja. Kasutaja on juba Google'isse sisse logitud, nii et Gmail avaneb kohe.
Miinused: manuseid lisada ei saa. Sisu peab mahtuma URL-i (paar tuhat tähemärki). Parameetrid cc ja bcc töötavad, aga see URL-formaat pole ametlikult dokumenteeritud API.
Kui kasutajal on mitu Google'i kontot, saab õige konto valida kujuga https://mail.google.com/mail/u/<email>/?view=cm....
```

## API märkmed — POST /api/tools/{toolId}/contact

```text
API: POST /api/tools/{toolId}/contact

ContactOwnerRequestDto.java
Request body:
{
  "subject": "Küsimus rendi kohta",
  "content": "Tere, kas tööriist on saadaval järgmisel nädalal?"
}

Response (200): NONE

API teenuse lisainfo:
Saaja leitakse toolId kaudu (tool.owner_id → app_user.id, google_sub kaudu seotud Google kontoga) ning e-kiri saadetakse omaniku profiili (profile tabeli email väli) aadressile. Saatja on sisse logitud kasutaja (senderId sessioonist), kelle andmeid backend e-kirja koostamisel vajadusel kasutab (nt saatja nimi/e-post pöördumisel).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'toolId' väärtusega: 123"
```
