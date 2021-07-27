FROM openjdk:11.0.11-jre-slim-buster
ADD KafkaStreamProject*.jar app.jar
RUN sh -c 'touch /app.jar'
ENV RUNNER "populate-csv-data"
ENTRYPOINT ["java","-jar", "-Drunner=${RUNNER}", "/app.jar"]