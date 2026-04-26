# ─────────────────────────────────────────────────────────────
# Dockerfile — weather-service (produção, multistage)
# Context: raiz do projeto (Weather-App/)
# ─────────────────────────────────────────────────────────────

# ── Stage 1: Build ────────────────────────────────────────────
FROM maven:3.9-amazoncorretto-21 AS builder

WORKDIR /app

COPY pom.xml ./pom.xml
COPY common/common-events/pom.xml  ./common/common-events/pom.xml
COPY common/common-lib/pom.xml     ./common/common-lib/pom.xml
COPY common/saga-core/pom.xml      ./common/saga-core/pom.xml
COPY services/user-service/pom.xml          ./services/user-service/pom.xml
COPY services/weather-service/pom.xml       ./services/weather-service/pom.xml
COPY services/routing-service/pom.xml       ./services/routing-service/pom.xml
COPY services/alert-service/pom.xml         ./services/alert-service/pom.xml
COPY services/observability-service/pom.xml ./services/observability-service/pom.xml

# Instala os commons ANTES do go-offline — são artefatos internos, não existem em repo remoto
COPY common/common-events/src  ./common/common-events/src
COPY common/common-lib/src     ./common/common-lib/src
COPY common/saga-core/src      ./common/saga-core/src
RUN mvn install -pl common/common-events,common/common-lib,common/saga-core -DskipTests -q

# Baixa dependências externas (commons já estão no .m2 do builder)
RUN mvn dependency:go-offline -q

COPY services/weather-service/src ./services/weather-service/src
RUN mvn package -pl services/weather-service -DskipTests

# ── Stage 2: Runtime (imagem leve) ────────────────────────────
FROM amazoncorretto:21-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/services/weather-service/target/*.jar app.jar

EXPOSE 8080

# ZGC: latência baixa para cache de radar de alta frequência
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseZGC", \
  "-jar", "app.jar"]