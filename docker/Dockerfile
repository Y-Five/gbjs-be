# 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# 의존성 관련 파일 먼저 복사하여 캐시 최적화
COPY ../gradlew .
COPY ../gradle gradle
COPY ../build.gradle settings.gradle ./
COPY ../gradle.properties gradle.properties

# 실행 권한 부여
RUN chmod +x gradlew

# 의존성 캐시를 위한 명령 실행
RUN ./gradlew dependencies --no-daemon

# 소스 복사
COPY ../src src

# 프로젝트 빌드 (clean 제거, spotless 작업 제외)
RUN ./gradlew build -x test -x spotlessApply -x spotlessJava

# 런타임 스테이지
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드 결과 복사 (jar 이름 명시)
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}"]