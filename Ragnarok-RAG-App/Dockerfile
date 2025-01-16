FROM openjdk:21-jdk-slim
VOLUME /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 7777

ENTRYPOINT ["java", "-jar", "app.jar"]