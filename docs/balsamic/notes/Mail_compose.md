# Gmaili kirjutamise akna avamine URL-iga

Gmaili saab avada nii, et kirjutamise aken on kohe lahti ja väljad on juba täidetud. Sellise lingi saad panna oma web appis nupu või lingi taha.

## Põhivorm

```
https://mail.google.com/mail/?view=cm&fs=1&to=...&su=...&body=...
```

| Parameeter | Tähendus |
|---|---|
| `view=cm` | avab kirja koostamise akna (*compose mail*) |
| `fs=1` | avab akna täisekraanil (valikuline) |
| `to` | saaja, mitu aadressi eralda komaga |
| `cc` | koopia |
| `bcc` | pimekoopia |
| `su` | teema (*subject*) |
| `body` | kirja sisu |
| `authuser` | kindel Gmaili konto, nt `authuser=sinu@gmail.com` (valikuline) |

Kui brauseris on sisse logitud mitu Google'i kontot, saab konto valida ka aadressis: `/mail/u/0/` on esimene konto ja `/mail/u/1/` teine.

### Näide

```
https://mail.google.com/mail/?view=cm&fs=1&to=tugi@firma.ee&cc=juht@firma.ee&su=Turvaintsident&body=Tampa%20objektil%20oli%20turvaintsident.
```

## Erimärgid tuleb kodeerida (URL encoding)

| Märk | Kood |
|---|---|
| tühik | `%20` |
| reavahetus | `%0A` |
| `&` | `%26` |
| `?` | `%3F` |
| `=` | `%3D` |
| `#` | `%23` |
| `/` | `%2F` |
| `:` | `%3A` |
| `ä õ ö ü` | `%C3%A4 %C3%B5 %C3%B6 %C3%BC` |

Ilma kodeerimiseta katkeb tekst esimese `&` või `#` märgi juures, sest brauser loeb selle järgmise parameetri alguseks.

## Link web appile kirja sisus

`body` on tavatekst. HTML-i (`<a href>`) sinna panna ei saa, aga Gmail muudab `https://...` aadressi ise klikitavaks lingiks.

Kirja sisu, mida soovid saada:

```
Tere!

Vaata juhtumit siit:
https://minuapp.ee/juhtum?id=42&vaade=detail

Parimate soovidega
```

Sama sisu kodeerituna `body` väärtuseks:

```
Tere!%0A%0AVaata%20juhtumit%20siit%3A%0Ahttps%3A%2F%2Fminuapp.ee%2Fjuhtum%3Fid%3D42%26vaade%3Ddetail%0A%0AParimate%20soovidega
```

Kogu link:

```
https://mail.google.com/mail/?view=cm&fs=1&to=tugi@firma.ee&su=Juhtum%2042&body=Tere!%0A%0AVaata%20juhtumit%20siit%3A%0Ahttps%3A%2F%2Fminuapp.ee%2Fjuhtum%3Fid%3D42%26vaade%3Ddetail%0A%0AParimate%20soovidega
```

**Kõige olulisem on kodeerida web appi lingi enda `?` ja `&` märgid** (`%3F`, `%26`). Muidu arvab Gmail, et `vaade=detail` on tema enda parameeter, ja link kirjas jääb poolikuks.

## Web appi lehel

### HTML

```html
<a href="https://mail.google.com/mail/?view=cm&fs=1&to=tugi@firma.ee&su=Juhtum%2042&body=..."
   target="_blank" rel="noopener">
  Saada Gmailiga
</a>
```

### JavaScript

JavaScriptis tee kodeerimine `encodeURIComponent()` abil, siis ei pea seda käsitsi tegema:

```javascript
const appLink = "https://minuapp.ee/juhtum?id=42&vaade=detail";
const body = `Tere!\n\nVaata juhtumit siit:\n${appLink}\n\nParimate soovidega`;

const url = "https://mail.google.com/mail/?view=cm&fs=1"
  + "&to=" + encodeURIComponent("tugi@firma.ee")
  + "&su=" + encodeURIComponent("Juhtum 42")
  + "&body=" + encodeURIComponent(body);

window.open(url, "_blank");
```

## Piirangud

- Kasutaja peab olema Gmaili sisse logitud, muidu suunatakse ta esmalt sisselogimislehele.
- Kiri ei lähe ise välja. Kasutaja näeb eeltäidetud akent ja vajutab ise „Saada“.
- Kirja sisu ei saa vormindada (paks kiri, nupud, lingitekst).
- Hoia kogu URL alla umbes 2000 märgi, pikem võib brauseris katkeda.
- Manuseid URL-iga lisada ei saa.

## Alternatiiv: `mailto:`

Kui kasutaja ei pruugi Gmaili kasutada, sobib paremini `mailto:` link. See avab tema vaikimisi postiprogrammi ja kodeerimise reeglid on samad:

```
mailto:tugi@firma.ee?subject=...&body=...
```
