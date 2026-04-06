```markdown
# Finance Dashboard Backend

A backend system for a finance dashboard built with Spring Boot, featuring role-based access control, JWT authentication with RSA RS256, Redis-backed session management, and soft delete support.

---

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.5**
- **MySQL 8** — primary data store
- **Redis** — token blacklisting and session management
- **JWT (RSA RS256)** — stateless authentication
- **Lombok** — boilerplate reduction
- **Docker** — containerized deployment

---

## Project Structure

```
src/main/java/org/example/screening/
├── config/          # Security, Redis, AuthProvider, DataSeeder
├── constant/        # Application-level constants
├── controller/      # REST controllers
├── dto/             # Request/Response DTOs
├── entity/          # JPA entities
├── event/           # Auth event listeners
├── exception/       # Global exception handling
├── filter/          # JWT validation filter, CSRF filter
├── repository/      # JPA + Redis repositories
├── service/         # Service interfaces and implementations
└── util/            # AuthUtil (JWT generation, key management)
```

---

## Roles & Permissions

| Endpoint                          | VIEWER | ANALYST | ADMIN |
|-----------------------------------|--------|---------|-------|
| POST /auth/register               | ✅     | ✅      | ✅    |
| POST /auth/login                  | ✅     | ✅      | ✅    |
| POST /auth/refresh                | ✅     | ✅      | ✅    |
| POST /auth/logout/single          | ✅     | ✅      | ✅    |
| POST /auth/logout/all             | ✅     | ✅      | ✅    |
| GET  /dashboard/summary           | ✅     | ✅      | ✅    |
| GET  /records/get                 | ❌     | ✅      | ✅    |
| POST /records/create              | ❌     | ❌      | ✅    |
| PUT  /records/update/{id}         | ❌     | ❌      | ✅    |
| DELETE /records/delete/{id}       | ❌     | ❌      | ✅    |
| GET  /admin/get                   | ❌     | ❌      | ✅    |
| PATCH /admin/update/role/{id}     | ❌     | ❌      | ✅    |
| PATCH /admin/toogle/status/{id}   | ❌     | ❌      | ✅    |
| DELETE /admin/delete/{id}         | ❌     | ❌      | ✅    |

> **Note:** Registration always assigns `ROLE_VIEWER`. Role elevation is done by admin only.

---

## Default Admin Credentials

```
Email:    admin@finance.com
Password: Admin@123
```

> A `DataSeeder` automatically creates this admin on first startup if it doesn't already exist.  
> Change these credentials immediately in a real environment.

---

## API Reference

### Auth Controller

| Method | Endpoint             | Description                        | Auth Required |
|--------|----------------------|------------------------------------|---------------|
| POST   | /auth/register       | Register a new user (VIEWER role)  | No            |
| POST   | /auth/login          | Login and receive JWT + refresh token | No         |
| POST   | /auth/refresh        | Get new access token via refresh token | No        |
| POST   | /auth/logout/single  | Logout from current device         | Yes           |
| POST   | /auth/logout/all     | Logout from all devices            | Yes           |

**Register request:**
```json
{
  "email": "user@example.com",
  "password": "pass@123",
  "name": "John Doe"
}
```

**Login request:**
```json
{
  "email": "admin@finance.com",
  "password": "Admin@123"
}
```

**Login response:**
```json
{
  "status": "OK",
  "jwtToken": "<token>",
  "refreshToken": "<uuid>"
}
```

---

### Financial Records Controller

| Method | Endpoint                  | Description                        | Role          |
|--------|---------------------------|------------------------------------|---------------|
| POST   | /records/create           | Create a financial record          | ADMIN         |
| GET    | /records/get              | Get paginated records with filters | ADMIN,ANALYST |
| PUT    | /records/update/{id}      | Update a record                    | ADMIN         |
| DELETE | /records/delete/{id}      | Soft delete a record               | ADMIN         |

**Record request body:**
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "description": "Monthly salary",
  "transactionDate": "2026-04-06"
}
```

**Available filters for GET /records/get:**

| Param    | Type            | Example        |
|----------|-----------------|----------------|
| type     | INCOME/EXPENSE  | ?type=INCOME   |
| category | String          | ?category=Food |
| from     | yyyy-MM-dd      | ?from=2026-01-01 |
| to       | yyyy-MM-dd      | ?to=2026-04-06 |
| page     | int (default 0) | ?page=0        |
| size     | int (default 10)| ?size=10       |

---

### Dashboard Controller

| Method | Endpoint           | Description              | Role                    |
|--------|--------------------|--------------------------|-------------------------|
| GET    | /dashboard/summary | Get full dashboard data  | ADMIN, ANALYST, VIEWER  |

**Response includes:**
- Total income
- Total expenses
- Net balance
- Category-wise totals
- Weekly trends
- Monthly trends
- Recent activity (last 10 records)

---

### Admin Controller

