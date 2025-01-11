# Build stage
FROM maven:3.8.1-openjdk-17-slim AS builder

# Install any necessary utilities (like unzip)
RUN apt-get update && apt-get install -y unzip

# Set the working directory for the build process
WORKDIR /usr/src/app

# Copy the necessary files to the container
COPY ./pom.xml /usr/src/app/
COPY ./src /usr/src/app/src/

# Build the application (skip tests)
RUN mvn clean install -DskipTests

# Runtime stage: Use a smaller base image to reduce size
FROM openjdk:17-slim

# Set the working directory in the runtime image
WORKDIR /usr/app

# Copy the built jar file from the builder stage
COPY --from=builder /usr/src/app/target/shoppin-assignments-1.0-SNAPSHOT.jar /usr/app/shoppin-assignments-1.0-SNAPSHOT.jar

# Expose the port your app will run on (default for Spring Boot is 8080)
EXPOSE 8080

# Set the entrypoint to run the Spring Boot app
ENTRYPOINT ["java", "-jar", "shoppin-assignments-1.0-SNAPSHOT.jar"]
