# Dèrive  - Guida Completa alla Configurazione e Sviluppo


### Componenti Principali:
1. **Frontend (`frontend`)**: Applicazione nativa Android sviluppata in Kotlin utilizzando **Jetpack Compose** per la UI. Integra Retrofit per le comunicazioni API, OkHttp, Coil per il caricamento delle immagini e il PayPal Checkout Android SDK per le transazioni.
2. **Backend (`backend`)**: Web API basata su **Spring Boot 3.x (Java 21)**. Si occupa della logica di business, della persistenza dei dati, dell'invio delle mail, della gestione dei caricamenti di file e dell'interazione con servizi di terze parti come PayPal e Unsplash.
3. **Libreria Condivisa (`in_common`)**: Modulo Java contenente le definizioni comuni di DTO (Data Transfer Objects) e Enum. Assicura che la struttura dei dati scambiati tra Frontend e Backend sia sempre sincronizzata ed evita la duplicazione del codice.
4. **Identity & Access Management (`keycloak`)**: Gestione di accessi, registrazioni e ruoli utente delegata a **Keycloak (quay.io/keycloak/keycloak:latest)** tramite il protocollo OpenID Connect (OIDC).
5. **Database (`db`)**: Database relazionale **PostgreSQL 15** per la memorizzazione dei dati di dominio dell'applicazione (es. profili utente, esperienze di viaggio, prenotazioni, preferiti).

---
## Configurazione Iniziale (Primo Clone)

Segui rigorosamente questi passaggi per configurare l'ambiente locale al primo clone del repository.

### 1. Configurazione del file `.env` (Root del Progetto)

Crea un file nominato `.env` nella directory principale (**root**) del progetto. Questo file definisce le variabili d'ambiente condivise tra i container Docker.

> [!IMPORTANT]
> È necessario impostare `HOST_IP` con l'indirizzo IP locale del tuo computer all'interno della rete Wi-Fi/LAN. **Non utilizzare `localhost`** se intendi testare l'app su un dispositivo fisico o sull'emulatore Android, altrimenti il dispositivo mobile non riuscirà a contattare i servizi in esecuzione sul computer.

Usa il seguente template per il file `.env`:

```env
# IP del computer di sviluppo nella rete locale
HOST_IP=<ip>

# Keycloak
KEYCLOAK_REALM=ae-realm
KEYCLOAK_APP_CLIENT_ID=ae-client
KEYCLOAK_APP_CLIENT_SECRET=travel-dev-secret
KEYCLOAK_ADMIN_CLIENT_ID=travel-backend-admin
KEYCLOAK_ADMIN_CLIENT_SECRET=travel-backend-admin-secret
KEYCLOAK_DEFAULT_CLIENT_ROLE=BASIC

# Database
SPRING_DATASOURCE_USERNAME=myuser
SPRING_DATASOURCE_PASSWORD=mypassword

# Payment Config
# Set PAYMENT_MOCK=true to test payment flow locally/offline (mock gateway)
# Set PAYMENT_MOCK=false to connect to PayPal Sandbox
PAYMENT_MOCK=false

PAYPAL_CLIENT_ID=AY5DQrR9-oKBLflS7_YhfZMR9ooyLgKs82vID1iyMUJ1vlsdXVeQUnVejDP0zHmxmzfgbjCqHBvWfU2z
PAYPAL_CLIENT_SECRET=EGcOQJTQaCtKqbLBRPIQUfGL6MSNU39lP9B5ZOM1GKUlZmhi-G2uSYuHxbhI3-eb8lp_grki_Wl1sUmD
```

*   `PAYMENT_MOCK`: Impostalo su `true` se vuoi bypassare i controlli reali di PayPal ed effettuare pagamenti di test istantanei simulati dal backend.

---

### 2. Allineamento e Compilazione della Libreria Condivisa (`in_common`) — solo per l'app Android

> [!NOTE]
> Questo passaggio serve **esclusivamente per l'app Android** (Android Studio compila il frontend fuori da Docker). Per il **backend non è necessario**: il `Dockerfile` compila `in_common` automaticamente all'interno dell'immagine.

Poiché il file `.jar` compilato del modulo `in_common` è escluso dal controllo di versione (`.gitignore`), è necessario compilarlo manualmente la prima volta (e ogni volta che apporti modifiche ai DTO in `in_common`) per consentire ad Android Studio di riconoscerlo.

Esegui il comando corrispondente al tuo sistema operativo dalla cartella root del progetto:

#### Windows (PowerShell):
```powershell
mvn -f in_common/pom.xml clean package ; Copy-Item -Path "in_common\target\common-dtos-1.0.0.jar" -Destination "frontend\app\libs\common-dtos-1.0.0.jar" -Force
```

#### macOS / Linux (Bash):
```bash
mvn -f in_common/pom.xml clean package && cp in_common/target/common-dtos-1.0.0.jar frontend/app/libs/
```

### 3. Configurazione del file `local.properties` (Frontend)

Apri la cartella `frontend` all'interno di Android Studio. Il file `local.properties` viene generato automaticamente puntando al tuo SDK Android. Apri questo file e aggiungi le configurazioni per indicare all'applicazione Android dove si trovano il backend e Keycloak.

Modifica/Aggiungi le seguenti righe sostituendo `<IP_DEL_PROPRIO_PC>` con lo stesso IP inserito in `.env`:

