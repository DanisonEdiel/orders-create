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
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.example.ordercreator.OrderCreatorApplication"]
