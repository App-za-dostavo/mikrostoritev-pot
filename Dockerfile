FROM amazoncorretto:18

RUN mkdir /app

WORKDIR /app

ADD ./api/target/api-1.0-SNAPSHOT.jar /app

EXPOSE 8080

CMD ["java", "-jar", "api-1.0-SNAPSHOT.jar"]
