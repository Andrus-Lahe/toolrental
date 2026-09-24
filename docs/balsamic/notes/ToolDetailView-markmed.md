# ToolDetailView.vue märkmed

## Vaate märkmed

```text
Roll: Kõik rollid (Admin, Customer, Külastaja)
Failinimi: ToolDetailView.vue
Frontend rada: /tools/{toolId}

Vaatega seotud lisainfo:
Vaate avamisel tehakse päring GET /api/tools/{toolId}, et laadida tööriista detailne info (nimi, kategooria, kirjeldus, pilt, staatus ja omaniku ID) ning seejärel päring GET /api/users/{userId} omaniku kontaktandmete kuvamiseks. Kui kasutaja vajutab nupule "Laenuta", suunatakse ta broneerimise vaatele (/tools/{toolId}/booking). Kui kasutaja pole sisse logitud, suunatakse ta esmalt sisse logima.
```

## API märkmed — GET /api/tools/{toolId}

```text
API: GET /api/tools/{toolId}

ToolDetailResponse.java
Response (200):
{
  "toolId": 1,
  "ownerId": 1,
  "toolName": "Akutrell",
  "categoryName": "Ehitustööd",
  "description": "18 V akutrell, kaks akut ja laadija. Sobib puurimiseks ja kruvide keeramiseks.",
  "imageData": "BASE64-image-data",
  "status": "A"
}

API teenuse lisainfo:
Tagastab konkreetse tööriista detailsed andmed koos põhipildiga (is_main = true). ownerId tagastatakse omaniku kontaktandmete eraldi pärimiseks.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'toolId' väärtusega: 123"
```

## API märkmed — GET /api/users/{userId}

```text
API: GET /api/users/{userId}

UserDetailResponse.java
Response (200):
{
  "userId": 1,
  "firstName": "Marko",
  "lastName": "Tamm",
  "email": "email@Gmail.com",
  "phone": "56565656"
}

API teenuse lisainfo:
Tagastab kasutaja avalikud kontaktandmed (tööriista omaniku info kuvamiseks).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'userId' väärtusega: 123"
```
