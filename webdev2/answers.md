# WEBDEV2 — WEEK 10 LAB ANSWERS
## Capstone Milestone 3: RESTful CRUD API

**Name:** Adrian Lacsamana  
**Student ID:** 2510955
**GitHub Repository:** https://github.com/Buris15/webdev2  
**Branch:** week10  
**Date Submitted:** November 13, 2026

---

# TASK 1 — URL DESIGN & CONTROLLER SKELETON

## URL Design Table

For my Automatic Pig Feeder capstone, I selected Pig as the primary resource because the system needs to manage pig information.

| HTTP Method | URL Path | Expected Status Codes |
|---|---|---|
| GET | `/api/pigs` | 200 OK |
| GET | `/api/pigs/{id}` | 200 OK, 404 Not Found |
| POST | `/api/pigs` | 201 Created, 400 Bad Request, 409 Conflict |
| PUT | `/api/pigs/{id}` | 200 OK, 400 Bad Request, 404 Not Found |
| PATCH | `/api/pigs/{id}` | 200 OK, 400 Bad Request, 404 Not Found |
| DELETE | `/api/pigs/{id}` | 204 No Content, 404 Not Found |

## Controller Skeleton

Created `PigController.java` using `@RestController` and `@RequestMapping("/api/pigs")`.

The controller includes the five required mapping annotations:

- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@PatchMapping`
- `@DeleteMapping`

The initial controller skeleton was later connected to the service layer and implemented with the required CRUD operations.

Example:

```java
@RestController
@RequestMapping("/api/pigs")
public class PigController {

