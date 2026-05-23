# Aurum Core Banking

A modern, enterprise-grade core banking platform built with Spring Boot, implementing hexagonal architecture principles with advanced features including fraud detection, AML screening, credit scoring, and workflow automation.

## 🏗️ Architecture

This project follows **Hexagonal (Ports & Adapters) Architecture** for clean separation of concerns:

```
├── domain/              # Business logic & domain models (pure Java)
│   ├── model/          # Domain entities
│   └── port/           # Interfaces for application services
│       └── in/         # Inbound ports (use cases)
├── application/         # Application services (use case implementations)
│   ├── service/        # Business services
│   └── usecase/        # Use case orchestrators
├── infrastructure/      # Technical implementations
│   ├── persistence/    # JPA repositories, entities, mappers
│   ├── security/       # Security, OAuth2, rate limiting
│   ├── rules/          # Drools rules engine integration
│   └── workflow/       # jBPM/Kogito workflow integration
├── interfaces/          # External interfaces (REST controllers)
│   └── rest/           # REST API controllers & DTOs
└── common/              # Shared utilities
    ├── audit/          # Audit logging
    └── exception/      # Custom exceptions
```

## 🚀 Technology Stack

### Core Framework
- **Java 21** - Modern LTS Java version
- **Spring Boot 4.0.5** - Application framework
- **Maven** - Build & dependency management

### Persistence
- **PostgreSQL 15** - Production database
- **H2** - In-memory database for dev/test
- **Spring Data JPA** - ORM & repository layer
- **Flyway** - Database migrations
- **HikariCP** - High-performance connection pool

### Security
- **Keycloak 26.6.2** - Identity & access management
- **Spring Security OAuth2** - JWT-based authentication
- **AES-256-GCM** - Encryption for sensitive data (e.g., national IDs)
- **Bouncy Castle** - Cryptographic provider

### Business Rules & Workflows
- **Drools 9.44.0.Final** - Business rules engine for:
  - Fraud detection
  - AML/PEP/sanctions screening
  - Credit scoring
- **jBPM/Kogito** - Business process automation (loan approvals)

### API & Rate Limiting
- **Spring Web** - REST API framework
- **Bucket4j 8.10.1** - Token-bucket rate limiting
- **Caffeine Cache** - High-performance in-memory cache

### Testing
- **JUnit 5** - Unit testing framework
- **Testcontainers** - Integration testing with real PostgreSQL
- **REST Assured** - API contract testing
- **Cucumber** - BDD scenarios
- **AssertJ** - Fluent assertions

### Observability
- **Spring Boot Actuator** - Health checks & metrics
- **SLF4J + Logback** - Structured logging
- **Micrometer** - Metrics collection

## 📋 Prerequisites

