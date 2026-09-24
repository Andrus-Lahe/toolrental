# AddToolView.vue märkmed

## Vaate märkmed

```text
Roll: Customer (sisse logitud kasutaja, kes lisab oma tööriista rendile)
Failinimi: AddToolView.vue
Frontend rada: /tools/new

Vaatega seotud lisainfo:
Nupule "Lisa pilt" vajutades avatakse failivalik ning valitud pilt kuvatakse eelvaate ruudus. Väli "Pildi nimi" näitab valitud faili nime kasutajaliideses — see on ainult kuvamise info ja seda backendile ei saadeta (tool_image tabelis nimeveergu pole).
Väljad "Saadavus: Alates / Kuni" vastavad booking tabeli start_date ja end_date veergudele (tööriista saadavuse periood), üldine saadavuse olek tuleneb booking tabeli status veerust.
Avatud küsimus: booking.renter_id on NOT NULL, aga uue tööriista lisamisel rentnikku veel pole — jääb backend taski koostamisel täpsustada, kuidas selline "saadavus"-kirje booking tabelisse luuakse (nt kas renter_id jäetakse esialgu tühjaks, kasutatakse owner_id väärtust, või luuakse selleks hoopis eraldi tabel).
Nupule "Salvesta" vajutades kogutakse lehelt kokku vajalikud andmed ning saadetakse backendile POST /api/tools sõnumiga.
```

## API märkmed — POST /api/tools

```text
API: POST /api/tools

ToolCreateRequestDto.java
Request body:
{
  "ownerId": 3,
  "categoryId": 2,
  "name": "Akutrell",
  "description": "18 V akutrell, kaks akut ja laadija. Sobib puurimiseks ja kruvide keeramiseks.",
  "imageData": "BASE64-image-data"
}

Response (200): NONE

API teenuse lisainfo:
ownerId tuleb sisse logitud kasutaja sessioonist. status väärtustatakse loomisel vaikimisi 'A'-ga. Kui imageData pole tühi string (""), luuakse selle põhjal tool_image kirje is_main=true väärtusega — vorm toetab hetkel ainult ühe (pea)pildi lisamist tööriista loomisel.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'categoryId' väärtusega: 123"
```
