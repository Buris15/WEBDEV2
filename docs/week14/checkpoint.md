# WEBDEV2 — Week 14 F2 Checkpoint

## Project continuity

- Project: Pet Veterinary Clinic.
- Branch: create `week14` from the submitted `week13` branch; never overwrite the earlier branch.
- Application root: repository root (`pom.xml`, Maven wrapper, `src/`, and `docs/`).
- Stack: vanilla HTML/CSS/JavaScript → Spring Boot 3.5.5 REST API → MySQL 8.0.46, port 3307, schema `infoman1_vetclinic_finals`.
- Shared INFOMAN1 migration: `week14/alter_week14.sql` adds `vet_availability`.

Create the branch from the submitted Week 13 branch without changing old branches:

```bash
git switch week13
git pull origin week13
git switch -c week14
git status
```

After checking the changes and local tests, commit meaningful stages and push:

```bash
git add .
git commit -m "Implement Week 14 pet and appointment CRUD"
git push -u origin week14
```

## Setup and run (PowerShell)

1. Confirm MySQL is running and the Week 13 schema/seed/views are present.
2. Run INFOMAN1 `week14/alter_week14.sql` once to install the schedule table and seed rows.
3. Set local connection environment variables and run the app:

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3307/infoman1_vetclinic_finals?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_LOCAL_PASSWORD"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

4. Open `http://localhost:8080/` and execute `docs/week14/api-tests.http` in a REST Client or equivalent. Never commit `.env`, passwords, tokens, or private database records.

## Requirement → feature → test/evidence

| Requirement | Feature / file | Test / evidence |
|---|---|---|
| Pet CRUD persists | `PetController`, `PetService`, `PetWriteRequest` | Create, read, update, delete a disposable pet; reload list and query MySQL |
| Appointment CRUD persists | `AppointmentController`, `AppointmentService` | Create, read, update, cancel an appointment; reload list and query MySQL |
| Relationship choices come from API | `ClinicLookupController`, `/api/owners`, `/api/pets`, `/api/vet-availability` | Inspect options and Network request responses; no hard-coded FK IDs in JS |
| Server validation and safe UI state | DTO Bean Validation, `request()` helper, form handlers | Invalid fields, duplicate chip, pending state, retained failed input |
| Empty 204 and destructive confirmation | `request()` helper and delete/cancel actions | DELETE returns 204; no JSON parse error; browser asks before deletion |
| Atomic appointment booking | `AppointmentService.bookAppointment` | POST one booking; query appointment and availability before/after |
| Rollback after first write | Invalid pet ID request reserves then fails before appointment insert | API returns 404; availability and appointment count match before-state |
| Conflict and network recovery | API exception handler and UI status region | Pet FK delete 409, no schedule capacity 409, stop/restart backend |

## Executed test matrix — fill with actual results

| # | Test | Expected | Actual HTTP/UI result | MySQL evidence |
|---:|---|---|---|---|
| 1 | Create a valid Pet | 201 Created; row persists | PENDING | PENDING |
| 2 | Read the created Pet | 200 OK with same values | PENDING | PENDING |
| 3 | Update Pet using complete PUT payload | 200 OK; updated row persists | PENDING | PENDING |
| 4 | Delete disposable Pet with no children | 204 No Content; row absent after reload | PENDING | PENDING |
| 5 | Submit negative age / blank required field | 400; field message; input remains | PENDING | PENDING |
| 6 | Duplicate microchip | 409; existing row unchanged | PENDING | PENDING |
| 7 | Delete seeded Pet with appointment/vaccination history | 409; related history remains | PENDING | PENDING |
| 8 | Book a valid appointment | 201; appointment inserted and capacity decremented by one | PENDING | PENDING |
| 9 | Update appointment to another available vet/date | 200; new slot decremented, old slot restored | PENDING | PENDING |
| 10 | Cancel appointment | 204; appointment absent and slot restored | PENDING | PENDING |
| 11 | Book with nonexistent Pet ID | 404; first schedule write rolled back | PENDING | PENDING |
| 12 | Read nonexistent appointment | 404 with safe message | PENDING | PENDING |
| 13 | Choose a full/no-schedule slot | 409; no appointment inserted | PENDING | PENDING |
| 14 | Stop backend, submit, restart and retry | Clear network error; successful recovery | PENDING | PENDING |

The requirement calls for at least eight executed cases. Capture browser UI, DevTools Network method/status/payload, API body, and direct MySQL before/after rows. Do not use a disappearing DOM row as proof of database deletion.

## API contract

- `GET /api/pets`, `GET /api/pets/{id}`, `POST /api/pets`, `PUT /api/pets/{id}`, `DELETE /api/pets/{id}`.
- `GET /api/appointments`, `GET /api/appointments/{id}`, `POST /api/appointments`, `PUT /api/appointments/{id}`, `DELETE /api/appointments/{id}`.
- `GET /api/owners`, `GET /api/veterinarians`, `GET /api/vet-availability?date=YYYY-MM-DD`.
- Existing reads remain: `GET /api/v1/pets?page=0&size=10` and `GET /api/v1/pets/{id}`.
- POST returns 201 with JSON; GET/PUT return 200 with JSON; DELETE returns 204 without a body; validation is 400; missing IDs 404; capacity, uniqueness, and referential conflicts 409.
- `docs/week14/api-tests.http` contains example requests. Change IDs/date to match available test data before running.

## Known limitations / verification status

- Seeded veterinarian schedule rows currently cover 2026-11-10 through 2027-03-31. Add later rows with a controlled schedule-management process when required.
- The project has no formal automated MySQL integration-test profile yet; the Week 14 verification is performed through API requests and direct MySQL queries. Do not substitute H2-only evidence for the required MySQL evidence.
- The supplied preparation environment cannot access your local MySQL service or browser. Maven compilation, endpoint execution, screenshots, and the test matrix must be verified on your computer before submission.

## Change log

- Added Appointment CRUD with schedule-aware booking, update, and cancellation.
- Added veterinarian/date capacity JPA model and pessimistic row locking.
- Added owner, veterinarian, and availability lookup endpoints for relationship selectors.
- Expanded the vanilla JS interface with Pet and Appointment create/edit/delete forms, pending states, confirmations, response-aware requests, and relationship selectors.
- Added structured 400/404/409 error responses and Week 14 API test requests.
