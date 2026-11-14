# WEBDEV2 — Week 13 F1 Checkpoint

## Project

- Project: Pet Veterinary Clinic
- Branch: `week13` (create from the latest working final-project branch required by the handbook, normally `week11`; do not overwrite an existing branch).
- Source baseline supplied here: WEBDEV2 Week 8 `webdev2` archive.
- Shared database: `infoman1_vetclinic_finals` (also used by INFOMAN1 Week 13 scripts).
- Application root: repository root, including `pom.xml`, Maven wrapper, `src/`, and `docs/`.

## Versions to record from the actual computer

- Java: run `java -version` and record output.
- Spring Boot: `3.5.5` in `pom.xml`.
- MySQL server: run `SELECT VERSION();` and record actual value (must be MySQL 8.0.16+ for the shared F1 schema).
- Maven: run `./mvnw -version` (Windows: `mvnw.cmd -version`).

## Setup and run

1. Ensure MySQL Server 8.0.16+ is running.
2. Execute INFOMAN1 `week13/schema.sql`, then `seed.sql`, then `views.sql`.
3. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` locally.
4. Run `./mvnw test` and `./mvnw spring-boot:run` (Windows use `mvnw.cmd`).
5. Open `http://localhost:8080/`.
6. Test `GET /api/v1/pets?page=0&size=10` and `GET /api/v1/pets/1`.
7. Compare one API/browser pet with the same row from a direct SQL query.

PowerShell example:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/infoman1_vetclinic_finals?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_LOCAL_PASSWORD"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

## Requirement → feature → test/evidence

| Requirement | Feature / file | Test and evidence |
|---|---|---|
| Pet list is loaded from MySQL | `PetReadController`, `PetReadService`, `PetRepository`, `static/js/app.js` | Browser list + Network response + direct SQL comparison |
| Pet detail shows related clinic data | `PetReadService`, `AppointmentRepository`, `VaccinationRecordRepository` | Select View details; compare pet, owner, appointments and vaccinations to SQL |
| Unique chip and required owner | `Pet`, `schema.sql`, `PetWriteRequest` | Duplicate microchip and invalid owner tests in INFOMAN1 evidence script |
| Appointment includes pet and veterinarian | `Appointment`, FKs, detail DTO | Detail page plus `vw_appointment_overview` result |
| Empty state is safe | `app.js` | Test using a fresh empty database or a filter/list case returning no rows |
| Missing ID is handled | `PetReadController` | Request `/api/v1/pets/999999`; record actual 404 response |

## Current known issues / verification status

- The supplied Week 8 configuration used `jdbc:mariadb` and `webdev2_week8`. This F1 package uses MySQL and the single clinic schema. Verify the actual installed server; MariaDB is not a substitute for the guide's MySQL 8.0.16+ requirement.
- The package was prepared from the Week 8 archive snapshot. If a newer Week 11 branch contains later approved features, merge these changes into that branch instead of replacing it with the older archive.
- No actual MySQL connection, Maven test run, browser capture, or DevTools diagnosis has been performed in this preparation environment. Attach real local results before submission.

## Change log

- Replaced the isolated Week 8 MariaDB datasource with the shared MySQL clinic schema configuration.
- Added Owner, Veterinarian, Appointment, and VaccinationRecord JPA mappings and the owner association on Pet.
- Added DTO-based paginated list and detail read endpoints.
- Added the vanilla JavaScript list/detail interface, loading/empty/error/not-found handling, and scoped CORS setting.

Java: 17.0.20.1
Spring Boot: 3.5.5
Database: MySQL 8.0.46
Port: 3307
Database: infoman1_vetclinic_finals