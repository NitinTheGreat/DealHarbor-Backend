# DealHarbor Backend

An intra-university e-commerce platform built with Spring Boot. DealHarbor enables students to buy and sell products within their academic community.

.\mvnw spring-boot:run 

docker run -d --name redis-local -p 6379:6379 redis:latest http://localhost:8080/admin.html

docker exec -it redis-local redis-cli KEYS "*" --> show what's currently in Redis

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [OAuth2 Authentication](#oauth2-authentication)
- [Production Deployment](#production-deployment)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before running this project, ensure you have the following installed:

- **Java 17 or higher** - [Download](https://adoptium.net/temurin/releases/)
- **Docker Desktop** (optional, for local Redis) - [Download](https://www.docker.com/products/docker-desktop/)

You do not need to install Maven; the project includes a Maven wrapper.

### Verify Java Installation

```bash
java -version
```

Expected output: `openjdk version "17.x.x"` or higher.

---

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/DealHarbor-Backend.git
cd DealHarbor-Backend
```

### 2. Configure the Application

The application requires external services. The default `application.properties` is pre-configured with development credentials for:

- **PostgreSQL Database**: Hosted on Supabase
- **Redis Session Store**: Hosted on Upstash

If you need to use your own services, see the [Configuration](#configuration) section.

### 3. Run the Application

On Windows:
```powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:
```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

### 4. Verify the Server is Running

```bash
curl http://localhost:8080/health
```

Expected response:
```json
{"status": "UP"}
```

---

## Configuration

All configuration is in `src/main/resources/application.properties`.

### Database (PostgreSQL)

```properties
spring.datasource.url=jdbc:postgresql://your-host:5432/postgres
spring.datasource.username=your-username
spring.datasource.password=your-password
```

### Redis (Session Storage)

The application uses Redis for session management. You have two options:

**Option A: Use a hosted Redis service (Upstash, Redis Cloud)**

```properties
spring.data.redis.host=your-redis-host.upstash.io
spring.data.redis.port=6379
spring.data.redis.password=your-redis-password
spring.data.redis.ssl.enabled=true
```

**Option B: Run Redis locally with Docker**

```bash
docker run -d --name redis-local -p 6379:6379 redis:latest
```

Then update the properties:
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.ssl.enabled=false
```

### OAuth2 (Google and GitHub Login)

To enable social login, configure OAuth2 credentials:

```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/oauth2/callback/google

# GitHub OAuth2
spring.security.oauth2.client.registration.github.client-id=your-github-client-id
spring.security.oauth2.client.registration.github.client-secret=your-github-client-secret
spring.security.oauth2.client.registration.github.redirect-uri=http://localhost:8080/oauth2/callback/github
```

Ensure the redirect URIs are registered in your OAuth provider's console.

### Email (SMTP)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

For Gmail, use an [App Password](https://support.google.com/accounts/answer/185833), not your regular password.

---

## Running the Application

### Development Mode

```bash
.\mvnw.cmd spring-boot:run
```

The server runs on port 8080 with hot-reload enabled via Spring DevTools.

### Build a JAR

```bash
.\mvnw.cmd clean package -DskipTests
```

The JAR file is created at `target/dealharbor-backend-0.0.1-SNAPSHOT.jar`.

### Run the JAR

```bash
java -jar target/dealharbor-backend-0.0.1-SNAPSHOT.jar
```

### Run with Docker

Build the Docker image:
```bash
docker build -t dealharbor-backend:latest .
```

Run the container:
```bash
docker run -p 8080:8080 dealharbor-backend:latest
```

---

## API Endpoints

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Server health status |
| GET | `/api/test/health` | API health check |

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login with email/password |
| POST | `/api/auth/verify` | Verify OTP after registration |
| GET | `/api/auth/me` | Get current user profile |
| POST | `/api/auth/forgot-password` | Request password reset |
| POST | `/api/auth/reset-password` | Reset password with OTP |
| POST | `/api/auth/logout` | Logout current session |

### OAuth2 (Social Login)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/oauth2/authorization/google` | Initiate Google login |
| GET | `/oauth2/authorization/github` | Initiate GitHub login |

### Products

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}` | Get product by ID |
| POST | `/api/products` | Create product (auth required) |
| PUT | `/api/products/{id}` | Update product (auth required) |
| DELETE | `/api/products/{id}` | Delete product (auth required) |
| POST | `/api/products/search` | Search products with filters |

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | List all categories |
| GET | `/api/categories/main` | List main categories with subcategories |

---

## OAuth2 Authentication

This application uses session-based authentication, not JWT tokens.

### How OAuth2 Login Works

1. Frontend redirects the user to `/oauth2/authorization/google` or `/oauth2/authorization/github`
2. User authenticates with the provider
3. Backend creates a session and redirects to the frontend with `?oauth=success`
4. Frontend calls `/api/auth/me` to verify the session

### Frontend Integration

All API requests must include credentials to send the session cookie:

```javascript
fetch('http://localhost:8080/api/auth/me', {
  method: 'GET',
  credentials: 'include'
})
```

---

## Production Deployment

### Environment Variables

For production, use environment variables instead of hardcoding credentials:

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

spring.data.redis.host=${REDIS_HOST}
spring.data.redis.password=${REDIS_PASSWORD}

jwt.secret=${JWT_SECRET}
```

### AWS App Runner Deployment

1. Push the Docker image to Amazon ECR:
   ```bash
   aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.ap-south-1.amazonaws.com
   docker tag dealharbor-backend:latest <account-id>.dkr.ecr.ap-south-1.amazonaws.com/dealharbor-backend:latest
   docker push <account-id>.dkr.ecr.ap-south-1.amazonaws.com/dealharbor-backend:latest
   ```

2. Create an App Runner service pointing to the ECR image.

3. Configure environment variables in App Runner.

4. Update OAuth redirect URIs to match your production URL.

See `AWS_DEPLOYMENT_GUIDE.md` for detailed instructions.

---

## Troubleshooting

### Port 8080 Already in Use

Find and kill the process using the port:

Windows:
```powershell
netstat -ano | findstr :8080
taskkill /PID <pid> /F
```

macOS/Linux:
```bash
lsof -i :8080
kill -9 <pid>
```

### Redis Connection Failed

Verify Redis is running:
```bash
docker ps | grep redis
```

If not running:
```bash
docker start redis-local
```

Or start a new container:
```bash
docker run -d --name redis-local -p 6379:6379 redis:latest
```

### Database Connection Failed

1. Check your internet connection
2. Verify database credentials in `application.properties`
3. Ensure the database is not paused (Supabase pauses inactive databases)

### OAuth Redirect Mismatch

Ensure the redirect URI in `application.properties` matches exactly what is configured in:
- Google Cloud Console
- GitHub OAuth App settings

Common mistakes:
- Using `http` instead of `https` (or vice versa)
- Missing trailing slash
- Different port numbers

### View Application Logs

The application logs are output to the console. For more detailed logs, set:
```properties
logging.level.com.dealharbor=DEBUG
logging.level.org.springframework.security=DEBUG
```

---

## Project Structure

```
src/main/java/com/dealharbor/dealharbor_backend/
├── config/          # Spring configuration classes
├── controllers/     # REST API controllers
├── dto/             # Data Transfer Objects
├── entities/        # JPA entity classes
├── enums/           # Enumeration types
├── repositories/    # Spring Data JPA repositories
├── security/        # Security configuration and handlers
├── services/        # Business logic layer
└── utils/           # Utility classes
```

---

## License

Open Source - developed by Nitin Kumar Pandey