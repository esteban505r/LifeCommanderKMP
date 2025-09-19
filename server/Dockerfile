# ---- build stage ----
FROM gradle:8.11.1-jdk21 AS build
WORKDIR /src
# copy only the server module from the repo root build context
COPY server/ ./

# Build a runnable jar (try Ktor's buildFatJar first; then fallbacks)
# Also list build/libs so CI logs show what was produced.
RUN --mount=type=cache,target=/root/.gradle \
    gradle --no-daemon clean buildFatJar -x test || \
    gradle --no-daemon clean shadowJar  -x test || \
    gradle --no-daemon clean build       -x test && \
    echo "---- build/libs content ----" && ls -la build/libs && \
    # pick whatever jar was produced and stash to a fixed path \
    JAR="$(ls build/libs/*-all.jar 2>/dev/null || true)"; \
    if [ -z "$JAR" ]; then JAR="$(ls build/libs/*-fat.jar 2>/dev/null || true)"; fi; \
    if [ -z "$JAR" ]; then JAR="$(ls build/libs/*-shadow.jar 2>/dev/null || true)"; fi; \
    if [ -z "$JAR" ]; then JAR="$(ls build/libs/*.jar 2>/dev/null | head -n1 || true)"; fi; \
    if [ -z "$JAR" ]; then echo "No JAR produced under build/libs"; exit 1; fi; \
    cp "$JAR" /tmp/app.jar

# ---- runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# copy the jar captured above
COPY --from=build /tmp/app.jar /app/app.jar

# copy logback from the repository (build context is repo root)
COPY server/src/main/resources/logback.xml /app/logback.xml

ENV JAVA_TOOL_OPTIONS="-Dlogback.configurationFile=/app/logback.xml"
USER 10001
EXPOSE 8080
CMD ["java","-jar","/app/app.jar"]
