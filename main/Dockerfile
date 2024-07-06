# 베이스 이미지 설정
FROM openjdk:17

# 작업 디렉토리 설정
WORKDIR /app

# 호스트의 JAR파일을 Docker 이미지로 복사
COPY build/libs/dividendpj-0.0.1-SNAPSHOT.jar dividendpj.jar

# 애플리케이션 실행을 위한 커맨드 설정
CMD ["java", "-jar", "dividendpj.jar"]