| Method | Endpoint                       | Description              |
|--------|--------------------------------|--------------------------|
| GET    | /admin/get                     | List all users (paginated) |
| PATCH  | /admin/update/role/{userId}    | Update user role         |
| PATCH  | /admin/toogle/status/{userId}  | Toggle user active status |
| DELETE | /admin/delete/{userId}         | Soft delete a user       |

**Update role — valid values:**
```
?role=ROLE_VIEWER
?role=ROLE_ANALYST
?role=ROLE_ADMIN
```

---

## Running with Docker (Recommended)

Make sure Docker is installed, then run:

```bash
docker-compose up
```

This pulls `harsh20jain/screening:v1` from Docker Hub and spins up MySQL, Redis, and the app automatically.

The app starts only after MySQL and Redis pass their health checks.

---

## Running Locally

### Prerequisites
- Java 21
- MySQL 8
- Redis
- Maven

### Steps

1. Clone the repository
```bash
git clone <repo-url>
cd screening
```

2. Create the MySQL database
```sql
CREATE DATABASE screening;
```

3. Update `src/main/resources/application.yml` if your credentials differ:
```yaml
datasource:
  username: your_username
  password: your_password
```

4. Start Redis locally (default port 6379)

5. Run the application
```bash
./mvnw spring-boot:run
```

The app runs on `http://localhost:8080`.

---

## Authentication Flow

1. Call `POST /auth/login` → receive `jwtToken` and `refreshToken`
2. Pass `jwtToken` in every request header: `Authorization: Bearer <token>`
3. JWT expires in **15 minutes**
4. Use `POST /auth/refresh` with `Refresh-Token: <refreshToken>` header to get a new access token
5. Refresh token expires in **7 days**

> **Note:** RSA key pair is generated on every application startup. All existing tokens become invalid after a restart. In a production environment, keys should be loaded from a keystore or environment variables.

---

## CSRF Protection

CSRF protection is enabled for all state-changing endpoints. Here's how to handle it:

### How it works
1. On your **first request** (e.g. login), the server sets a `XSRF-TOKEN` cookie in the response
2. Copy that cookie value
3. Pass it as a header `X-XSRF-TOKEN` in all subsequent requests

### Endpoints that require X-XSRF-TOKEN

All authenticated endpoints require this header except:

| Endpoint | CSRF Required |
|---|---|
| POST /auth/login | ❌ |
| POST /auth/register | ❌ |
| POST /auth/refresh | ❌ |
| POST /auth/logout/single | ❌ |
| POST /auth/logout/all | ❌ |
| All other endpoints | ✅ |

### In Postman

**Step 1** — Call `POST /auth/login`. In the response, go to **Cookies** tab and copy the value of `XSRF-TOKEN`.

**Step 2** — Add this header to every subsequent request:
```
Key:   X-XSRF-TOKEN
Value: <paste XSRF-TOKEN cookie value here>
```

**Example headers for a protected request:**
```
Authorization:  Bearer <jwtToken>
X-XSRF-TOKEN:   06504f97-c605-45d0-a6d4-a3be2d8fecb0
```

> **Note:** The CSRF token is tied to your session. If you clear cookies or start a new session, you need to fetch a fresh token by calling login again.

---

## API Documentation

A Postman collection is included in the repository: `Screening.postman_collection.json`

Import it into Postman to test all endpoints. For protected routes, set:
- `Authorization: Bearer <jwtToken>` from the login response
- `X-XSRF-TOKEN: <value>` from the `XSRF-TOKEN` cookie set after login

---

## Assumptions & Design Decisions

1. **Registration always assigns `ROLE_VIEWER`** — role elevation is done exclusively by admin.
2. **ANALYST sees all records** — not scoped to their own, useful for reporting and insights.
3. **VIEWER can only access dashboard summary** — no access to raw transaction records.
4. **Soft delete on both users and records** — data is never permanently removed; historical records are preserved for dashboard accuracy and audit purposes.
5. **Inactive users cannot login** — the `active` flag is checked at authentication time via `CustomUserDetailService`.
6. **Deleting a user does not delete their records** — financial history is retained for audit and dashboard aggregation correctness.
7. **RSA keys regenerate on startup** — acceptable for this assignment scope. In production, persist keys via environment variables or a keystore.
8. **Redis is required** — used for token blacklisting, enabling reliable single-device and all-device logout.
9. **CSRF protection is enabled** — requests from a browser context need an `X-XSRF-TOKEN` header obtained from the `XSRF-TOKEN` cookie.

---

## Optional Features Implemented

- ✅ JWT authentication with RSA RS256
- ✅ Refresh token support
- ✅ Logout from single device
- ✅ Logout from all devices
- ✅ Soft delete for users and financial records
- ✅ Pagination on record listing
- ✅ Filtering by type, category, and date range
- ✅ Role-based data scoping (admin/analyst see all, viewer sees none)
- ✅ Data seeder for first admin
- ✅ Docker support with Docker Hub image
- ✅ Global exception handling with structured error responses
- ✅ Input validation with field-level error messages
```
