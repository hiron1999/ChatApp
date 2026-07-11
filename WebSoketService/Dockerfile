#Build
FROM maven:3.9.8-eclipse-temurin-17-alpine AS build

WORKDIR /home/app
COPY . /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests


#Create image
ENV KAFKA_CONN_BS '0.0.0.0'
ENV REDIS_HOST '0.0.0.0'
ENV REDIS_PORT '0000'

RUN echo "kafka boot strap url : $KAFKA_CONN_BS , redis : $REDIS_HOST : $REDIS_PORT  "

FROM eclipse-temurin:17-alpine
EXPOSE 8080
COPY --from=build /home/app/target/WebSoketService-0.0.1-SNAPSHOT.jar WebSoketService-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["sh","-c","java -jar /WebSoketService-0.0.1-SNAPSHOT.jar"]