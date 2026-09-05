FROM eclipse-temurin:25-jre-alpine AS extractor
WORKDIR /app
COPY service-identity/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=extractor --chown=app:app /app/extracted/dependencies/ ./
COPY --from=extractor --chown=app:app /app/extracted/spring-boot-loader/ ./
COPY --from=extractor --chown=app:app /app/extracted/snapshot-dependencies/ ./
COPY --from=extractor --chown=app:app /app/extracted/application/ ./
USER app
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]