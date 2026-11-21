FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace
COPY pom.xml ./pom.xml
COPY src ./src
RUN for attempt in 1 2 3 4 5; do \
      wget -q -T 60 https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.tar.gz -O /tmp/maven.tgz && break; \
      rm -f /tmp/maven.tgz; \
      sleep 5; \
    done \
    && tar -xzf /tmp/maven.tgz -C /opt \
    && for attempt in 1 2 3; do \
      /opt/apache-maven-3.9.16/bin/mvn -q -Daether.transport.http.retryHandler.count=10 -DskipTests package && break; \
      if [ "$attempt" -eq 3 ]; then exit 1; fi; \
      sleep 10; \
    done

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/target/codementor-api-0.1.0-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]
