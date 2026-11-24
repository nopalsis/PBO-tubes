# Stage 1: Build JAR dengan Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Salin pom.xml dan unduh dependensi
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Salin source code & static web assets
COPY src ./src

# Build single executable fat JAR
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime Environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Ambil fat jar hasil build
COPY --from=builder /app/target/smarthome.jar app.jar

# Render akan otomatis menginjeksi environment variable $PORT
ENV PORT=8080
EXPOSE 8080

# Jalankan server
CMD ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]
