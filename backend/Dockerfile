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

# Heap em 60% da memória do container: o restante fica para metaspace, threads e código da JVM.
# Com 75% em um container de 512 MB (plano gratuito do Render), o processo pode estourar o limite e ser encerrado.
# SerialGC: o melhor para 1 CPU e pouca memória. Fica fixo para ser igual no treino do cache e na execução.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+ExitOnOutOfMemoryError -XX:+UseSerialGC"

# O jar é extraído (app.jar + lib/) porque o cache AOT exige um classpath fixo, igual no treino e na execução.
COPY --from=build /app/target/PomoHistoria-*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --destination extraido && rm app.jar

# Cache AOT (Java 25): uma rodada de treino sobe o Spring até o fim da configuração e grava as classes
# carregadas, já ligadas. Com 1 CPU, a inicialização caiu de ~11 s para ~4,4 s. A rodada de treino não acessa o
# banco: o Flyway fica desligado e o Hibernate não consulta o banco para descobrir o dialeto.
# Se um dia o cache não servir (ex.: outra versão da JVM), a JVM só avisa no log e sobe normalmente, sem ele.
# -XX:MaxRAM=512m: o treino calcula a memória como no servidor (512 MB no plano gratuito). Sem isso, a máquina de
# build, com muita memória, passa de 32 GB de heap, desliga os "compressed oops", e o servidor recusa o cache
# ("Unable to map shared spaces").
RUN java -XX:MaxRAM=512m -XX:AOTCacheOutput=app.aot \
        -Dspring.context.exit=onRefresh \
        -Dspring.flyway.enabled=false \
        -Dspring.jpa.hibernate.ddl-auto=none \
        -Dspring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect \
        -Dspring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false \
        -jar extraido/app.jar

USER app

# A porta real vem da variável PORT (o Render define); 8080 é o padrão.
EXPOSE 8080
ENTRYPOINT ["java", "-XX:AOTCache=app.aot", "-jar", "extraido/app.jar"]
