FROM openjdk:11.0.11-jre-slim-buster
ADD KafkaStreamProject*.jar app.jar
RUN sh -c 'touch /app.jar'
CMD ["java","-jar","/app.jar"]