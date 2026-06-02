# Simple single-stage build - uses pre-built JAR from local build
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user
RUN groupadd -r banking && useradd -r -g banking banking

# Copy pre-built JAR from local target directory
COPY target/aurum-core-banking-1.0.0-SNAPSHOT.jar app.jar

# Ensure application directories exist and are owned by the non-root user
RUN mkdir -p /opt/aurum/logs /opt/aurum/tmp && chown -R banking:banking /opt/aurum || true

# Change ownership of the app directory
RUN chown -R banking:banking /app

USER banking

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
