FROM eclipse-temurin:17.0.8.1_1-jdk-jammy
WORKDIR /certificate-generator
COPY . .
RUN ./mvnw clean install -DskipTests
ENTRYPOINT ["java", "-jar", "target/certificate-generator-0.0.1-SNAPSHOT.jar"]
EXPOSE 8080
