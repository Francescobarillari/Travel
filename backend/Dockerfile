# Stadio 1: Compila common-dtos
FROM maven:3.9.6-eclipse-temurin-21 AS common-build
WORKDIR /common
COPY ../in_common /common
RUN mvn clean install -DskipTests

# Stadio 2: Compila il backend
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copia il JAR di common-dtos dal primo stadio nel repository Maven locale
COPY --from=common-build /root/.m2/repository/it/unical/ea/common-dtos /root/.m2/repository/it/unical/ea/common-dtos
COPY . .
RUN mvn clean package -DskipTests

# Stadio 3: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]