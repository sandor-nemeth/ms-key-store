FROM java:8u92-jdk-alpine

COPY target/ms-key-store-0.1.0-SNAPSHOT.jar /ms-key-store-0.1.0.jar
COPY src/main/docker/docker-entrypoint.sh /

RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

CMD ["java", "-Xmx128m", "-Xss256k", "-jar", "/ms-key-store-0.1.0.jar"]