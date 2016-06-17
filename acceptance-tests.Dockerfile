FROM maven:3.3.9-jdk-8

COPY target/ms-key-store-0.1.0-SNAPSHOT.jar /ms-key-store-0.1.0.jar
COPY src/main/docker/docker-entrypoint.sh /

RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
