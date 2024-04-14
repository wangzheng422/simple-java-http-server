FROM docker.io/rocky:9

RUN dnf update -y && dnf install -y java-latest-openjdk

ADD target/http-service-1.0-SNAPSHOT.jar /http-service-1.0-SNAPSHOT.jar

ENTRYPOINT java -jar /http-service-1.0-SNAPSHOT.jar