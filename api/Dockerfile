FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY ./target/grill-gauge-api.jar /app/grill-gauge-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "grill-gauge-api.jar"]