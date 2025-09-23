# ---- build stage ----
FROM gradle:8.11.1-jdk18 AS build
WORKDIR /src

# Copy the entire repo so Gradle can see settings + version catalog + wrapper, etc.
COPY . .

# Build just the :server module (try fatJar -> shadowJar -> build)
RUN --mount=type=cache,target=/root/.gradle \
    gradle --no-daemon :server:fatJar   -x test || \
    gradle --no-daemon :server:shadowJar -x test || \
    gradle --no-daemon :server:build     -x test && \
    echo "---- server/build/libs content ----" && ls -la server/build/libs && \
    JAR="$(ls server/build/libs/*-all.jar 2>/dev/null || true)"; \
    if [ -z "$JAR" ]; then JAR="$(ls server/build/libs/*-fat.jar 2>/dev/null || true)"; fi; \
    if [ -z "$JAR" ]; then JAR="$(ls server/build/libs/*-shadow.jar 2>/dev/null || true)"; fi; \
    if [ -z "$JAR" ]; then JAR="$(ls server/build/libs/*.jar 2>/dev/null | head -n1 || true)"; fi; \
    if [ -z "$JAR" ]; then echo "No JAR produced under server/build/libs"; exit 1; fi; \
    cp "$JAR" /tmp/app.jar

# ---- runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /tmp/app.jar /app/app.jar
COPY server/src/main/resources/logback.xml /app/logback.xml
ENV JAVA_TOOL_OPTIONS="-Dlogback.configurationFile=/app/logback.xml"
USER 10001
EXPOSE 8080
CMD ["java","-jar","/app/app.jar"]