    @GetMapping
    public ResponseEntity<?> getAllPigs() {
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPigById(
            @PathVariable Long id) {
        return null;
    }

    @PostMapping
    public ResponseEntity<?> createPig(
            @RequestBody CreatePigRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePig(
            @PathVariable Long id,
            @RequestBody UpdatePigRequest request) {
        return null;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchPig(
            @PathVariable Long id,
            @RequestBody UpdatePigRequest request) {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePig(
            @PathVariable Long id) {
        return null;
    }
}
```

## Evidence

- [Insert screenshot of PigController.java showing all mapping annotations.]
- [Insert screenshot or REST Client output showing a working stub endpoint.]

## Reflection Question

**Why do REST URLs use plural nouns (`/api/students`) rather than verb phrases (`/api/getStudents`), and what role does the HTTP method play?**

**Answer:**

REST URLs use plural nouns because they represent resources or collections of resources. The HTTP method determines the operation to perform. GET retrieves information, POST creates a resource, PUT replaces information, PATCH updates selected fields, and DELETE removes a resource.

---

# TASK 2 — GET ENDPOINTS: LIST & GET BY ID

## Approach / Explanation

I implemented two GET endpoints for the Pig resource.

The GET all endpoint retrieves all pig records from the database and returns them as a list of PigDTO objects with a 200 OK response. When there are no records, it returns an empty list instead of a 404 error.

The GET by ID endpoint retrieves a specific pig and returns 200 OK when the record exists or 404 Not Found when it does not.

I also created a `toDTO()` method in PigService to convert JPA entities into response DTOs.

## PigDTO.java

```java
package com.example.webdev2.dto;

public class PigDTO {

    private Long id;
    private String tagNumber;
    private String name;
    private String breed;
    private Double weightKg;

    public PigDTO() {
    }

    public PigDTO(
            Long id,
            String tagNumber,
            String name,
            String breed,
            Double weightKg) {

        this.id = id;
        this.tagNumber = tagNumber;
        this.name = name;
        this.breed = breed;
        this.weightKg = weightKg;
    }

    public Long getId() {
        return id;
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }
}
```

## toDTO() Mapping Method

Implemented inside `PigService.java`:

```java
public PigDTO toDTO(Pig pig) {

    return new PigDTO(
            pig.getId(),
            pig.getTagNumber(),
            pig.getName(),
            pig.getBreed(),
            pig.getWeightKg()
    );
}
```

## GET All Pigs

**Request:**

```http
GET /api/pigs
```

**URL:**

`http://localhost:8080/api/pigs`

**Expected Response:**

```http
HTTP/1.1 200 OK
```

Example JSON:

```json
[
    {
        "id": 1,
        "tagNumber": "PIG-001",
        "name": "Bantay",
        "breed": "Landrace",
        "weightKg": 45.5
    }
]
```

When no records exist, the expected response body is:

```json
[]
```

## GET Pig by ID

**Request:**

```http
GET /api/pigs/1
```

**Expected Response when found:**

```http
HTTP/1.1 200 OK
```

**Expected Response when not found:**

```http
HTTP/1.1 404 Not Found
```

## Evidence

- [Insert screenshot of PigDTO.java.]
- [Insert screenshot of PigService.java showing toDTO().]
- [Insert GET all response showing 200 OK.]
- [Insert GET by ID response showing 200 OK.]
- [Insert GET by nonexistent ID response showing 404 Not Found.]

## Reflection Question

**What would go wrong if you returned the JPA entity directly from `@GetMapping("/{id}")` instead of a DTO?**

**Answer:**

Returning a JPA entity directly may expose internal database fields and relationships that should not be included in API responses. It may also cause serialization problems when entities have bidirectional relationships. Using DTOs controls the information shared with clients and separates the API response from the database entity.

---

# TASK 3 — POST ENDPOINT: CREATE WITH 201 + LOCATION

## Approach / Explanation

I created a `CreatePigRequest` DTO to receive the required information when registering a new pig.

The service converts the request DTO into a Pig entity, saves it using PigRepository, and converts the saved entity into PigDTO before returning it to the controller.

The controller uses `ResponseEntity.created()` to return 201 Created and a Location header containing the URL of the newly created pig.

## CreatePigRequest.java

```java
package com.example.webdev2.dto;

public class CreatePigRequest {

    private String tagNumber;
    private String name;
    private String breed;
    private Double weightKg;

    public CreatePigRequest() {
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }
}
```

## Service Create Method

Implemented inside PigService.java:

```java
public PigDTO create(CreatePigRequest request) {

    if (request.getTagNumber() == null ||
            request.getTagNumber().isBlank() ||
            request.getName() == null ||
            request.getName().isBlank()) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tag number and name are required"
        );
    }

    if (pigRepository.existsByTagNumber(
            request.getTagNumber())) {

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Pig tag number already exists"
        );
    }

    Pig pig = new Pig();

    pig.setTagNumber(request.getTagNumber());
    pig.setName(request.getName());
    pig.setBreed(request.getBreed());
    pig.setWeightKg(request.getWeightKg());

    Pig savedPig = pigRepository.save(pig);

    return toDTO(savedPig);
}
```

## Controller create() Method

Implemented inside PigController.java:

```java
@PostMapping
public ResponseEntity<PigDTO> createPig(
        @RequestBody CreatePigRequest request) {

    PigDTO created = pigService.create(request);

    URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();

    return ResponseEntity
            .created(location)
            .body(created);
}
```

## Sample POST Request

**URL:**

`http://localhost:8080/api/pigs`

**Method:** POST

**Body:**

```json
{
    "tagNumber": "PIG-W10-001",
    "name": "Bantay",
    "breed": "Landrace",
    "weightKg": 45.5
}
```

**Expected Response:**

```http
HTTP/1.1 201 Created
Location: http://localhost:8080/api/pigs/GENERATED_ID
```

Example response body:

```json
{
    "id": 6,
    "tagNumber": "PIG-W10-001",
    "name": "Bantay",
    "breed": "Landrace",
    "weightKg": 45.5
}
```

## Evidence

- [Insert screenshot of CreatePigRequest.java.]
- [Insert screenshot of create() method showing Location header.]
- [Insert POST response showing 201 Created and Location header.]

## Reflection Question

**Why does the REST contract specify 201 Created for a successful POST rather than 200 OK, and why is the Location header useful to include?**

**Answer:**

201 Created indicates that a new resource has been successfully created. The Location header provides the URL of the newly created resource so the client can easily access it using a GET request.

---

# TASK 4 — PUT, PATCH & DELETE

## Approach / Explanation

I implemented PUT, PATCH, and DELETE endpoints for the Pig resource.

The PUT method replaces all updatable information of an existing pig. The PATCH method updates only the fields provided in the request while keeping other fields unchanged.

The DELETE method checks whether the pig exists before removing it. When the record is successfully deleted, the API returns 204 No Content. When the same ID is deleted again, the API returns 404 Not Found.

## UpdatePigRequest.java

```java
package com.example.webdev2.dto;

public class UpdatePigRequest {

    private String tagNumber;
    private String name;
    private String breed;
    private Double weightKg;

    public UpdatePigRequest() {
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }
}
```

## PUT Service Method

```java
public Optional<PigDTO> update(
        Long id,
        UpdatePigRequest request) {

    if (request.getTagNumber() == null ||
            request.getTagNumber().isBlank() ||
            request.getName() == null ||
            request.getName().isBlank()) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tag number and name are required");
    }

    return pigRepository.findById(id).map(pig -> {

        if (pigRepository.existsByTagNumberAndIdNot(
                request.getTagNumber(), id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pig tag number already exists");
        }

        pig.setTagNumber(request.getTagNumber());
        pig.setName(request.getName());
        pig.setBreed(request.getBreed());
        pig.setWeightKg(request.getWeightKg());

        return toDTO(pigRepository.save(pig));
    });
}
```

## PATCH Service Method

```java
public Optional<PigDTO> patch(
        Long id,
        UpdatePigRequest request) {

    if (request.getTagNumber() == null &&
            request.getName() == null &&
            request.getBreed() == null &&
            request.getWeightKg() == null) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "At least one field must be provided");
    }

    return pigRepository.findById(id).map(pig -> {

        if (request.getTagNumber() != null) {

            if (request.getTagNumber().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tag number cannot be blank");
            }

            if (pigRepository.existsByTagNumberAndIdNot(
                    request.getTagNumber(), id)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Pig tag number already exists");
            }

            pig.setTagNumber(request.getTagNumber());
        }

        if (request.getName() != null) {

            if (request.getName().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Name cannot be blank");
            }

            pig.setName(request.getName());
        }

        if (request.getBreed() != null) {
            pig.setBreed(request.getBreed());
        }

        if (request.getWeightKg() != null) {
            pig.setWeightKg(request.getWeightKg());
        }

        return toDTO(pigRepository.save(pig));
    });
}
```

## DELETE Service Method

```java
public boolean delete(Long id) {

    if (!pigRepository.existsById(id)) {
        return false;
    }

    pigRepository.deleteById(id);

    return true;
}
```

## Controller Methods

**PUT:**

```java
@PutMapping("/{id}")
public ResponseEntity<PigDTO> updatePig(
        @PathVariable Long id,
        @RequestBody UpdatePigRequest request) {

    return pigService.update(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

**PATCH:**

```java
@PatchMapping("/{id}")
public ResponseEntity<PigDTO> patchPig(
        @PathVariable Long id,
        @RequestBody UpdatePigRequest request) {

    return pigService.patch(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

**DELETE:**

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deletePig(
        @PathVariable Long id) {

    if (pigService.delete(id)) {
        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.notFound().build();
}
```

## Sample Test Results

| Operation | Expected Status |
|---|---|
| PUT update pig | 200 OK |
| PATCH selected field | 200 OK |
| DELETE existing pig | 204 No Content |
| DELETE the same ID again | 404 Not Found |
| GET deleted pig | 404 Not Found |

**Example PATCH Request:**

```http
PATCH /api/pigs/6
Content-Type: application/json
```

```json
{
    "weightKg": 55.0
}
```

The expected response should retain the existing tag number, name, and breed while changing only the weight.

## Evidence

- [Insert UpdatePigRequest.java screenshot.]
- [Insert PUT response showing 200 OK.]
- [Insert PATCH response showing only the selected field changed.]
- [Insert DELETE response showing 204 No Content.]
- [Insert second DELETE response showing 404 Not Found.]
- [Insert GET deleted pig response showing 404 Not Found.]

## Reflection Question

**What is the difference between PUT and PATCH in terms of what the client sends, and what must the service-layer patch() method do differently from update()?**

**Answer:**

PUT sends all updatable information and replaces the existing resource values. PATCH sends only the fields that need to be changed. The patch() service method checks each field and updates only the non-null values, while update() replaces all updatable fields using the submitted information.

---

# TASK 5 — FULL DTO LAYER AUDIT + CAPSTONE MILESTONE 3

## DTO Class List

The following DTO classes are used as request and response types in the Pig REST API:

1. `PigDTO`
2. `CreatePigRequest`
3. `UpdatePigRequest`

No JPA entity classes are used as request body parameters or response types in the active PigController.

## DTO Layer Audit

I implemented three DTO classes to separate the REST API from the database entity.

CreatePigRequest handles pig registration, UpdatePigRequest handles PUT and PATCH requests, and PigDTO is used for API responses.

The PigService converts entities into response DTOs using the `toDTO()` method before returning information to the controller.

The active PigController uses DTOs for all request and response objects. The old Pet controller from Lab 8 was retained as a legacy source file but disabled for the Week 10 API.

## 409 Conflict Scenario

### Business Rule

Each pig must have a unique tag number. Registering another pig with an existing tag number is not allowed.

The service checks whether the tag number already exists before saving the new pig.

### Request

```http
POST /api/pigs
Content-Type: application/json
```

```json
{
    "tagNumber": "PIG-W10-001",
    "name": "Duplicate Pig",
    "breed": "Landrace",
    "weightKg": 40.0
}
```

### Expected Response

```http
HTTP/1.1 409 Conflict
Content-Type: application/json
```

```json
{
    "message": "Pig tag number already exists"
}
```

## Capstone Milestone 3 — Complete REST API Smoke Check

The Pig REST API supports the complete CRUD operations using DTOs.

| Operation | Endpoint | Expected Status |
|---|---|---|
| POST | `/api/pigs` | 201 Created |
| GET | `/api/pigs/{id}` | 200 OK |
| PUT | `/api/pigs/{id}` | 200 OK |
| PATCH | `/api/pigs/{id}` | 200 OK |
| DELETE | `/api/pigs/{id}` | 204 No Content |
| GET deleted resource | `/api/pigs/{id}` | 404 Not Found |

### Test Sequence

**1. POST — Create Pig**

Registers a new pig and returns 201 Created with a Location header.

**2. GET — Retrieve Pig**

Retrieves the newly created pig and returns 200 OK.

**3. PUT — Replace Pig Information**

Updates all updatable pig information and returns 200 OK.

**4. PATCH — Partially Update Pig**

Changes only the submitted field and returns 200 OK.

**5. DELETE — Remove Pig**

Deletes the existing record and returns 204 No Content.

**6. GET — Confirm Deletion**

Attempts to retrieve the deleted pig and returns 404 Not Found.

### Evidence

- [Insert POST 201 Created screenshot showing Location header.]
- [Insert GET by ID 200 OK screenshot.]
- [Insert PUT 200 OK screenshot.]
- [Insert PATCH 200 OK screenshot showing other fields unchanged.]
- [Insert DELETE 204 No Content screenshot.]
- [Insert GET deleted pig 404 Not Found screenshot.]
- [Insert duplicate tag 409 Conflict screenshot.]
- [Insert final REST API smoke-check evidence.]

## Reflection Question

**For one endpoint in your capstone API, state the method, the URL, what status code it returns on success, what status code it returns when the resource is not found, and why there is no JPA entity in the controller method signature.**

**Answer:**

The GET by ID endpoint uses the GET method and the URL `/api/pigs/{id}`. It returns 200 OK when the pig exists and 404 Not Found when the requested pig does not exist.

I use PigDTO instead of the JPA entity in the controller because DTOs control the information exposed by the API and prevent direct exposure of database relationships and internal fields.

---

# OVERALL CONCLUSION

In this laboratory activity, I developed a RESTful CRUD API using Spring Boot and Spring Data JPA for my Automatic Pig Feeder capstone project.

I learned how to design resource-oriented URLs and implement GET, POST, PUT, PATCH, and DELETE operations using the appropriate HTTP methods and status codes.

I also implemented separate request and response DTOs to prevent direct exposure of JPA entities through the REST API. The service layer handles entity mapping, validation, and database operations, while the controller handles HTTP requests and responses.

Additionally, I implemented duplicate tag-number checking to return a 409 Conflict response when a pig is registered using an existing tag number.

Through this activity, I improved my understanding of REST API development, DTO mapping, HTTP status codes, and integrating Spring Boot controllers with a database-backed application.