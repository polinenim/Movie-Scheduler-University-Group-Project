FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -B -e -DskipTests dependency:go-offline

# Build the shaded jar
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

ENV SERVER_PORT=8080
ENV JDBC_URL=jdbc:h2:file:/app/data/demodb;AUTO_SERVER=TRUE
ENV DB_USER=sa
ENV DB_PASS=
ENV JWT_SECRET=dev-secret-change-me

COPY --from=build /app/target/CineSchedule-1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
RUN mkdir -p /app/data
VOLUME ["/app/data"]

HEALTHCHECK --interval=30s --timeout=5s --retries=5 CMD curl -f http://localhost:${SERVER_PORT}/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
