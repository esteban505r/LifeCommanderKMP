# ---- build stage ----
FROM gradle:8.11.1-jdk17 AS build
WORKDIR /src

# Copy the entire repo so Gradle can see settings + version catalog + wrapper, etc.
COPY . .

# Build the :server module using shadowJar
RUN --mount=type=cache,target=/root/.gradle \
    gradle --no-daemon :server:shadowJar -x test && \
    echo "---- server/build/libs content ----" && ls -la server/build/libs && \
    # Find the shadowJar output file
    JAR="$(ls server/build/libs/*-all.jar 2>/dev/null || ls server/build/libs/*-shadow.jar 2>/dev/null | head -n1 || true)"; \
    if [ -z "$JAR" ]; then echo "No JAR produced under server/build/libs"; exit 1; fi; \
    cp "$JAR" /tmp/app.jar

# ---- runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the JAR from the build stage and the logback configuration
COPY --from=build /tmp/app.jar /app/app.jar
COPY server/src/main/resources/logback.xml /app/logback.xml

# Set environment variable for Logback configuration
ENV JAVA_TOOL_OPTIONS="-Dlogback.configurationFile=/app/logback.xml"

# Use a non-root user for security
USER 10001

# Expose the app port
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "/app/app.jar"]
