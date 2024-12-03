# Usa una imagen oficial de OpenJDK como base
FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo en /app
WORKDIR /app

# Define una variable de entorno para el host
ENV HOST 0.0.0.0

# Copia el archivo JAR generado a /app
COPY target/app-0.0.1-SNAPSHOT.jar app.jar

# Copia el archivo secrets.properties al contenedor
#COPY secrets.properties /app/secrets.properties

# Expone el puerto en el que correrá la aplicación (por defecto es 8080 en Spring Boot)
EXPOSE 8080

# Ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]