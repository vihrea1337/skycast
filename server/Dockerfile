# Образ сервера SkyCast: готовый fat-jar в лёгком JRE-образе.
# Джарник собираем локально заранее (из корня репозитория): ./gradlew.bat :server:buildFatJar
# Затем: docker build -t skycast-server -f server/Dockerfile server
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/server-all.jar /app/app.jar
ENV HOST=0.0.0.0 \
    PORT=8090
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
