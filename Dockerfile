FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline || echo "dependency warm-up skipped"
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN apk add --no-cache tzdata wget \
    && addgroup -S taskflow \
    && adduser -S -G taskflow taskflow
COPY --from=build /build/target/*.jar /app/app.jar
USER taskflow
EXPOSE 3000
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]