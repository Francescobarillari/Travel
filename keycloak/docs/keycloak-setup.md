# Setup Keycloak per Travel

Il realm Keycloak viene importato automaticamente da Docker usando:

```text
keycloak/ae-realm-realm.json
```

Il JSON di import deve restare JSON valido, quindi la procedura di test sta in questo file.
Il servizio Docker `keycloak-config` completa automaticamente la configurazione assegnando al client tecnico del backend i permessi necessari per creare utenti.

## Architettura

Keycloak gestisce:

```text
email / username
password
ruoli
login
JWT
```

Il database Travel gestisce:

```text
profilo applicativo
nome e cognome
esperienze
preferiti
altri dati di dominio
```

Flusso registrazione:

```text
frontend -> POST /api/auth/signup -> backend
backend -> crea utente su Keycloak
backend -> salva profilo applicativo nel DB Travel
```

Flusso login:

```text
frontend -> POST /api/auth/login -> backend
backend -> verifica credenziali su Keycloak
backend -> restituisce access_token Keycloak
frontend -> chiama API con Authorization: Bearer <access_token>
```

## Avvio

Avvia Docker Desktop, poi dalla root del progetto:

```powershell
docker compose up -d
```

Se Keycloak era gia stato creato prima delle modifiche al realm:

```powershell
docker compose up -d --force-recreate keycloak keycloak-config
docker compose restart backend
```

Servizi:

```text
Backend:  http://localhost:8080
Keycloak: http://localhost:8081
Swagger:  http://localhost:8080/swagger-ui/index.html
```

Console admin Keycloak:

```text
URL:      http://localhost:8081
Username: admin
Password: admin
```

## Configurazione Importata

Realm:

```text
ae-realm
```

Client usato per login/token:

```text
client_id:     ae-client
client_secret: travel-dev-secret
```

Client usato dal backend per creare utenti su Keycloak:

```text
client_id:     travel-backend-admin
client_secret: travel-backend-admin-secret
```

Questo client usa il flow `client_credentials`. Il frontend non deve conoscere questo secret: lo usa solo il backend tramite le variabili nel `docker-compose.yml`.

Utenti di test gia presenti:

```text
basic-user / password
admin-user / password
```

Ruoli:

```text
basic-user -> BASIC
admin-user -> BASIC, ADMIN
```

## Test Registrazione Dal Backend

Metodo:

```text
POST
```

URL:

```text
http://localhost:8080/api/auth/signup
```

Tab `Body` -> `raw` -> `JSON`:

```json
{
  "email": "new-user@example.com",
  "password": "Password1",
  "firstName": "New",
  "lastName": "User"
}
```

Risultato atteso:

```text
registrazione completata
```

Effetto:

```text
Keycloak: utente new-user@example.com con password Password1
DB Travel: profilo con firstName/lastName/email
```

## Test Login Dal Backend

Metodo:

```text
POST
```

URL:

```text
http://localhost:8080/api/auth/login
```

Tab `Body` -> `raw` -> `JSON`:

```json
{
  "email": "new-user@example.com",
  "password": "Password1"
}
```

Risultato atteso:

```text
eyJ...
```

La risposta e il JWT Keycloak da usare come Bearer token.

## Test Endpoint Protetto

Metodo:

```text
GET
```

URL:

```text
http://localhost:8080/api/basic
```

Tab `Authorization`:

```text
No Auth
```

Tab `Headers`:

```text
Authorization: Bearer <token restituito da /api/auth/login>
```

Risultato atteso:

```text
Accesso BASIC consentito
```

## Test Admin

Fai login con:

```json
{
  "email": "admin-user",
  "password": "password"
}
```

Poi chiama:

```text
GET http://localhost:8080/api/admin
```

Risultato atteso:

```text
Accesso ADMIN consentito
```

Con un utente solo `BASIC`, `/api/admin` deve rispondere:

```text
403 Forbidden
```

## Test Diretto Su Keycloak

Serve solo per debug. Il frontend normalmente non deve conoscere il client secret.

Metodo:

```text
POST
```

URL:

```text
http://localhost:8081/realms/ae-realm/protocol/openid-connect/token
```

Tab `Body` -> `x-www-form-urlencoded`:

```text
client_id      ae-client
client_secret  travel-dev-secret
grant_type     password
username       basic-user
password       password
```

## Note

I token durano pochi minuti. Se ricevi `401 Unauthorized`, genera un token nuovo.

In produzione i secret non devono stare nel repository. Per sviluppo locale sono nel realm importato per rendere il progetto testabile dal team.
