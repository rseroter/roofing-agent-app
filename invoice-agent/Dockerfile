# ====================================================================================
# Stage 1: Build the application using Maven and JDK 21
# ====================================================================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the application
COPY src ./src
RUN mvn package -DskipTests

# ====================================================================================
# Stage 2: Create the final, lightweight runtime image using JRE 21
# We use 'jammy' (Ubuntu 22.04) as it's a modern LTS release.
# ====================================================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the executable JAR from the 'builder' stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]