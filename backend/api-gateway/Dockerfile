# Stage 1: Build & Extract Layers
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy parent pom and dependencies
COPY pom.xml .
COPY common-library common-library
COPY api-gateway api-gateway

# Package the service module and its library dependencies
RUN mvn -pl api-gateway -am clean package -DskipTests

# Extract layered JAR
RUN cd api-gateway && java -Djarmode=layertools -jar target/api-gateway-*.jar extract

# Stage 2: Runtime Minimal Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for security hardening
RUN addgroup -S accio && adduser -S accio -G accio
USER accio

# Copy extracted layers
COPY --from=builder /workspace/api-gateway/dependencies/ ./
COPY --from=builder /workspace/api-gateway/spring-boot-loader/ ./
COPY --from=builder /workspace/api-gateway/snapshot-dependencies/ ./
COPY --from=builder /workspace/api-gateway/application/ ./

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8 -Duser.timezone=UTC"

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q "UP" || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
