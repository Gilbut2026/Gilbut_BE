FROM openjdk:17-alpine

WORKDIR /app

ARG JAR_FILE=/build/libs/*-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime
