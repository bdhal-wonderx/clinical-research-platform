# RWE Platform — Doctor Onboarding

Spring Boot service for **doctor onboarding only**: mobile OTP authentication, medical profile, hospital details, MOU signing, and study assignment.

## Onboarding Flow

```
Mobile OTP → Verify OTP (JWT) → Register → Profile → MOU → Study Assignment → ACTIVE
```

### Status Flow

| Status | Description |
|--------|-------------|
| `PENDING_OTP` | Mobile number captured, awaiting OTP |
| `OTP_VERIFIED` | OTP verified, JWT issued |
| `PROFILE_COMPLETED` | Medical registration + hospital + specialization saved |
| `MOU_SIGNED` | Agreement signed |
| `ACTIVE` | Fully onboarded and assigned to a study |

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring Data JPA + PostgreSQL 17 + Flyway
- Redis (OTP storage)
- JWT authentication
- MapStruct + Lombok
- springdoc-openapi (Swagger UI)

## Quick Start

```bash
# Start PostgreSQL + Redis
docker compose up -d

# Run application
./mvnw spring-boot:run
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## API Endpoints

### Authentication (public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/otp/send` | Send OTP to mobile |
| POST | `/api/v1/auth/otp/verify` | Verify OTP, get JWT |

### Doctor Onboarding (JWT required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/doctors/register` | Register name & email |
| PUT | `/api/v1/doctors/{id}/profile` | Medical registration, hospital, specialization |
| POST | `/api/v1/doctors/{id}/mou` | Sign MOU agreement |
| POST | `/api/v1/doctors/{id}/studies` | Assign to study |
| GET | `/api/v1/doctors/me` | Current doctor profile |
| GET | `/api/v1/doctors/{id}` | Doctor details |
| GET | `/api/v1/doctors` | List all doctors |

### Study (for assignment)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/studies` | Create study |
| GET | `/api/v1/studies/active` | List active studies |
| GET | `/api/v1/studies/{id}` | Get study |
| PATCH | `/api/v1/studies/{id}/activate` | Activate study |

## Database Tables

| Table | Purpose |
|-------|---------|
| `doctor` | Core doctor record and onboarding status |
| `doctor_profile` | Medical registration, hospital, specialization |
| `doctor_mou` | MOU agreement |
| `doctor_study` | Study assignment |
| `study` | Study configuration |
| `study_protocol` | Protocol version per study |
| `study_protocol_rule` | Eligibility rules per protocol |
| `audit_log` | Onboarding audit trail |

A seed study `RWE-HF-2026` is pre-loaded for testing study assignment.

## Example

```bash
# 1. Send OTP (check logs for OTP in dev mode)
curl -X POST http://localhost:8080/api/v1/auth/otp/send \
  -H "Content-Type: application/json" \
  -d '{"mobileNumber": "9876543210"}'

# 2. Verify OTP
curl -X POST http://localhost:8080/api/v1/auth/otp/verify \
  -H "Content-Type: application/json" \
  -d '{"mobileNumber": "9876543210", "otp": "123456"}'

# 3–6. Register → Profile → MOU → Study assignment (see previous README curl examples)
```

## Project Structure

```
src/main/java/com/wonderx/rwe/
├── config/       → Redis, JWT, OpenAPI
├── controller/   → Auth, Doctor, Study APIs
├── entity/       → Doctor, Profile, MOU, Study entities
├── service/      → OTP, Doctor, Study, Audit
├── security/     → JWT authentication
├── repository/   → Spring Data JPA
└── mapper/       → MapStruct DTO mapping
```
