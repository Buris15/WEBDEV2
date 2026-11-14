# WEBDEV2 — Week 13 F1 Lecture

## 1. Baseline audit

The supplied WEBDEV2 Week 8 project contains a Spring Boot 3.5.5 / Java 17 application. It has a `Pet` entity, a `PetRepository`, a `PetService`, and a REST `PetController` under `/api/pets`. It already supports basic create, list, detail, update, delete, species search, name search, distinct species, and batch registration.

The Week 8 `application.properties` points to a MariaDB URL for a separate `webdev2_week8` database. Its model contains pet microchip number, name, species, and breed, but does not include the INFOMAN1 clinic relationships or the `owner_id`, age, veterinarian, appointment, and vaccination data. The supplied Week 8 project therefore gives us a usable pet CRUD starting point, but not a complete shared clinic schema.

| Requirement / feature | Baseline status | Evidence reference |
|---|---|---|
| Pet CRUD service and repository | Existing / to be re-run | Supplied `src/main/java/.../PetService.java`, `PetRepository.java`, `PetController.java` |
| Unique required microchip | Existing mapping | Supplied `Pet.java` used `@Column(unique=true, nullable=false)` |
| MySQL 8 shared clinic schema | Missing in supplied WEBDEV2 config | Supplied `application.properties` used `jdbc:mariadb` and `webdev2_week8` |
| Owner, veterinarian, appointment and vaccination relationships | Missing from supplied WEBDEV2 entities | Week 3/4 INFOMAN1 clinic design; added in this F1 project |
| DTO-based paginated pet list/detail API | Missing from supplied project | Added `/api/v1/pets` read endpoints |
| Vanilla JavaScript browser read interface | Missing from supplied project | Added `src/main/resources/static/` |

## 2. Six testable requirements

1. The application shall display a paginated list of pets retrieved from the shared MySQL database.
2. A user shall be able to open a pet record by ID and see its owner, appointments, and vaccination history.
3. Each pet shall have a unique, required microchip number.
4. Each pet shall belong to exactly one owner, and the owner shall be shown in the pet details.
5. Each appointment shall reference exactly one pet and one veterinarian; the pet details shall show appointment date, reason, and veterinarian.
6. A missing pet ID shall return HTTP 404, while a valid empty list shall return HTTP 200 with an empty `content` array.

## 3. Architecture and data-flow

```text
Browser (vanilla HTML / CSS / JavaScript)
        | HTTP GET /api/v1/pets and /api/v1/pets/{id}
        v
Spring Boot REST Controller
        v
PetReadService (maps entities to response DTOs inside read-only transactions)
        v
Spring Data JPA Repositories
        v
MySQL 8+ — infoman1_vetclinic_finals
```

The browser only sends HTTP requests and renders JSON. It does not connect to MySQL and never receives database credentials. The backend applies request handling and persistence rules; MySQL stores rows and enforces PK, FK, UNIQUE, NOT NULL, and CHECK constraints.

## 4. Endpoint contract and response shape

| Method and path | Request | Successful response | Error / empty behavior |
|---|---|---|---|
| `GET /api/v1/pets?page=0&size=10` | Page and size query parameters | HTTP 200, Spring `Page` JSON with records in `content` | HTTP 200 with `content: []` when empty |
| `GET /api/v1/pets/{id}` | Pet ID path parameter | HTTP 200, `PetDetailResponse` DTO | HTTP 404 when ID does not exist |

The list DTO contains ID, microchip number, name, species, breed, age, and owner name. The detail DTO also includes owner phone, appointment summaries, and vaccination summaries. The response does not expose the JPA entity graph.

## 5. DTO and persistence boundary

JPA entities represent relational tables and contain lazy relationships. They are not returned by the new F1 read endpoints. `PetReadService` maps entity values into `PetListItemResponse` and `PetDetailResponse` while a read-only transaction is active. This prevents lazy proxies and bidirectional entity references from becoming the browser's JSON contract.

## 6. Origins and CORS

An origin is the combination of scheme, host, and port. A page at `http://localhost:8080` and an API at `http://localhost:8080` are same-origin. A Live Server page at `http://localhost:5500` calling `http://localhost:8080` is cross-origin, so the backend must allow the exact frontend origin. This project uses `APP_CORS_ALLOWED_ORIGIN` for that exact value. `no-cors` is not a solution because it prevents the frontend from reading a normal JSON response.

## 7. Actual DevTools diagnosis — complete after local run

Do not invent this evidence. After starting the application, open browser DevTools → Network, reload the pet list, and record the actual request URL, status, response JSON shape, and any error. If the first attempt fails, classify it as connection refused, 404 route, 500 backend/database error, or CORS policy block; record the cause and actual fix in `evidence/devtools_diagnosis.md`.

## 8. Read-path trace — complete with actual run

Use `GET /api/v1/pets?page=0&size=10` in the browser. The controller calls `PetReadService.listPets`, which calls `PetRepository.findAll(Pageable)`. The repository loads the owner relationship for the list page; the service maps each row into `PetListItemResponse`; Spring serializes the page as JSON; JavaScript reads `result.content` and renders the rows using `textContent`. Select View details to call `GET /api/v1/pets/{id}`; the service separately reads appointments and vaccination records and maps them to DTOs.

Before submitting, identify one actual pet ID and compare its browser detail with `SELECT` output from MySQL. Save both results in `docs/week13/evidence/`.
