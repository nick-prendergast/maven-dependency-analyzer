# Use official OpenJDK 21 runtime as base image
FROM openjdk:21-jdk-slim

# Set working directory in container
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better Docker layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Set the entrypoint to run our application
ENTRYPOINT ["java", "-jar", "target/maven-dependency-analyzer-0.0.1-SNAPSHOT.jar"]

# Default command (can be overridden)
CMD ["-d", "/project", "-o", "/output/dependencies.json"]