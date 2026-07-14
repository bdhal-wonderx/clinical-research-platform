# TOLERATE-HF Clinical Research Platform

End-to-end RWE platform for the **TOLERATE-HF** study: investigator onboarding, patient registration, prescription capture, OCR/validation, follow-up reminders, payments, and multi-stakeholder dashboards.

## End-to-End Flow

```
Doctor OTP → Profile → MOU → Study Assignment
    → Patient Register → Consent Photo
    → Baseline Rx Upload → OCR → Validation → Lock
    → Auto Schedule 90-day Follow-up → SMS/Voice Reminders
    → Follow-up Rx Upload → OCR → Validation → Lock
    → Payment Eligibility → Dashboards Updated
```

## Tech Stack

- Java 21, Spring Boot 3.5, PostgreSQL, Flyway, Redis, RabbitMQ
- JWT auth, MapStruct, springdoc-openapi

## Run

```bash
docker compose up -d          # PostgreSQL + Redis + RabbitMQ
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8081/swagger-ui.html`

## API Overview

### Auth & Onboarding
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/otp/send` | Send OTP |
| POST | `/api/v1/auth/otp/verify` | Verify OTP, get JWT |
| POST | `/api/v1/doctors/register` | Register doctor |
| PUT | `/api/v1/doctors/{id}/profile` | Medical profile |
| PUT | `/api/v1/doctors/{id}/payment-profile` | Bank/UPI details |
| POST | `/api/v1/doctors/{id}/mou` | Sign MOU |
| POST | `/api/v1/doctors/{id}/documents` | Sign EC/Protocol/Privacy docs |
| POST | `/api/v1/doctors/{id}/studies` | Assign to study |

### Patient & Prescription
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/patients` | Register patient (max 20/doctor) |
| POST | `/api/v1/patients/{id}/consent` | Upload consent photo |
| POST | `/api/v1/prescriptions/patients/{id}/upload?visitType=BASELINE` | Upload Rx image |
| POST | `/api/v1/prescriptions/{id}/lock` | Lock validated prescription |
| POST | `/api/v1/prescriptions/{id}/validate` | Re-run protocol validation |

### Dashboards
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/dashboard/investigator/me` | Investigator dashboard |
| GET | `/api/v1/dashboard/ops` | Wondrx operations console |
| GET | `/api/v1/dashboard/sponsor/{studyId}` | Sponsor view (de-identified) |
| GET | `/api/v1/dashboard/client/{studyId}` | Client portal |
| POST | `/api/v1/dashboard/support-queries` | Raise support query |

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| PATCH | `/api/v1/payments/{id}/mark-paid` | Mark honorarium paid (ops) |

## Database Migrations

| Migration | Content |
|-----------|---------|
| V1 | Doctor onboarding + study config |
| V2 | Seed study |
| V3 | Full clinical pipeline (patient, Rx, OCR, QC, validation, follow-up, payment) |
| V4 | TOLERATE-HF study rules, payment & reminder config |

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8081 | Application port |
| `OCR_MOCK_ENABLED` | true | Use mock OCR (set false + `OCR_SERVICE_URL` for production) |
| `OCR_SERVICE_URL` | — | External OCR module REST endpoint |
| `UPLOAD_DIR` | uploads | Local file storage for Rx/consent images |
| `FOLLOWUP_CRON` | `0 0 8 * * *` | Daily reminder scheduler (8 AM) |

## Seed Study ID

TOLERATE-HF study: `a0000000-0000-0000-0000-000000000001`

## Project Structure

```
com.wonderx.rwe/
├── controller/    Auth, Doctor, Patient, Prescription, Dashboard, Payment
├── service/       Business logic for all modules
├── validation/    Protocol validation engine
├── ocr/           OCR integration client (mock + REST)
├── scheduler/     Follow-up SMS/voice reminders
├── storage/       Document upload service
├── entity/        25+ JPA entities
└── event/         RabbitMQ platform events
```
