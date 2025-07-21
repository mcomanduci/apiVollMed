# Etapa final: imagem mínima com Java 21
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copia o JAR do projeto (ajuste o nome do JAR se for diferente)
COPY target/api-0.0.1-SNAPSHOT.jar app.jar

# Porta padrão do Spring Boot
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
