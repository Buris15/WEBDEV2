# Pet Veterinary Clinic — WEBDEV2 Week 13 (F1)

This project continues the supplied WEBDEV2 Week 8 Pet CRUD/JPA code and aligns it with the Veterinary Clinic domain used in INFOMAN1. The shared MySQL database is `infoman1_vetclinic_finals`.

## Project baseline actually found in the supplied Week 8 archive

- Spring Boot 3.5.5 and Java 17.
- A `Pet` entity with `id`, `microchipNumber`, `name`, `species`, and `breed`.
- `PetRepository`, `PetService`, and `/api/pets` CRUD/search endpoints.
- A `PetSmokeTestRunner` that inserts a sample pet if the table is empty.
- `application.properties` used a MariaDB JDBC URL and a separate `webdev2_week8` schema; this package changes the datasource to MySQL and the shared clinic schema.
- The original `User` JPA example was not part of the clinic ERD and is omitted from the shared domain model.

## Run order

1. Install/use a MySQL Server version 8.0.16 or later. Workbench is only a client; check the server with `SELECT VERSION();`.
2. From the INFOMAN1 `week13` folder, execute `schema.sql`, `seed.sql`, and `views.sql` in that order.
3. Set local environment variables `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` (examples below). Never commit real credentials.
4. Run the application with Java 17+ and Maven.
5. Open `http://localhost:8080/`. The page calls the read-only DTO endpoints `/api/v1/pets` and `/api/v1/pets/{id}`.

PowerShell example:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/infoman1_vetclinic_finals?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_LOCAL_PASSWORD"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

If the Maven wrapper needs to download Maven, an internet connection may be required.

## API contract

| Method | Path | Success | Failure / notes |
|---|---|---:|---|
| GET | `/api/v1/pets?page=0&size=10` | 200 | Paginated JSON; records are in `content`. |
| GET | `/api/v1/pets/{id}` | 200 | 404 when no pet exists. Includes owner, appointments, and vaccinations. |
| GET | `/api/pets` | 200 | Legacy Week 8 CRUD API, JSON DTO list. |
| POST | `/api/pets` | 201 | Requires `microchipNumber`, `name`, `species`, `breed`, `age`, and `ownerId`. |
| GET | `/api/pets/{id}` | 200 | 404 when no pet exists. |
| PUT | `/api/pets/{id}` | 200 | Requires complete valid pet body including `ownerId`. |
| DELETE | `/api/pets/{id}` | 204 | 404 if missing; 409 when dependent history prevents deletion. |
| GET | `/api/pets/species?name=Dog` | 200 | Empty list if none match. |
| GET | `/api/pets/search?keyword=Ma` | 200 | Empty list if none match. |
| GET | `/api/pets/species-list` | 200 | Distinct species list. |

## CORS and origin

The default page is served by Spring Boot at `http://localhost:8080/`, so its API calls are same-origin and do not require CORS. If you serve it with VS Code Live Server at `http://localhost:5500`, set `APP_CORS_ALLOWED_ORIGIN=http://localhost:5500`. Do not use `mode: "no-cors"`.

## Important verification limit

The source package is prepared, but no local MySQL server or Maven executable is available in the preparation environment. Do not treat generated files as a passing run. Capture the actual MySQL version, application startup, browser results, DevTools Network request/response, and matching SQL result on your computer.
