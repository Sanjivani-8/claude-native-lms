# ---- Build stage ----
# Build context MUST be the repository root (lms-project/), so the parent pom.xml
# and all module sources are visible, e.g.:
#   docker build -f course-service/Dockerfile -t lms/course-service:1.0.0 .
FROM maven:3.9.9-eclipse-temurin-11 AS build
COPY zscaler-certs/zscaler.pem /tmp/zscaler.pem
RUN keytool -import -trustcacerts -noprompt -alias zscaler -cacerts -storepass changeit -file /tmp/zscaler.pem
WORKDIR /workspace

# Cache dependency resolution: copy only POMs first
COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY student-service/pom.xml student-service/pom.xml
COPY course-service/pom.xml course-service/pom.xml
COPY assignment-service/pom.xml assignment-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN mvn -q -pl course-service -am dependency:go-offline -DskipTests || true

# Now copy full sources and build just this module (+ its deps in the reactor)
COPY course-service course-service
RUN mvn -q -pl course-service -am package -DskipTests

# ---- Runtime stage ----
   FROM eclipse-temurin:11-jre-jammy
   RUN apt-get update && apt-get install -y --no-install-recommends wget && rm -rf /var/lib/apt/lists/* \
       && groupadd -r lms && useradd -r -g lms lms
WORKDIR /app
COPY --from=build /workspace/course-service/target/course-service.jar app.jar
RUN mkdir -p /data && chown -R lms:lms /app /data
USER lms

ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8083/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
