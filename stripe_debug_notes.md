# Stato Debug Stripe - Ripresa Lavoro

Questo file riassume le modifiche effettuate e lo stato del debug per il pagamento Stripe, in modo da poter riprendere rapidamente domani.

## Stato Attuale dell'Integrazione

1. **Stripe Reale Abilitato**: Abbiamo configurato il progetto per utilizzare il vero SDK di Stripe in modalità Test (`stripe.mock=false` in `application.properties`).
2. **Conferma Lato Client**: Per evitare la necessità di configurare tunnel di webhook locali (come Stripe CLI o Ngrok), abbiamo aggiunto un endpoint di conferma manuale:
   - **Backend**: Endpoint `POST /itinerary/booking/{bookingId}/confirm` che sposta lo stato della prenotazione e delle relative attività a `CONFIRMED` nel database.
   - **Frontend**: Quando il `PaymentSheet` di Stripe si completa con successo sul telefono, l'app contatta questo endpoint sul server locale.
3. **Commit & Push**: Tutte le modifiche sono state committate e pushati sul branch `pagamento-mock` con il messaggio:
   `integrazione stripe con conferma client`

---

## Analisi del Problema "Invalid API Key"

Durante i test, la schermata di pagamento di Stripe sul cellulare mostrava ancora l'errore `invalid api key`. Abbiamo effettuato test diretti tramite curl verso i server di Stripe con le chiavi attualmente salvate nel file `.env`:

1. **Chiave Segreta (`STRIPE_API_KEY`) -> VALIDA**
   - La chiamata backend a Stripe ha successo. Il server crea correttamente il PaymentIntent e restituisce un ID valido (es. `pi_3TqKhs...`).
2. **Chiave Pubblica (`STRIPE_PUBLISHABLE_KEY`) -> INVALIDA**
   - Testando la chiave pubblica configurata (`pk_test_51Tq9...A1Zj`) direttamente tramite le API di Stripe, il server restituisce errore di autenticazione:
     `Invalid API Key provided: pk_test_***********************************************************************************************A1Zj`
   - **Motivo**: La chiave pubblica salvata in `.env` è obsoleta, errata, o è stata rigenerata/cancellata sulla Dashboard di Stripe.

---

## Passi da Seguire Domani

1. **Inserire le Chiavi Corrette**:
   - Accedere alla Stripe Dashboard (in modalità Test): [dashboard.stripe.com/test/apikeys](https://dashboard.stripe.com/test/apikeys).
   - Copiare la **Chiave pubblicabile** (`pk_test_...`) e incollarla nel file `.env` alla riga `STRIPE_PUBLISHABLE_KEY`.
   - Copiare la **Chiave segreta** (`sk_test_...`) e incollarla alla riga `STRIPE_API_KEY`.
   - **Salvare il file `.env`**.

2. **Aggiornare il Backend**:
   - Riavviare il container Docker per caricare la nuova chiave segreta:
     ```bash
     docker compose down
     docker compose up
     ```

3. **Compilare e Avviare il Frontend**:
   - Compilare la nuova chiave pubblica nell'app ed avviarla sull'emulatore:
     ```powershell
     cd frontend
     ./gradlew installDebug
     ```
   - (Opzionale) Se l'app non si aggiorna, eseguire la reinstallazione pulita terminando prima il processo:
     ```powershell
     adb shell am force-stop com.travel.app
     adb shell am start -n com.travel.app/.MainActivity
     ```
