# HomeView.vue märkmed

## Vaate märkmed

```text
Roll: Kõik rollid (Admin, Customer, Külastaja)
Failinimi: HomeView.vue
Frontend rada: /

Vaatega seotud lisainfo:
Nupp "Logi sisse / Registreeru" on nähtav ainult sisse logimata külastajale ning sellele vajutamisel avaneb modaal Google kontoga sisselogimiseks (POST /auth/google). Kategooriale klikkimisel kontrollitakse sessiooni — kui kasutaja pole sisse logitud, kuvatakse Google modaal, ning sisselogituna suunatakse kasutaja otsinguvaatele valitud filtri query parameetriga (nt /tools?categoryId=1).
```

## API märkmed — GET /api/categories/detailed-info

```text
API: GET /api/categories/detailed-info

CategoryDetailedInfoDto.java
Response (200):
[
  {
    "categoryId": 1,
    "categoryName": "Aiatööd",
    "description": "Muruniidukid, labidad, rehad",
    "imageData": "BASE64-image-data"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõigi kategooriate nimekirja koos kirjelduse ja pildiga avalehel kategooriakaartide kuvamiseks, sordituna sequence välja järgi kasvavalt.

Veateated: —
```
