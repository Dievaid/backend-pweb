FROM maven:3.8.4-openjdk-17-slim as base

COPY . /app
WORKDIR /app

RUN mvn clean install

FROM openjdk:17-alpine
WORKDIR /app
COPY --from=base /app/target/web-0.0.1-SNAPSHOT.jar .

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "web-0.0.1-SNAPSHOT.jar"]
