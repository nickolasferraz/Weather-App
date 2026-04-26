# ─────────────────────────────────────────────────────────────
# Dockerfile — saga-core
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
#   docker build -f common/saga-core/Dockerfile -t saga-core:latest .
# ─────────────────────────────────────────────────────────────
FROM maven:3.9-amazoncorretto-21 AS builder

WORKDIR /app

# Copia o pom.xml pai para resolver dependências entre módulos
COPY pom.xml ./pom.xml
COPY common/saga-core/pom.xml ./common/saga-core/pom.xml

RUN mvn dependency:go-offline -pl common/saga-core -q

COPY common/saga-core/src ./common/saga-core/src

# Instala o JAR no repositório Maven local do container
RUN mvn install -pl common/saga-core -DskipTests -q

# Resultado: JAR disponível em /root/.m2 para outros builds
# Em CI, publique com: mvn deploy -pl common/saga-core
