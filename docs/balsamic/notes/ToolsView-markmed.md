# ToolsView.vue märkmed

## Vaate märkmed

```text
Roll: Kõik rollid (Admin, Customer, Külastaja)
Failinimi: ToolsView.vue
Frontend rada: /tools

Vaatega seotud lisainfo:
Vaate avamisel (sh avalehe kategooriale vajutades) või filtrite (kategooria, linn, linnaosa) rakendamisel tehakse päring GET /api/tools vastavate query parameetritega. Kaardil nupule "Vaata detaile" vajutades suunatakse kasutaja tööriista detailvaatele (/tools/{toolId}). Leheküljenumbritele vajutamisel küsitakse järgmise lehe andmed uue pageNumber väärtusega.
```

## API märkmed — GET /api/tools

```text
API: GET /api/tools

ToolsResponse.java
Response (200):
{
  "pageNumber": 1,
  "pageSize": 6,
  "totalPages": 2,
  "totalElements": 8,
  "tools": [
    {
      "toolId": 1,
      "toolName": "Akutrell",
      "description": "18 V akutrell, kaks akut ja laadija. Sobib puurimiseks ja kruvide keeramiseks.",
      "imageData": "BASE64-image-data",
      "status": "A",
      "cityName": "Tallinn",
      "districtName": "Kristiine"
    },
    ...
  ]
}

API teenuse lisainfo:
Query parameetrid categoryId, cityId, districtId ja status on valikulised (0 tähendab filtri ignoreerimist; vaikimisi saab avalikus otsingus ette anda status="A"). Tulemused tagastatakse lehekülgedena vastavalt pageNumber ja pageSize parameetritele (vaikimisi nt pageNumber=1, pageSize=6). imageData sisaldab tööriista põhipilti (is_main = true).

Veateated: —
```

## API märkmed — GET /api/categories

```text
API: GET /api/categories

CategoryDto.java
Response (200):
[
  {
    "categoryId": 1,
    "categoryName": "Aiatööd"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõigi kategooriate nimekirja filtri rippmenüü täitmiseks, sordituna sequence välja järgi kasvavalt.

Veateated: —
```

## API märkmed — GET /api/cities

```text
API: GET /api/cities

CityDto.java
Response (200):
[
  {
    "cityId": 1,
    "cityName": "Tallinn"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõigi linnade nimekirja linna rippmenüü täitmiseks.

Veateated: —
```

## API märkmed — GET /api/cities/{cityId}/districts

```text
API: GET /api/cities/{cityId}/districts

DistrictDto.java
Response (200):
[
  {
    "districtId": 1,
    "districtName": "Kristiine"
  },
  ...
]

API teenuse lisainfo:
Tagastab ainult valitud linna (cityId) linnaosad linnaosa rippmenüü täitmiseks.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'cityId' väärtusega: 123"
```
