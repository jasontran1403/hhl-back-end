FROM openjdk:17-ea-33-jdk-slim-buster

WORKDIR /app
COPY ./target/hhl-0.0.9.jar /app
COPY src/main/resources/assets /app/src/main/resources/assets

CMD ["java", "-jar", "hhl-0.0.9.jar"]