```properties
# Indirizzo del server backend
backend.url=http://<IP_DEL_PROPRIO_PC>:8080/

# Indirizzo del server Keycloak
keycloak.url=http://<IP_DEL_PROPRIO_PC>:8081/
```

---

## Avvio dell'Infrastruttura (Docker Compose)

Il progetto include una configurazione Docker Compose completa che si occupa di istanziare, collegare e configurare tutti i servizi server.

Per avviare l'infrastruttura, apri un terminale nella root del progetto ed esegui:

```bash
docker compose up -d --build
```

Questo comando avvierà i seguenti servizi:

| Servizio | Porta Esterna | Descrizione |
| :--- | :--- | :--- |
| **db** | `5433` (interna `5432`) | Database PostgreSQL per la persistenza locale. |
| **keycloak** | `8081` (interna `8080`) | Server di identità e accessi OIDC. |
| **keycloak-config** | *Temporaneo* | Script automatico che attende l'avvio di Keycloak, verifica l'importazione del realm e configura i permessi di amministrazione per il backend. |
| **backend** | `8080` (interna `8080`) | Applicazione Spring Boot. Costruita da `backend/Dockerfile` (immagine multi-stage: build con Maven, runtime su JRE Alpine come utente non-root) ed eseguita come `.jar` precompilato. L'autenticazione OIDC è sempre attiva. |

### Aggiornare il backend dopo una modifica al codice

> [!IMPORTANT]
> Il backend gira da un'**immagine** che contiene il `.jar` già compilato, **non** più montando i sorgenti dal disco. Di conseguenza, dopo aver modificato il codice del backend il semplice restart **non** basta: bisogna **ricostruire l'immagine**.

| Cosa hai modificato | Comando |
| :--- | :--- |
| Codice backend (`.java`), `pom.xml`, `in_common` | `docker compose up -d --build backend` |
| Solo variabili d'ambiente (`.env`) | `docker compose up -d backend` (senza `--build`) |
| Codice Android | Nessuna azione Docker — si ricompila in Android Studio |

La regola pratica: **dopo un `git pull` o una modifica al backend, usa sempre `--build`**. La prima build scarica le dipendenze Maven (qualche minuto); le successive sfruttano la cache e sono rapide.

> [!TIP]
> Se durante lo sviluppo il `--build` continuo è scomodo, si può creare un `docker-compose.override.yml` (non versionato) che rimonta i sorgenti ed esegue `mvn spring-boot:run`, mantenendo il `docker-compose.yml` ufficiale con l'immagine per la consegna. Docker Compose fonde automaticamente i due file.

### Ripristino dell'ambiente in caso di errori
Se si riscontrano problemi di consistenza dei dati, modifiche al file realm o errori nei ruoli utente su Keycloak, è consigliabile eliminare i volumi Docker e riavviare da zero:

> [!WARNING]
> `docker compose down -v` elimina **anche il volume del database** (`postgres_data`): tutti i dati locali andranno persi. Per un semplice aggiornamento del codice usa `docker compose up -d --build`, non `down -v`.

```bash
# Arresta e rimuove tutti i container e i volumi associati (incluso il DB)
docker compose down -v

# Riavvia forzando la compilazione
docker compose up -d --build
```

---

## Test e Verifica dell'Ambiente (API Testing)

Dopo avviare Docker Compose, puoi verificare che tutto funzioni correttamente effettuando dei test sulle API del backend tramite **Swagger UI** o client come Postman.

### Documentazione Interattiva (Swagger):
*   **URL**: `http://localhost:8080/swagger-ui/index.html`

---

## 🔑 Credenziali di Test per la Valutazione (Preconfigurate)

Per facilitare la fase di test e valutazione del progetto, sono disponibili le seguenti credenziali preconfigurate all'interno dell'ambiente:

### 👤 Account Utenti (Keycloak)
È possibile effettuare il login nell'applicazione (o tramite Swagger/Postman) utilizzando uno dei seguenti profili configurati nel realm `ae-realm`:

| Ruolo | Username | Password | Email | Note |
| :--- | :--- | :--- | :--- | :--- |
| **Basic User** | `a` | `a` | `a@a.com` | Profilo utente rapido per test veloci |
| **Basic User** | `basic-user` | `password` | `basic-user@example.com` | Profilo utente standard |
| **Administrator** | `admin-user` | `password` | `admin-user@example.com` | Profilo con permessi amministrativi completi |

---

### 🛡️ Amministrazione Keycloak
Per accedere alla console di amministrazione di Keycloak (disponibile a `http://localhost:8081` una volta avviato Docker):
*   **Username**: `admin`
*   **Password**: `admin`
*   **Realm**: `ae-realm`
*   **Client ID Applicazione**: `ae-client` (Secret: `travel-dev-secret`)
*   **Client ID Amministratore**: `travel-backend-admin` (Secret: `travel-backend-admin-secret`)

---

### 💾 Database PostgreSQL
Se si desidera analizzare o connettersi direttamente al database PostgreSQL (porta esterna `5433`):
*   **Host**: `localhost`
*   **Porta**: `5433`
*   **Database**: `my_project_db`
*   **Username**: `myuser`
*   **Password**: `mypassword`

---

### ✉️ Server di Posta Locale (Mailpit)
Tutte le email inviate dal backend (es. registrazioni, notifiche o conferme) vengono intercettate localmente e possono essere visualizzate tramite la dashboard web:
*   **URL Dashboard**: `http://localhost:8025/`

