# Stage 1: Build the application
# Use a Gradle image that includes JDK 24 to match the project's Java version
FROM gradle:9.0.0-jdk24 AS build

# Set the working directory
WORKDIR /home/gradle/src

# Copy the entire project
COPY . .

# Grant execute permission to the Gradle wrapper
RUN chmod +x ./gradlew

# Build the application and create the executable JAR
# Disable the test report verification for Docker builds to avoid potential CI/CD complexities
RUN ./gradlew bootJar -x jacocoTestCoverageVerification

# Stage 2: Create the final, lean image
# Use a minimal JRE image for the runtime
FROM eclipse-temurin:24-jre

# Create a dedicated user and group for the application
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Set the working directory
WORKDIR /app

# Copy the executable JAR from the build stage
# The JAR is located in build/libs/ and its name is based on the project settings
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8095

# Set the entrypoint for the container
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
