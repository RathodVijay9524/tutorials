# 🚀 Quick Start Implementation Guide

This guide provides ready-to-use code snippets and configurations to start implementing industry standards immediately.

---

## 1. 🔐 Security Configuration Files

### A. Environment Variables Template

Create `.env.example`:
```bash
# JWT Configuration
JWT_SECRET=your-base64-encoded-secret-min-256-bits
JWT_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000

# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=user_master
DB_USERNAME=root
DB_PASSWORD=your_secure_password

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Application Configuration
APP_ENV=development
APP_PORT=9091

# Judge0 Configuration
JUDGE0_API_URL=http://localhost:2358
JUDGE0_API_KEY=your_api_key
```

### B. Updated application.properties

```properties
# Application
spring.application.name=User-Master
server.port=${APP_PORT:9091}

# Database - Use environment variables
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:user-master}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}

# JPA/Hibernate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=${HIBERNATE_DDL_AUTO:update}
spring.jpa.show-sql=${SHOW_SQL:false}
spring.jpa.properties.hibernate.format_sql=${FORMAT_SQL:false}

# Connection Pooling
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.leak-detection-threshold=60000

# JWT - Use environment variables
app.jwt-secret=${JWT_SECRET}
app-jwt-expiration-milliseconds=${JWT_EXPIRATION_MS:900000}
app-jwt-refresh-expiration-milliseconds=${JWT_REFRESH_EXPIRATION_MS:604800000}

# File Upload
user.profile.image.path=images/users/
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Email - Use environment variables
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Logging - Environment specific
logging.level.root=${LOG_LEVEL:INFO}
logging.level.com.vijay.User_Master=${APP_LOG_LEVEL:DEBUG}
logging.level.org.springframework.security=${SECURITY_LOG_LEVEL:WARN}
logging.level.org.hibernate.SQL=${SQL_LOG_LEVEL:WARN}

# Actuator
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true

# Judge0
judge0.api.url=${JUDGE0_API_URL:http://localhost:2358}
judge0.api.key=${JUDGE0_API_KEY:}
judge0.api.host=${JUDGE0_API_HOST:}

# Server
server.forward-headers-strategy=framework
server.tomcat.connection-timeout=20000
server.tomcat.keep-alive-timeout=20000
```

### C. Security Headers Configuration

Create `src/main/java/com/vijay/User_Master/config/SecurityHeadersConfig.java`:

```java
package com.vijay.User_Master.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Configuration
public class SecurityHeadersConfig {

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, 
                           FilterChain chain) throws IOException, ServletException {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // Security Headers
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
            httpResponse.setHeader("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains");
            httpResponse.setHeader("Content-Security-Policy", 
                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            httpResponse.setHeader("Permissions-Policy", 
                "geolocation=(), microphone=(), camera=()");

            // Remove server header
            httpResponse.setHeader("Server", "");

            chain.doFilter(request, response);
        }
    }
}
```

---

## 2. 🧪 Testing Setup

### A. Test Dependencies (build.gradle)

Add to `dependencies` block:
```gradle
// Testing
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.security:spring-security-test'
testImplementation 'org.testcontainers:mysql:1.19.0'
testImplementation 'org.testcontainers:junit-jupiter:1.19.0'
testImplementation 'org.mockito:mockito-inline:5.2.0'
testImplementation 'com.h2database:h2'
testImplementation 'org.jacoco:org.jacoco.agent:0.8.11'

// Test coverage
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.11"
}

test {
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}
```

### B. Sample Unit Test

Create `src/test/java/com/vijay/User_Master/service/UserServiceTest.java`:

```java
package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.UserRequest;
import com.vijay.User_Master.dto.UserResponse;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.exceptions.UserAlreadyExistsException;
import com.vijay.User_Master.repository.UserRepository;
import com.vijay.User_Master.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest userRequest;
    private User user;

    @BeforeEach
    void setUp() {
        userRequest = UserRequest.builder()
                .name("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .name("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
    }

    @Test
    void createUser_Success() {
        // Given
        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse response = userService.create(userRequest).join();

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void createUser_UserAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(Optional.of(user));

        // When & Then
        assertThrows(UserAlreadyExistsException.class, 
            () -> userService.create(userRequest).join());
        verify(userRepository, never()).save(any(User.class));
    }
}
```

### C. Test Configuration

Create `src/test/resources/application-test.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

app.jwt-secret=test-secret-key-for-testing-purposes-only-min-256-bits-required
app-jwt-expiration-milliseconds=900000

logging.level.root=WARN
logging.level.com.vijay.User_Master=DEBUG
```

---

## 3. 🐳 Docker Configuration

### A. Dockerfile

Create `Dockerfile`:

