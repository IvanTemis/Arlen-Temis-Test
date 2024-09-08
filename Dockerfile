# Usa una imagen oficial de OpenJDK como base
FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo en /app
WORKDIR /app

# Copia el archivo JAR generado a /app
COPY target/app-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto en el que correrá la aplicación (por defecto es 8080 en Spring Boot)
EXPOSE 8080

# Ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
