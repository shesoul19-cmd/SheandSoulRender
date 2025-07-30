# ---- Stage 1: Build the application ----
# Use a Maven image that includes the JDK to build the project
FROM maven:3.8.5-openjdk-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project into the container
COPY . .

# Run the Maven command to build the project and create the JAR file
# This creates the /app/target/your-app-name.jar
RUN mvn clean package -DskipTests

# ---- Stage 2: Create the final, lightweight image ----
# Use a slim Java image because we only need to run the app, not build it
FROM openjdk:17-jdk-slim

# Copy ONLY the built JAR file from the 'builder' stage into the final image
COPY --from=builder /app/target/*.jar app.jar

# Set the command to run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]