```dockerfile
# Multi-stage build for optimized image size
FROM gradle:8.5-jdk21-alpine AS build

WORKDIR /app

# Copy dependency files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY . .

# Build application
RUN gradle build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 9091

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9091/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

### B. .dockerignore

Create `.dockerignore`:

```
.gradle
build
.idea
*.iml
*.log
.env
.env.*
.git
.gitignore
README.md
*.md
k8s/
judge0-setup/
```

### C. docker-compose.yml

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: tutoreals-app
    ports:
      - "9091:9091"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=user_master
      - DB_USERNAME=${DB_USERNAME:-root}
      - DB_PASSWORD=${DB_PASSWORD:-root}
      - JWT_SECRET=${JWT_SECRET}
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_started
    networks:
      - tutoreals-network
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    container_name: tutoreals-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD:-root}
      MYSQL_DATABASE: user_master
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - tutoreals-network
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: tutoreals-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - tutoreals-network
    restart: unless-stopped

  prometheus:
    image: prom/prometheus:latest
    container_name: tutoreals-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
    networks:
      - tutoreals-network
    restart: unless-stopped

  grafana:
    image: grafana/grafana:latest
    container_name: tutoreals-grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD:-admin}
    volumes:
      - grafana_data:/var/lib/grafana
    depends_on:
      - prometheus
    networks:
      - tutoreals-network
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:
  prometheus_data:
  grafana_data:

networks:
  tutoreals-network:
    driver: bridge
```

---

## 4. 🔄 CI/CD Pipeline

### A. GitHub Actions Workflow

Create `.github/workflows/ci-cd.yml`:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

env:
  JAVA_VERSION: '21'
  GRADLE_VERSION: '8.5'

jobs:
  test:
    name: Run Tests
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: testpassword
          MYSQL_DATABASE: user_master_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping -h localhost"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Cache Gradle dependencies
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run tests
        env:
          DB_HOST: localhost
          DB_PORT: 3306
          DB_NAME: user_master_test
          DB_USERNAME: root
          DB_PASSWORD: testpassword
        run: ./gradlew test

      - name: Generate test report
        if: always()
        uses: gradle/gradle-build-action@v2
        with:
          arguments: jacocoTestReport

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: build/test-results/test/

      - name: Upload coverage reports
        if: always()
        uses: codecov/codecov-action@v3
        with:
          files: build/reports/jacoco/test/jacocoTestReport.xml
          flags: unittests
          name: codecov-umbrella

  build:
    name: Build Application
    runs-on: ubuntu-latest
    needs: test
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Build with Gradle
        run: ./gradlew build -x test

      - name: Build Docker image
        run: docker build -t tutoreals-app:${{ github.sha }} .

      - name: Upload Docker image
        if: github.event_name == 'push' && github.ref == 'refs/heads/main'
        run: |
          echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
          docker tag tutoreals-app:${{ github.sha }} tutoreals-app:latest
          docker push tutoreals-app:${{ github.sha }}
          docker push tutoreals-app:latest

  security-scan:
    name: Security Scan
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'sarif'
          output: 'trivy-results.sarif'

      - name: Upload Trivy results to GitHub Security
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'
```

---

## 5. 📊 Monitoring Configuration

### A. Prometheus Configuration

Create `prometheus.yml`:

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'tutoreals-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:9091']
        labels:
          application: 'tutoreals-app'
          environment: 'production'
```

### B. Actuator Dependencies

Already in build.gradle, but ensure:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
```

---

## 6. ✅ Quick Implementation Checklist

### Week 1: Security & Configuration
- [ ] Create `.env.example` file
- [ ] Update `application.properties` to use environment variables
- [ ] Implement `SecurityHeadersConfig`
- [ ] Remove hardcoded secrets from code
- [ ] Set up environment-specific profiles

### Week 2: Testing
- [ ] Add test dependencies to `build.gradle`
- [ ] Create `application-test.properties`
- [ ] Write unit tests for services (target: 80% coverage)
- [ ] Write integration tests for controllers
- [ ] Set up test coverage reporting

### Week 3: Docker & CI/CD
- [ ] Create optimized `Dockerfile`
- [ ] Create `docker-compose.yml`
- [ ] Set up GitHub Actions workflow
- [ ] Configure Docker registry
- [ ] Test Docker build locally

### Week 4: Monitoring
- [ ] Add Actuator endpoints
- [ ] Configure Prometheus
- [ ] Set up Grafana dashboards
- [ ] Implement structured logging
- [ ] Add health checks

---

## 🎯 Next Steps

1. **Start with Security** - This is the highest priority
2. **Add Tests** - Build confidence in changes
3. **Containerize** - Enable consistent deployments
4. **Monitor** - Understand application behavior
5. **Iterate** - Continuous improvement

---

**Remember:** Implement these changes incrementally and test thoroughly at each step!

