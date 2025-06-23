FROM maven:3.9.7-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
