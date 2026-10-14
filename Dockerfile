# Etapa de build: usa uma imagem com o Maven e Java 21 para compilar o projeto
FROM maven:3.9.8-eclipse-temurin-21 AS build
# Cria um diretório para armazenar os arquivos do projeto
RUN mkdir /opt/app
# Copia os arquivos do projeto para dentro do container
COPY . /opt/app
# Define o diretório de trabalho dentro do container
WORKDIR /opt/app
# Executa o build do projeto, gerando o arquivo JAR
# (os testes automatizados já são executados no job de CI do pipeline)
RUN mvn clean package -DskipTests

# Etapa de runtime: usa uma imagem leve com apenas o JRE para rodar o app
FROM eclipse-temurin:21-jre-alpine
# Cria o diretório da aplicação no ambiente final
RUN mkdir /opt/app
# Copia o JAR gerado na etapa de build para a imagem final
COPY --from=build /opt/app/target/app.jar /opt/app/app.jar
# Define o diretório de trabalho
WORKDIR /opt/app
# Define a variável de ambiente do profile (dev, stg ou prd)
ENV PROFILE=prd
# Expõe a porta 8080 (porta padrão da aplicação Spring Boot)
EXPOSE 8080
# Comando de inicialização, interpolando a variável PROFILE
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=$PROFILE -jar app.jar"]
