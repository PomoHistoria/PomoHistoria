FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# -Ddocker desliga o perfil de testes do pom.xml (PostgreSQL embutido, ~75 MB que a imagem não usa).
# Os testes rodam no desenvolvimento (./mvnw test); aqui só empacota, sem nem compilar os testes.
ARG MAVEN_FLAGS="-B -Ddocker -Dmaven.test.skip=true"

COPY mvnw pom.xml ./
COPY .mvn .mvn
# Camada só com as dependências: é reaproveitada enquanto o pom.xml não mudar.
RUN chmod +x mvnw && ./mvnw $MAVEN_FLAGS dependency:go-offline

COPY src src
RUN ./mvnw $MAVEN_FLAGS package

FROM eclipse-temurin:25-jre

# Roda sem privilégios de root.
RUN groupadd --system app && useradd --system --gid app --no-create-home app

WORKDIR /app
COPY --from=build --chown=app:app /app/target/PomoHistoria-*.jar app.jar

USER app

# Heap em 60% da memória do container: o restante fica para metaspace, threads e código da JVM.
# Com 75% em um container de 512 MB (plano gratuito do Render), o processo pode estourar o limite e ser encerrado.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+ExitOnOutOfMemoryError"

# A porta real vem da variável PORT (o Render define); 8080 é o padrão.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
