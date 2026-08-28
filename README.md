# Airport Traffic Simulator

Desktop aplikacija za simulaciju avionskog saobraćaja, razvijena u programskom jeziku Java korišćenjem biblioteka AWT i Swing.

Aplikacija omogućava dodavanje aerodroma i letova, učitavanje i čuvanje podataka, pokretanje i pauziranje simulacije, praćenje kretanja aviona na mapi i interakciju sa prikazanim objektima.

## Funkcionalnosti

- dodavanje i prikaz aerodroma na mapi
- dodavanje letova između izabranih aerodroma
- prikaz aviona i njihovog kretanja u realnom vremenu
- pokretanje, pauziranje i nastavljanje simulacije
- prikaz trenutnog dana i vremena simulacije
- izbor aerodroma klikom i njegovo treperenje na mapi
- prikaz podataka o aerodromu prelaskom miša preko njega
- izbor aviona i zadavanje putanje pomoću waypoint tačaka
- detekcija sudara aviona
- uklanjanje aviona sa mape nakon sudara
- učitavanje i čuvanje podataka u CSV i JSON formatu
- tabelarni prikaz aerodroma i letova
- praćenje aktivnosti korisnika i reagovanje na neaktivnost

## Korišćene tehnologije

- Java
- Java AWT
- Java Swing
- višedretveno programiranje (`Thread`, `Runnable`)
- obrada događaja miša i tastature
- CSV i JSON formati podataka
- Git i GitHub

## Organizacija projekta

Glavni paketi projekta su:

```text
src/
├── gui/
│   ├── Window.java
│   ├── AirportFlightService.java
│   ├── PanelFactory.java
│   └── InactivityMonitor.java
├── model/
│   ├── Airport.java
│   ├── Flight.java
│   ├── Map.java
│   └── Simulation.java
└── Exceptions/
    └── SimulationException.java
```

### `gui`

Sadrži glavni prozor aplikacije, forme, tabele, dugmad i obradu korisničkih akcija.

### `model`

Sadrži klase koje predstavljaju aerodrome, letove, mapu i stanje simulacije.

### `Exceptions`

Sadrži prilagođene izuzetke koji se koriste za prijavljivanje grešaka tokom simulacije.

## Pokretanje projekta u Eclipse-u

1. Klonirati repozitorijum:

```bash
git clone <URL_REPOZITORIJUMA>
```

2. Otvoriti Eclipse.
3. Izabrati **File → Import → Existing Projects into Workspace**.
4. Izabrati direktorijum kloniranog projekta.
5. Pokrenuti klasu koja sadrži metodu `main` preko opcije **Run As → Java Application**.

Ako projekat sadrži samo `src` direktorijum:

1. Napraviti novi Java projekat u Eclipse-u.
2. Kopirati postojeće pakete u njegov `src` direktorijum.
3. Proveriti da li nazivi paketa odgovaraju direktorijumima.
4. Pokrenuti glavnu klasu aplikacije.

## Formati podataka

### Aerodromi – CSV

```csv
code,name,x,y,z
BEG,Nikola Tesla,20.3091,44.8184,102
JFK,John F. Kennedy,-73.7781,40.6413,4
```

### Letovi – CSV

```csv
from,to,departure,duration
BEG,JFK,08:30,540
```

### Letovi – JSON

```json
[
  {
    "from": "BEG",
    "to": "JFK",
    "departure": "08:30",
    "duration": 540
  }
]
```

Koordinate aerodroma predstavljaju geografsku dužinu i širinu, koje se prilikom iscrtavanja pretvaraju u koordinate piksela na mapi.

## Rad simulacije

Za svaki aktivni let čuva se trenutno stanje aviona i njegov napredak između početne i odredišne tačke. Pozicija aviona se periodično preračunava na osnovu vremena simulacije, a mapa se ponovo iscrtava kako bi se prikazalo kretanje.

Simulacija koristi posebne niti za poslove koji se izvršavaju u pozadini, kao što su:

- ažuriranje vremena simulacije
- treperenje izabranog aerodroma
- praćenje neaktivnosti korisnika
- odloženo uklanjanje aviona nakon sudara

Promene korisničkog interfejsa izvršavaju se preko AWT Event Dispatch Thread-a.

## Autor

Projekat je izrađen kao deo predmeta Objektno orijentisano programiranje 2.
