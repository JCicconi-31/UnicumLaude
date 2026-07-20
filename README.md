# 📌 Unicum Laude

Unicum Laude è un gioco di ruolo (RPG) stile arcade nel quale si impersonifica uno studente di Informatica durante il proprio percorso universitario.
Attraverso lo svolgimento degli esami in stile combattimenti, lo studente se riesce conseguirà la Laurea.

---

## 🚀 Come eseguire il progetto

### Prerequisiti
- Java 25 (LTS)
- Gradle (9.3.0)

### Istruzioni

```bash
git clone https://github.com/JCicconi-31/UnicumLaude.git
cd UnicumLaude
```

### Build del progetto
```bash
./gradlew build
```

### Esecuzione
```bash
./gradlew run
```

---

## 🤖 Uso di strumenti di AI

Per lo sviluppo di Unicum Laude è stato utilizzato Claude (Anthropic) come assistente alla programmazione.
L'intelligenza artificiale è stata impiegata come supporto tecnico per risolvere diverse problemamatiche implementative e per
approfondire concetti progettuali.

L'AI è stata impiegata come **assistente**, non come generatore autonomo del progetto.

In particolare è stata usate per:

- **progettazione assistita**: discussione delle scelte architetturali (applicazione
  principi SOLID, pattern strategy, separazione in package) a partire da un analisi del gioco.
- **generazione e revisione del codice**: stesura di alcune classi e metodi poi letti,
  compresi ed eventualmente modificati.
- **scrittura dei test**: definizione dei casi di test JUnit e dei test double
  (mock/stub ai confini del sistema).
- **debugging e code review**: individuazione di bug e di code smell (es. divisione
  intera nella barra HP, gestione dei thread in JavaFX, duplicazioni di codice), e verifica
  della coerenza con il design.

Ogni suggerimento dell'AI è stato **valutato, compreso e verificato** prima di essere
usato: le decisioni di progetto (scope, scelte tra alternative, cosa includere o
escludere) sono state prese consapevolmente, la correttezza del comportamento è
garantita dalla suite di **97 test** automatici (`./gradlew test`).

Le classi realizzate con assistenza dell'AI riportano una nota di dichiarazione nel
javadoc.
---

📌 Per una descrizione più dettagliata dell’uso dell’AI, utilizzare la **Wiki del repository**.




