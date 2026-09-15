# Weather Widget — Widget Android nativo

Widget per la home screen di Android che mostra il meteo reale (dati **Open-Meteo**,
gratuiti, senza API key) basato sulla posizione GPS del telefono.

## 🔵 Opzione A — Compilare dal telefono con GitHub Actions (nessun PC richiesto)

1. Crea un account gratuito su **github.com** (dal browser del telefono)
2. Crea una nuova repository (pubblica o privata, indifferente)
3. Carica **tutto il contenuto di questa cartella** nella repository:
   - dal sito GitHub: "Add file" → "Upload files" → trascina tutti i file/cartelle
   - oppure con l'app **Termux** + git, se preferisci la riga di comando
4. Vai nella tab **"Actions"** della repository: la build parte da sola
   (il file `.github/workflows/build.yml` è già incluso e configurato)
5. Aspetta 3-5 minuti che la build finisca (icona verde ✅)
6. Apri la build completata → in fondo alla pagina trovi **"Artifacts"**
   → scarica `weather-widget-apk` (è uno zip contenente l'APK)
7. Estrai lo zip sul telefono, apri il file `.apk` per installarlo
   (Android potrebbe chiederti di abilitare "Installa da fonti sconosciute" — è normale,
   succede per qualunque APK non scaricato dal Play Store)

Puoi far ripartire la build in ogni momento da "Actions" → "Build APK" → "Run workflow".

## 🟢 Opzione B — Compilare da PC con Android Studio

1. **Installa Android Studio** (gratis): https://developer.android.com/studio
2. Apri Android Studio → **Open** → seleziona questa cartella `WeatherWidget`
3. Aspetta che Gradle scarichi le dipendenze (prima volta ci vuole qualche minuto)
4. Collega il telefono via USB con il **debug USB attivo**
   (Impostazioni → Info telefono → tocca 7 volte "Numero build" → Opzioni sviluppatore → Debug USB)
5. Premi il tasto ▶️ **Run** in Android Studio: l'app si installa sul telefono

## Come aggiungere il widget alla home

1. Apri l'app "Weather Widget" una volta (serve solo per concedere il permesso di posizione)
2. Concedi il permesso quando richiesto
3. Torna alla schermata Home del telefono
4. Tieni premuto su uno spazio vuoto → **Widget**
5. Cerca **"Weather Widget"** nella lista → trascinalo sulla home

Il widget si aggiorna automaticamente ogni **30 minuti** (è il minimo che Android
permette per gli aggiornamenti automatici dei widget). Se vuoi un aggiornamento
più frequente, bisogna usare `WorkManager`, dimmi pure se vuoi che te lo aggiunga.

## Struttura del progetto

- `WeatherWidgetProvider.kt` — il widget vero e proprio: prende la posizione GPS,
  chiama Open-Meteo, aggiorna la UI
- `WeatherApi.kt` — chiamata di rete a Open-Meteo e parsing della risposta
- `MainActivity.kt` — schermata che si apre toccando l'icona dell'app,
  serve solo a richiedere il permesso di posizione (il widget da solo non può farlo)
- `res/layout/weather_widget.xml` — il layout grafico del widget
- `res/xml/weather_widget_info.xml` — configurazione del widget (dimensioni, frequenza aggiornamento)

## Note

- Nessuna API key richiesta (Open-Meteo è gratuito e pubblico)
- Serve il permesso di posizione (`ACCESS_FINE_LOCATION`), richiesto la prima
  volta che apri l'app
- `minSdk = 26` → funziona da Android 8.0 in su
