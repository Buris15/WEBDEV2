# Pet Veterinary Clinic — WEBDEV2 Week 14 (F2)

Shared final project for WEBDEV2 and INFOMAN1: vanilla HTML/CSS/JavaScript → Spring Boot REST API → MySQL.

## Requirements
- Java 17 or later
- MySQL 8.0.16 or later (InnoDB)
- Windows CMD or PowerShell
- Internet access on first Maven-wrapper/dependency download if dependencies are not cached

## Database
The database is `infoman1_vetclinic_finals`. The local repository folder may be named `infoman`; do not confuse the repository name with the database name.

For an existing Week 13 database, keep its tables/seed data and execute the INFOMAN repository's `week14/alter_week14.sql`. Do not rerun `week13/schema.sql` over an existing populated database.

Known local connection from the Week 13 project: `127.0.0.1:3307`.

## Run from Windows CMD
Open CMD in the folder containing `pom.xml` and `mvnw.cmd`:

```cmd
set "DB_URL=jdbc:mysql://127.0.0.1:3307/infoman1_vetclinic_finals?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
set "DB_USERNAME=root"
set "DB_PASSWORD=YOUR_LOCAL_MYSQL_PASSWORD"
mvnw.cmd test
mvnw.cmd spring-boot:run
```

Replace the password locally. Never commit credentials. Open `http://localhost:8080/`.

## API
- `GET/POST /api/pets`, `GET/PUT/DELETE /api/pets/{id}` — Pet CRUD.
- `GET/POST /api/appointments`, `GET/PUT/DELETE /api/appointments/{id}` — Appointment CRUD.
- `GET /api/owners`, `GET /api/veterinarians`, `GET /api/vet-availability?date=YYYY-MM-DD` — live relationship and availability choices.
- Existing F1 reads remain at `GET /api/v1/pets` and `GET /api/v1/pets/{id}`.

POST returns 201, GET/PUT return 200, and DELETE returns 204 without a body. Validation is 400, missing records are 404, and duplicate/referential/capacity conflicts are 409.

## Atomic appointment workflow
`AppointmentService` performs the availability-row lock, capacity check/decrement, and appointment insert in one `@Transactional` public service method. Updates reserve/release capacity as necessary in one transaction. Cancellation releases the slot and deletes the appointment in one transaction. `VetAvailabilityRepository` uses a pessimistic write lock so concurrent bookings do not both claim the same remaining slot.

## Week 14 documents
See `docs/week14/lecture.md`, `docs/week14/checkpoint.md`, and `docs/week14/api-tests.http`. The checkpoint keeps actual results as `PENDING` until you execute the tests on your local MySQL database.

## Week 15 F3 — Authentication and authorization

The application now uses Spring Security server-side sessions, BCrypt password verification against `app_user`, and the `MANAGER` / `READER` roles. The manager can use the existing pet and appointment CRUD endpoints. The reader can use protected GET requests only. The backend enforces these rules independently of the role-aware frontend.

The application must connect to MySQL as `capstone_app`, not `root`. Run INFOMAN1 Week 15 `auth_schema.sql` and account provisioning first. The runtime MySQL account requires SELECT on `app_user`, and the application-user rows are inserted by the local schema maintainer using BCrypt hashes. See `docs/week15/README.md` and `docs/week15/` for the test matrix and evidence plan.
