FROM docker.io/rockylinux:9

RUN dnf update -y && \
    dnf install -y epel-release && \
    dnf update -y && \
    dnf install -y java-latest-openjdk && \
    dnf clean all

ADD target/http-service-1.0-SNAPSHOT.jar /http-service-1.0-SNAPSHOT.jar

ENTRYPOINT java -jar /http-service-1.0-SNAPSHOT.jar