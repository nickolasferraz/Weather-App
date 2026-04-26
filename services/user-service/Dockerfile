# Dockerfile — user-service (produção)
# Context: raiz do projeto (Weather-App/)
FROM maven:3.9-amazoncorretto-21 AS builder
WORKDIR /app

# 1. pom.xml pai (multi-module)
COPY pom.xml ./pom.xml

# 2. pom.xml dos commons
COPY common/common-events/pom.xml  ./common/common-events/pom.xml
COPY common/common-lib/pom.xml     ./common/common-lib/pom.xml
COPY common/saga-core/pom.xml      ./common/saga-core/pom.xml

# 3. pom.xml de TODOS os serviços — obrigatório porque o pom.xml pai
#    declara todos em <modules> e o Maven valida a árvore inteira
COPY services/user-service/pom.xml          ./services/user-service/pom.xml
COPY services/weather-service/pom.xml       ./services/weather-service/pom.xml
COPY services/routing-service/pom.xml       ./services/routing-service/pom.xml
COPY services/alert-service/pom.xml         ./services/alert-service/pom.xml
COPY services/observability-service/pom.xml ./services/observability-service/pom.xml

# 4. Instala os commons ANTES do go-offline — são artefatos internos, não existem em repo remoto
COPY common/common-events/src  ./common/common-events/src
COPY common/common-lib/src     ./common/common-lib/src
COPY common/saga-core/src      ./common/saga-core/src
RUN mvn install -pl common/common-events,common/common-lib,common/saga-core -DskipTests -q

# 5. Baixa dependências externas (commons já estão no .m2 do builder)
RUN mvn dependency:go-offline -q

# 6. Compila e empacota o serviço
COPY services/user-service/src ./services/user-service/src
RUN mvn package -pl services/user-service -DskipTests -q

FROM amazoncorretto:21-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=builder /app/services/user-service/target/*.jar app.jar
EXPOSE 8080
# G1GC: workload misto leitura/escrita, equilíbrio throughput/latência
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-jar", "app.jar"]
