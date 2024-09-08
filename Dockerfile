# Usa una imagen oficial de OpenJDK como base
FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo en /app
WORKDIR /app

ENV HOST 0.0.0.0

#Establece un argumento de ejecución
ARG JAR_FILE

# Copia el archivo JAR generado a /app
COPY ${JAR_FILE} app.jar

# Expone el puerto en el que correrá la aplicación (por defecto es 8080 en Spring Boot)
EXPOSE 8080

# Ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "${JAR_FILE}"]
