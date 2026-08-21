# pathshala-meter-stub

Throwaway Spring Boot stub backend for Flutter background-tracking testing.  
**Do not invest in this service** — it gets thrown away once real backend endpoints are built.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+ (or use the included `mvnw` wrapper)
- PostgreSQL (or an Aiven Postgres instance)

### Environment Variables

| Variable | Description | Default |
|---|---|---|
| `DATABASE_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/pathshala_stub` |
| `DATABASE_USERNAME` | DB username | `postgres` |
| `DATABASE_PASSWORD` | DB password | `postgres` |
| `JWT_SECRET` | HMAC secret (≥32 chars) | embedded fallback |
| `SERVER_PORT` | HTTP port | `8080` |

Copy `.env.example` to `.env` and fill in your Aiven credentials.

### Run

```bash
mvn spring-boot:run
```

## API Reference

### 1. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone_number": "9999999999", "password": "test1234"}'
```

**Response:**
```json
{ "token": "eyJhbG..." }
```

Any other credentials → `401 { "error": "Invalid credentials" }`

### 2. Get Tracking Window

```bash
# Default window (09:00–17:00, 15min interval)
curl http://localhost:8080/api/tracking-window \
  -H "Authorization: Bearer <token>"

# Override for testing (10-minute window, 1-minute interval)
curl "http://localhost:8080/api/tracking-window?start=09:35&end=09:45&interval=1" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{ "start_time": "09:35", "end_time": "09:45", "interval_minutes": 1 }
```

### 3. Submit Location Batch

```bash
curl -X POST http://localhost:8080/api/locations/batch \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '[{"lat": 28.6139, "lng": 77.2090, "captured_at": "2026-08-21T10:00:00Z"}]'
```

**Response:**
```json
{ "received": 1 }
```

### 4. Retrieve Stored Locations

```bash
curl http://localhost:8080/api/locations \
  -H "Authorization: Bearer <token>"
```

Returns all stored points for the authenticated user, ordered by `captured_at`.

## Test Credentials

| Field | Value |
|---|---|
| phone_number | `9999999999` |
| password | `test1234` |
| user_id (in JWT) | `user-001` |
