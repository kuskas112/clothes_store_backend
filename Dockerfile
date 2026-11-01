# ---------- build ----------
FROM maven:3.9-eclipse-temurin-26 AS builder

WORKDIR /app

# Сначала только pom.xml: слой с зависимостями переиспользуется,
# пока не изменился pom. Раньше COPY src инвалидировал кэш на каждую правку кода.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:26-jre-alpine

# Не работаем под root.
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=builder /app/target/clothesstore-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/uploads && chown -R app:app /app
USER app

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
