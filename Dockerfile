FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# 소스 코드 복사
COPY . .

# 프로젝트 빌드
RUN ./gradlew build -x test

# 런타임 이미지
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 이미지에서 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"] 