- **Java 21** - [Download OpenJDK 21](https://adoptium.net/)
- **PostgreSQL 15+** - [Download PostgreSQL](https://www.postgresql.org/download/)
- **Keycloak 26.6.2** - [Download Keycloak](https://www.keycloak.org/downloads)
- **Maven 3.9+** - Build tool (wrapper included: `mvnw`)
- **Docker** (optional) - For running Testcontainers-based integration tests

## ⚙️ Configuration

### 1. Database Setup

Create the PostgreSQL database and user:

```sql
CREATE DATABASE banking_db;
CREATE USER banking_user WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE banking_db TO banking_user;
```

Update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/banking_db
    username: banking_user
    password: your_secure_password
```

Flyway migrations will automatically initialize the schema on startup.

### 2. Keycloak Setup

**Start Keycloak:**

```powershell
cd path\to\keycloak-26.6.2\bin
.\kc.bat start-dev --http-port=8180
```

**Configure Realm:**

1. Access Keycloak Admin Console: http://localhost:8180
2. Create realm: `banking`
3. Create client: `banking-api`
   - Client authentication: **ON**
   - Valid redirect URIs: `http://localhost:8080/*`
   - Web origins: `http://localhost:8080`
4. Create roles: `BANKING_USER`, `LOAN_OFFICER`, `COMPLIANCE_OFFICER`
5. Create test users and assign roles

**Update application.yml:**

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8180/realms/banking/protocol/openid-connect/certs
```

### 3. Encryption Key Configuration

Generate a 256-bit AES key (64 hex characters):

```powershell
# PowerShell
-join ((1..64) | ForEach-Object { '{0:X}' -f (Get-Random -Max 16) })
```

Set environment variable:

```powershell
$env:ENCRYPTION_KEY="your_64_character_hex_key_here"
```

Or update `application.yml`:

```yaml
encryption:
  key: ${ENCRYPTION_KEY}
```

### 4. Rate Limiting Configuration

Default configuration (adjust as needed):

```yaml
rate-limit:
  capacity: 100              # Maximum tokens in bucket
  refill-tokens: 100         # Tokens to add per refill
  refill-seconds: 60         # Refill interval (1 minute)
```

## 🔧 Build & Run

### Development Mode

```powershell
# Using Maven wrapper
.\mvnw clean install
.\mvnw spring-boot:run

# Or with explicit profile
.\mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on **http://localhost:8080**

### Production Mode

```powershell
# Build JAR
.\mvnw clean package -DskipTests

# Run with production profile
java -jar target/aurum-core-banking-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

**Production Environment Variables:**

```powershell
$env:DB_PASSWORD="production_db_password"
$env:ENCRYPTION_KEY="production_encryption_key"
$env:SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI="https://keycloak.prod.example.com/realms/banking/protocol/openid-connect/certs"
```

## 🧪 Testing

### Run All Tests

```powershell
.\mvnw test
```

### Run Specific Test Suites

```powershell
# Unit tests only
.\mvnw test -Dtest=*Test

# Integration tests (requires Docker for Testcontainers)
.\mvnw test -Dtest=*IntegrationTest

# BDD scenarios
.\mvnw test -Dtest=*Steps
```

### Test Coverage

```powershell
.\mvnw verify jacoco:report
# Report: target/site/jacoco/index.html
```

## 🔑 Key Features

### 1. Account Management
- Create/update accounts with multiple currencies
- Account status management (ACTIVE, SUSPENDED, CLOSED)
- Balance tracking with pessimistic locking for concurrency

### 2. Transaction Processing
- **Deposits** - Credit account balance
- **Withdrawals** - Debit with balance validation
- **Transfers** - Atomic dual-entry bookkeeping
- Idempotency keys to prevent duplicate transactions

### 3. Fraud Detection (Drools Rules)
- Real-time transaction analysis
- Pattern detection (velocity, amount thresholds)
- PEP/sanctions list screening
- Configurable rules in `src/main/resources/rules/`

### 4. AML Screening
- Customer risk profiling
- Transaction monitoring
- Sanctions list checking
- Compliance reporting

### 5. Credit Scoring
- Automated credit decision engine
- Configurable scoring rules
- Integration with loan approval workflow

### 6. Loan Processing (jBPM)
- Automated loan approval workflow
- Human task assignments for review
- Business process monitoring
- Process definition: `src/main/resources/processes/loan-approval.bpmn2`

### 7. Security Features
- JWT-based authentication via Keycloak
- Role-based access control (RBAC)
- AES-256-GCM field-level encryption
- Rate limiting (100 requests/min per IP)

### 8. Audit Trail
- Comprehensive audit logging
- All operations tracked with user context
- Tamper-evident audit records

## 📡 API Documentation

### Authentication

All API endpoints require a valid JWT token from Keycloak:

```bash
# Get token from Keycloak
curl -X POST http://localhost:8180/realms/banking/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=banking-api" \
  -d "client_secret=<your_client_secret>" \
  -d "grant_type=password" \
  -d "username=testuser" \
  -d "password=testpass"

# Use token in API calls
curl -X GET http://localhost:8080/api/v1/accounts/123 \
  -H "Authorization: Bearer <access_token>"
```

### Core Endpoints

#### Transactions
- `POST /api/v1/transactions/deposit` - Deposit funds
- `POST /api/v1/transactions/withdraw` - Withdraw funds
- `GET /api/v1/transactions/account/{accountId}` - Get account transactions
- `GET /api/v1/transactions/{transactionId}` - Get transaction details

#### Transfers
- `POST /api/v1/transfers` - Execute money transfer
- `GET /api/v1/transfers/page` - Paginated transfer history

#### Loans
- `POST /api/v1/loans/apply` - Submit loan application
- `GET /api/v1/loans/{loanId}` - Get loan details
- `POST /api/v1/loans/{loanId}/approve` - Approve loan (LOAN_OFFICER role)
- `GET /api/v1/loans/tasks` - Get pending approval tasks

#### Health & Monitoring
- `GET /actuator/health` - Application health status
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/info` - Application information

### Example Request Bodies

**Deposit Request:**
```json
{
  "accountId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 1000.00,
  "currency": "EUR",
  "reference": "Salary deposit",
  "idempotencyKey": "deposit-2026-05-23-001"
}
```

**Transfer Request:**
```json
{
  "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
  "toAccountId": "987fcdeb-51a2-43e7-b789-123456789abc",
  "amount": 500.00,
  "currency": "EUR",
  "reference": "Payment for invoice #12345",
  "idempotencyKey": "transfer-2026-05-23-001"
}
```

## 📁 Project Structure

```
aurum-core-banking/
├── src/
│   ├── main/
│   │   ├── java/com/aurum/corebanking/
│   │   │   ├── CoreBankingApplication.java    # Main application entry point
│   │   │   ├── application/                    # Use case implementations
│   │   │   │   ├── service/                    # Business services
│   │   │   │   │   ├── AccountService.java
│   │   │   │   │   ├── TransferService.java
│   │   │   │   │   ├── FraudDetectionService.java
│   │   │   │   │   ├── CreditScoringService.java
│   │   │   │   │   └── LoanProcessService.java
│   │   │   │   └── usecase/                    # Use case orchestrators
│   │   │   ├── common/
│   │   │   │   ├── audit/                      # Audit logging
│   │   │   │   └── exception/                  # Custom exceptions
│   │   │   ├── domain/                         # Domain models & business logic
│   │   │   │   ├── model/                      # Domain entities
│   │   │   │   ├── port/in/                    # Use case interfaces
│   │   │   │   └── rules/                      # Drools fact objects
│   │   │   ├── infrastructure/
│   │   │   │   ├── persistence/
│   │   │   │   │   ├── entity/                 # JPA entities
│   │   │   │   │   ├── mapper/                 # Entity-Domain mappers
│   │   │   │   │   └── repository/             # JPA repositories
│   │   │   │   ├── rules/                      # Drools configuration
│   │   │   │   ├── security/                   # Security, OAuth2, rate limiting
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── RateLimitingInterceptor.java
│   │   │   │   │   └── AesEncryptionConverter.java
│   │   │   │   └── workflow/                   # jBPM/Kogito integration
│   │   │   └── interfaces/
│   │   │       └── rest/                       # REST API layer
│   │   │           ├── controller/
│   │   │           ├── dto/
│   │   │           │   ├── request/
│   │   │           │   └── response/
│   │   │           ├── mapper/                 # DTO-Domain mappers
│   │   │           └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml                 # Main configuration
│   │       ├── application-prod.yml            # Production overrides
│   │       ├── logback-spring.xml              # Logging configuration
│   │       ├── db/migration/                   # Flyway SQL migrations
│   │       │   ├── V1__init_schema.sql
│   │       │   └── V2__audit_log.sql
│   │       ├── processes/                      # BPMN workflow definitions
│   │       │   └── loan-approval.bpmn2
│   │       └── rules/                          # Drools rules files
│   │           ├── fraud-detection.drl
│   │           ├── aml-screening.drl
│   │           └── credit-scoring.drl
│   └── test/
│       ├── java/com/aurum/corebanking/
│       │   ├── BaseIntegrationTest.java        # Base class for integration tests
│       │   ├── api/                            # REST Assured API tests
│       │   ├── application/                    # Service unit tests
│       │   ├── bdd/steps/                      # Cucumber step definitions
│       │   ├── integration/                    # Integration tests
│       │   ├── rules/                          # Drools rules tests
│       │   └── security/                       # Security tests
│       └── resources/
│           ├── application-test.yml            # Test configuration
│           └── features/                       # Cucumber BDD scenarios
│               ├── fraud.feature
│               ├── loan.feature
│               └── transfer.feature
├── pom.xml                                     # Maven configuration
├── HELP.md                                     # Detailed technical documentation
└── README.md                                   # This file
```

## 🔒 Security Best Practices

### Production Deployment

1. **Never commit secrets** - Use environment variables for:
   - Database passwords
   - Encryption keys
   - Keycloak credentials

2. **Enable HTTPS** - Configure SSL/TLS certificates in production

3. **Firewall Rules** - Restrict access to:
   - Database port (5432)
   - Management port (8081)
   - Keycloak admin console

4. **Regular Updates** - Keep dependencies updated for security patches

5. **Audit Logs** - Monitor and archive audit logs regularly

6. **Rate Limiting** - Adjust limits based on legitimate traffic patterns

## 📊 Performance Tuning

### Database Connection Pool (HikariCP)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # Adjust based on load
      minimum-idle: 5
      connection-timeout: 30000
      max-lifetime: 1800000
```

### JVM Options (Production)

```powershell
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar aurum-core-banking-1.0.0-SNAPSHOT.jar
```

## 🐛 Troubleshooting

### Common Issues

**Issue: Application fails to start - "Cannot create PoolableConnectionFactory"**
- **Solution:** Check PostgreSQL is running and credentials are correct

**Issue: "Invalid JWT token"**
- **Solution:** Verify Keycloak is running on port 8180 and realm/client are configured

**Issue: Tests fail with "No container runtime found"**
- **Solution:** Install Docker for Testcontainers-based integration tests

**Issue: Rate limit errors (429 Too Many Requests)**
- **Solution:** Increase rate limit capacity or implement token refresh logic

## 📞 Support & Contribution

### Reporting Issues
For bugs or feature requests, create an issue with:
- Environment details (Java version, OS)
- Steps to reproduce
- Error logs/stack traces

### Development Guidelines
- Follow hexagonal architecture principles
- Write unit tests for all business logic
- Add integration tests for new endpoints
- Update documentation for API changes

## 📝 License

Copyright © 2026 Aurum Banking. All rights reserved.

---

**Built with ❤️ using Spring Boot, Java 21, and Clean Architecture principles**
