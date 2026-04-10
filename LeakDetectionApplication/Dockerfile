
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy dependency descriptors first for better layer caching
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Ensure wrapper is executable (critical on Alpine)
RUN chmod +x mvnw

# Pre-download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -q

# Copy source and build
COPY src/ src/
RUN ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/LeakDetectionApplication-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# -Dspring.profiles.active MUST come before -jar (JVM flag, not app arg)
# MaxRAMPercentage keeps heap inside Render's 512MB container limit
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]