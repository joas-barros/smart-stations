# Estágio 1: Build (Compilação com Maven e Java 21)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila e gera o JAR (pula testes para ser mais rápido na demo)
RUN mvn clean package -DskipTests

# Estágio 2: Runtime (Apenas o JRE para rodar o app)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o JAR gerado no estágio anterior
# O nome do arquivo segue o artifactId e version do seu pom.xml
COPY --from=build /app/target/smart-stations-1.0-SNAPSHOT.jar app.jar

# Define a variável que controlará qual classe Main será executada
ENV MAIN_CLASS=""

# Comando de entrada: executa o Java apontando para a classe definida na variável
ENTRYPOINT ["sh", "-c", "java -cp app.jar ${MAIN_CLASS}"]