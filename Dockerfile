# Sử dụng image OpenJDK chính thức làm base image
FROM openjdk:17-jdk-slim AS builder

# Thiết lập thư mục làm việc
WORKDIR /app

# Cài đặt Maven
RUN apt-get update && apt-get install -y maven

# Copy file pom.xml và tải dependency trước để tận dụng cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ source code và build ứng dụng
COPY src ./src
RUN mvn clean package -DskipTests

# Image chạy ứng dụng
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy file JAR từ bước build
COPY --from=builder /app/target/*.jar app.jar

# Thiết lập biến môi trường (nếu cần)
ENV SPRING_PROFILES_ACTIVE=prod

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]