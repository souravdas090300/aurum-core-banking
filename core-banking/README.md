# Aurum Core Banking System

Enterprise-grade core banking application built with Spring Boot 3.5, featuring hexagonal architecture, OAuth2 security, Drools rules engine, and comprehensive compliance features.

## 🏗️ Architecture

**Hexagonal (Clean) Architecture:**
- `application/` - Application services and use cases
- `domain/` - Domain models, business rules, and port interfaces
- `infrastructure/` - External integrations (database, security, rules engine)
- `interfaces/` - REST API controllers and DTOs

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Keycloak 26.6+ (for authentication)
- PostgreSQL 15+ (for production) or H2 (for development)

### Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd core-banking
   ```

2. **Configure environment variables**
   ```bash
   cp .env.example .env
   # Edit .env and add your secrets
   ```

3. **Run in development mode** (uses H2 database)
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. **Run in production mode**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
   ```

### Starting Keycloak

```bash
cd /path/to/keycloak/bin
./kc.bat start-dev --http-port=8180  # Windows
./kc.sh start-dev --http-port=8180   # Linux/Mac
```

**Keycloak Setup:**
1. Create realm: `banking`
2. Create client: `banking-app` (public, standard flow enabled)
3. Create test user with password

## 🔒 Security Configuration

### Environment Variables (Required for Production)

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_PASSWORD` | PostgreSQL database password | `your_secure_password` |
| `AES_SECRET_KEY` | 32-byte encryption key (hex) | `0123...abcd` (64 chars) |
| `KEYCLOAK_JWK_URI` | Keycloak JWT verification endpoint | `https://auth.example.com/realms/banking/...` |
| `KEYCLOAK_ISSUER_URI` | Keycloak issuer URI | `https://auth.example.com/realms/banking` |

### Security Profiles

- **dev** - Disabled authentication, all requests permitted (localhost only!)
- **prod** - Full OAuth2/JWT authentication via Keycloak

## 📊 Key Features

- ✅ **Account Management** - CRUD operations with IBAN support
- ✅ **Transfers** - Domestic and international transfers with rate limiting
- ✅ **Fraud Detection** - Real-time rule-based screening (Drools)
- ✅ **AML Compliance** - Sanctions list checking and FIAU reporting
- ✅ **Loan Processing** - Credit scoring and workflow automation
- ✅ **Audit Logging** - Complete audit trail for compliance
- ✅ **Strong Customer Authentication (SCA)** - Multi-factor auth support
- ✅ **Data Encryption** - At-rest encryption for sensitive data
- ✅ **GDPR Compliance** - Data export and erasure capabilities

## 🔧 Building & Testing

```bash
# Clean build
./mvnw clean install

# Run tests
./mvnw test

# Run with code coverage
./mvnw clean test jacoco:report

# Skip tests
./mvnw clean install -DskipTests
```

## 📦 Project Structure

```
core-banking/
├── src/main/
│   ├── java/com/aurum/core_banking/
│   │   ├── application/service/      # Business logic
│   │   ├── common/                   # Shared utilities
│   │   ├── domain/                   # Domain models & ports
│   │   ├── infrastructure/           # External systems
│   │   └── interfaces/rest/          # REST API
│   └── resources/
│       ├── application.yml           # Dev config
│       ├── application-prod.yml      # Production config
│       ├── db/migration/             # Flyway SQL scripts
│       ├── rules/                    # Drools DRL files
│       └── processes/                # BPMN workflows
└── src/test/                         # Tests (unit, integration, BDD)
```

## 🌐 API Endpoints

| Endpoint | Description | Auth Required |
|----------|-------------|---------------|
| `GET /api/accounts` | List all accounts | ✅ |
| `POST /api/accounts` | Create account | ✅ |
| `POST /api/transfers` | Execute transfer | ✅ |
| `GET /api/transactions` | Transaction history | ✅ |
| `POST /api/loans/apply` | Apply for loan | ✅ |
| `GET /actuator/health` | Health check | ❌ |

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.5.14
- **Java:** 21 LTS
- **Database:** PostgreSQL 15 / H2 (dev)
- **Security:** Spring Security + OAuth2 + Keycloak
- **Rules Engine:** Drools 9.44
- **Workflow:** jBPM (lightweight)
- **Rate Limiting:** Bucket4j 8.10
- **Testing:** JUnit 5, Mockito, Cucumber BDD, Testcontainers
- **Monitoring:** Spring Actuator, Prometheus, Zipkin

## ⚠️ Security Best Practices

### ❌ NEVER commit to git:
- `.env` files with real credentials
- Private keys (*.key, *.pem, *.p12)
- Keystores (*.jks, *.keystore)
- Log files with sensitive data
- Database dumps

### ✅ ALWAYS:
- Use environment variables for secrets
- Rotate encryption keys regularly
- Review `.gitignore` before committing
- Use HTTPS in production
- Enable SSL for database connections
- Keep dependencies updated

## 📝 License

[Your License Here]

## 👥 Contributors

[Your Team Information Here]
