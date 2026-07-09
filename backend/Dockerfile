# =========================================================================
# Dockerfile multi-stage per il backend Spring Boot (BUILD -> SHIP -> RUN)
#
# Il contesto di build è la ROOT del progetto, così che lo stadio di build
# veda sia backend/ sia in_common/ (la libreria condivisa dei DTO).
# Costruire con:  docker build -t travel-backend:1.0 -f backend/Dockerfile .
# oppure tramite docker compose (build.context: .).
# =========================================================================

# --- Stadio 1: BUILD (contiene Maven, viene scartato) -------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /src

# Compila e installa prima la libreria condivisa nel repository Maven locale
COPY in_common /src/in_common
RUN mvn -f /src/in_common/pom.xml clean install -DskipTests

# Compila e impacchetta il backend in un fat jar eseguibile
COPY backend /src/backend
RUN mvn -f /src/backend/pom.xml clean package -DskipTests

# --- Stadio 2: RUN (solo JRE + jar, immagine leggera) -------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Esegue l'applicazione come utente non privilegiato e prepara le directory
# scrivibili a runtime (log applicativi e upload) con i permessi corretti.
RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/logs /app/uploads \
    && chown -R app:app /app

# Copia solo l'artefatto compilato dallo stadio di build
COPY --from=build --chown=app:app /src/backend/target/*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
