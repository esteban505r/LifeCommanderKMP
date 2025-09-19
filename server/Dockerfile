# ---- build stage ----
FROM gradle:8.11.1-jdk21 AS build
WORKDIR /src
# copy just the server module from the repo root (your build context must be the repo root)
COPY server/ ./

# Try common fat-jar tasks (fatJar, then shadowJar). Fall back to build.
# Then capture ANY produced jar into /tmp/app.jar so the runtime stage is simple.
RUN --mount=type=cache,target=/root/.gradle \
    gradle --no-daemon clean fatJar   -x test || \
    gradle --no-daemon clean shadowJar -x test || \
    gradle --no-daemon clean build     -x test && \
    echo "---- build/libs content ----" && ls -la build/libs && \
    JAR="$(ls build/libs/*-all.jar 2>/dev/null || true)"; \
    if [ -z "$JAR" ]; then JAR="$(ls build/libs/*-fat.jar 2>/dev/null || true)"; fi; \
    if [ -z "$JAR" ]; then JAR="$(ls build/libs/*-shadow.jar 2>/dev/null || true)"; fi; \
    if [ -z "$JAR" ]; then JAR="$(ls build/libs/*.jar 2>/dev/null | head -n1 || true)"; fi; \
    if [ -z "$JAR" ]; then echo "No JAR produced under build/libs"; exit 1; fi; \
    cp "$JAR" /tmp/app.jar

# ---- runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /tmp/app.jar /app/app.jar
# logback is in the repo at server/src/main/resources/
COPY server/src/main/resources/logback.xml /app/logback.xml
ENV JAVA_TOOL_OPTIONS="-Dlogback.configurationFile=/app/logback.xml"
USER 10001
EXPOSE 8080
CMD ["java","-jar","/app/app.jar"]
