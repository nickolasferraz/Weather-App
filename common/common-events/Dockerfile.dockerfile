# ─────────────────────────────────────────────────────────────
# Dockerfile — common-events
# Context: raiz do projeto (Weather-App/)
#
# USO: este módulo é uma biblioteca Maven, NÃO um serviço.
# Não sobe como container em produção.
#
# Para que serve este Dockerfile?
# 1. CI/CD: buildar e publicar o JAR num registry Maven (Nexus, GitHub Packages)
# 2. Build isolado para validar que o módulo compila sozinho
#
# Uso:
#   docker build -f common/common-events/Dockerfile -t common-events:latest .
# ─────────────────────────────────────────────────────────────
FROM maven:3.9-amazoncorretto-21 AS builder

WORKDIR /app

# Copia o pom.xml pai para resolver dependências entre módulos
COPY pom.xml ./pom.xml
COPY common/common-events/pom.xml ./common/common-events/pom.xml

RUN mvn dependency:go-offline -pl common/common-events -q

COPY common/common-events/src ./common/common-events/src

# Instala o JAR no repositório Maven local do container
RUN mvn install -pl common/common-events -DskipTests -q

# Resultado: JAR disponível em /root/.m2 para outros builds
# Em CI, publique com: mvn deploy -pl common/common-events
