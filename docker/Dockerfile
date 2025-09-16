FROM eclipse-temurin:17-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}"]