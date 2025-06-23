FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace/app

# Instalar Gradle
RUN apk add --no-cache gradle

# Copiar archivos del proyecto
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Construir el proyecto
RUN gradle build -x test

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
