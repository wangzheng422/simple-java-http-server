FROM docker.io/eclipse-temurin:22

ADD target/http-service-1.0-SNAPSHOT.jar /http-service-1.0-SNAPSHOT.jar

RUN wget -O /opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# CMD ["java", "-jar", "/http-service-1.0-SNAPSHOT.jar"]

ENTRYPOINT java -jar -javaagent:/opentelemetry-javaagent.jar /http-service-1.0-SNAPSHOT.jar