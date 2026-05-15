# Setup Keycloak per Travel

Il realm Keycloak viene importato automaticamente da Docker usando:

```text
keycloak/ae-realm-realm.json
```

Il JSON di import deve restare JSON valido, quindi non puo contenere commenti. Questa guida contiene la procedura di test per il team.

## Avvio

Avvia Docker Desktop, poi dalla root del progetto:

```powershell
docker compose up -d
```

Servizi esposti:

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

Client:

```text
client_id:     ae-client
client_secret: travel-dev-secret
```

Utenti di test:

```text
basic-user / password
admin-user / password
```

Ruoli:

```text
basic-user -> BASIC
admin-user -> BASIC, ADMIN
```

## Test Con Postman

### 1. Richiedi un token BASIC

Metodo:

```text
POST
```

URL:

```text
http://localhost:8081/realms/ae-realm/protocol/openid-connect/token
```

Tab `Authorization`:

```text
No Auth
```

Tab `Body` -> `x-www-form-urlencoded`:

```text
client_id      ae-client
client_secret  travel-dev-secret
grant_type     password
username       basic-user
password       password
```

Copia solo il valore di `access_token`.

### 2. Testa endpoint BASIC

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
Authorization: Bearer <access_token>
```

Risultato atteso:

```text
Accesso BASIC consentito
```

### 3. Testa endpoint ADMIN con basic-user

Metodo:

```text
GET
```

URL:

```text
http://localhost:8080/api/admin
```

Usa lo stesso header `Authorization` del token `basic-user`.

Risultato atteso:

```text
403 Forbidden
```

### 4. Richiedi un token ADMIN

Ripeti la richiesta token, cambiando:

```text
username       admin-user
password       password
```

Usa il nuovo `access_token` su:

```text
GET http://localhost:8080/api/admin
```

Risultato atteso:

```text
Accesso ADMIN consentito
```

## Test Con PowerShell

Token BASIC:

```powershell
$body = @{
  client_id = "ae-client"
  client_secret = "travel-dev-secret"
  grant_type = "password"
  username = "basic-user"
  password = "password"
}

$tokenResponse = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8081/realms/ae-realm/protocol/openid-connect/token" `
  -Body $body

$token = $tokenResponse.access_token

Invoke-RestMethod `
  -Headers @{ Authorization = "Bearer $token" } `
  -Uri "http://localhost:8080/api/basic"
```

Token ADMIN:

```powershell
$body.username = "admin-user"

$tokenResponse = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8081/realms/ae-realm/protocol/openid-connect/token" `
  -Body $body

$token = $tokenResponse.access_token

Invoke-RestMethod `
  -Headers @{ Authorization = "Bearer $token" } `
  -Uri "http://localhost:8080/api/admin"
```

## Note Operative

I token durano pochi minuti. Se ricevi `401 Unauthorized`, genera un token nuovo e riprova.

Se Keycloak era gia stato avviato prima dell'import automatico, ricrea il container Keycloak:

```powershell
docker compose up -d --force-recreate keycloak
docker compose restart backend
```

Gli endpoint `/api/auth/login` e `/api/auth/signup` appartengono alla vecchia autenticazione JWT interna del progetto. Con Keycloak, i token validi per accedere alle API devono essere quelli emessi da Keycloak.
