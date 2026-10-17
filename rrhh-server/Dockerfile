FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -pl rrhh-server -am package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/rrhh-server/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
