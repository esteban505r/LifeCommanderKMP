# ---- build stage ----
FROM gradle:8.11.1-jdk21 AS build
WORKDIR /src
COPY .. .
RUN echo "Contents of /src after COPY:" && ls -R .
RUN --mount=type=cache,target=/root/.gradle gradle clean shadowJar -x test

# ---- runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/libs/*-all.jar /app/app.jar
RUN ls -R /src
COPY server/src/main/resources/logback.xml /app/logback.xml
ENV JAVA_TOOL_OPTIONS="-Dlogback.configurationFile=/app/logback.xml"
USER 10001
EXPOSE 8080
CMD ["java","-jar","/app/app.jar"]
