# LAB 8: DATABASE-BACKED CRUD APPLICATION

**Course:** WEBDEV2  
**Week:** 8 (Midterm)  
**Project:** webdev2  
**Branch:** week8  
**Database:** webdev2_week8  
**Framework:** Spring Boot, Spring Data JPA, MariaDB

---

# TASK 1 — CREATE THE REPOSITORY INTERFACE

## Objective

Create a Pet entity and a Spring Data JPA repository to perform database operations without writing SQL queries manually.

## Implementation

Created a `Pet` entity with the following fields:

- id
- microchipNumber
- name
- species
- breed

The `microchipNumber` field uses `@Column(unique = true, nullable = false)` to ensure that every pet has a unique and required microchip number.

Created `PetRepository.java` extending `JpaRepository<Pet, Long>`.

```java
package com.example.webdev2.repository;

import com.example.webdev2.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {

}
```

Created `PetSmokeTestRunner.java` to insert a sample pet if the database table is empty and display the saved records using `findAll()`.

## Expected Console Output

```text
===== PET RECORDS =====

ID: 1
Name: Max
Species: Dog
Breed: Golden Retriever
-----------------------
```

## Result

The application successfully connected to the MariaDB database and retrieved the existing pet record using the Spring Data JPA repository.

**Evidence:**
- [Insert screenshot of Pet.java]
- [Insert screenshot of PetRepository.java]
- [Insert screenshot of console output and MySQL/MariaDB result]

---

# TASK 2 — IMPLEMENT FULL CRUD VIA A SERVICE LAYER

## Objective

Implement complete CRUD operations using a service layer and REST controller.

## Implementation

Created `PetService.java` using the `@Service` annotation and constructor injection of `PetRepository`.

The service contains five operations:

1. `registerPet()` — Registers a new pet.
2. `getAllPets()` — Retrieves all pets.
3. `getPetById()` — Retrieves a specific pet using its ID.
4. `updatePet()` — Updates an existing pet.
5. `removePet()` — Deletes a pet.

Created `PetController.java` using `@RestController` and `@RequestMapping("/api/pets")`.

## REST API Endpoints

| HTTP Method | Endpoint | Description |
|---|---|---|
| POST | `/api/pets` | Register a pet |
| GET | `/api/pets` | Retrieve all pets |
| GET | `/api/pets/{id}` | Retrieve a pet by ID |
| PUT | `/api/pets/{id}` | Update a pet |
| DELETE | `/api/pets/{id}` | Delete a pet |

## Sample Requests

### 1. Register a Pet

**Method:** POST  
**URL:** `http://localhost:8080/api/pets`

```json
{
    "microchipNumber": "CHIP-0002",
    "name": "Buddy",
    "species": "Dog",
    "breed": "Labrador"
}
```

Expected status: `201 Created`

### 2. Retrieve All Pets

**Method:** GET  
**URL:** `http://localhost:8080/api/pets`

Expected status: `200 OK`

### 3. Retrieve Pet by ID

**Method:** GET  
**URL:** `http://localhost:8080/api/pets/1`

Expected status: `200 OK`

### 4. Update a Pet

**Method:** PUT  
**URL:** `http://localhost:8080/api/pets/1`

```json
{
    "microchipNumber": "CHIP-0001",
    "name": "Max Updated",
    "species": "Dog",
    "breed": "German Shepherd"
}
```

Expected status: `200 OK`

### 5. Delete a Pet

**Method:** DELETE  
**URL:** `http://localhost:8080/api/pets/1`

Expected status: `204 No Content`

## Result

The service and controller provide the complete Create, Read, Update, and Delete functionality for the Pet entity.

**Evidence:**
- [Insert screenshot of PetService.java]
- [Insert screenshot of PetController.java]
- [Insert screenshot of POST request]
- [Insert screenshot of GET requests]
- [Insert screenshot of PUT request]
- [Insert screenshot of DELETE request]

---

# TASK 3 — DERIVED AND CUSTOM QUERY METHODS

## Objective

Create derived query methods and a custom JPQL query to search and retrieve pet records.

## Implementation

Added three query methods to `PetRepository.java`.

### 1. Search by Species

```java
List<Pet> findBySpeciesIgnoreCase(String species);
```

This method searches for pets by species without considering uppercase or lowercase letters.

### 2. Search by Name

```java
List<Pet> findByNameContainingIgnoreCase(String keyword);
```

This method searches for pets whose names contain the given keyword.

### 3. Retrieve Distinct Species

```java
@Query("SELECT DISTINCT p.species FROM Pet p")
List<String> findAllSpecies();
```

This JPQL query retrieves the unique species stored in the Pet table.

## REST API Endpoints

| HTTP Method | Endpoint | Description |
|---|---|---|
| GET | `/api/pets/species?name=Dog` | Search pets by species |
| GET | `/api/pets/search?keyword=Max` | Search pets by name |
| GET | `/api/pets/species-list` | Retrieve distinct species |

## Sample Responses

### 1. Search by Species

**GET:** `http://localhost:8080/api/pets/species?name=Dog`

```json
[
    {
        "id": 1,
        "microchipNumber": "CHIP-0001",
        "name": "Max",
        "species": "Dog",
        "breed": "Golden Retriever"
    }
]
```

### 2. Search by Name

**GET:** `http://localhost:8080/api/pets/search?keyword=Max`

```json
[
    {
        "id": 1,
        "microchipNumber": "CHIP-0001",
        "name": "Max",
        "species": "Dog",
        "breed": "Golden Retriever"
    }
]
```

### 3. Retrieve All Species

**GET:** `http://localhost:8080/api/pets/species-list`

```json
[
    "Dog"
]
```

*Note: These are example responses. Actual results depend on the records stored in the database.*

## Result

The repository now supports derived queries and a custom JPQL query. These methods allow users to search pets by species, search names using keywords, and retrieve distinct species.

**Evidence:**
- [Insert screenshot of PetRepository.java]
- [Insert screenshot of species search response]
- [Insert screenshot of name search response]
- [Insert screenshot of species list response]

---

# TASK 4 — APPLY @TRANSACTIONAL AND OBSERVE ROLLBACK

## Objective

Demonstrate that a transaction rolls back all database changes when an error occurs during batch registration.

## Implementation

Added the `@Transactional` annotation to the `batchRegister()` method in `PetService.java`.

```java
@Transactional
public void batchRegister(List<Pet> pets) {

    for (Pet pet : pets) {
        petRepository.save(pet);
    }

}
```

Added a batch registration endpoint in `PetController.java`.

```java
@PostMapping("/batch")
public ResponseEntity<String> batchRegister(
        @RequestBody List<Pet> pets) {

    petService.batchRegister(pets);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body("Pets registered successfully!");
}
```

## Testing the Rollback

**Method:** POST  
**URL:** `http://localhost:8080/api/pets/batch`

Used the following JSON request:

```json
[
    {
        "microchipNumber": "ROLLBACK-TEST-001",
        "name": "Rocky",
        "species": "Dog",
        "breed": "Bulldog"
    },
    {
        "microchipNumber": "ROLLBACK-TEST-001",
        "name": "Milo",
        "species": "Cat",
        "breed": "Persian"
    }
]
```

Both pets have the same microchip number, which violates the UNIQUE constraint.

## Database Verification

Executed the following SQL query:

```sql
USE webdev2_week8;

SELECT * FROM pet
WHERE microchip_number = 'ROLLBACK-TEST-001';
```

Expected result:

```text
Empty set
```

## Observation

The `@Transactional` annotation makes batch registration an atomic operation. When the second pet causes a duplicate microchip error, the transaction is rolled back.

This means the first pet is also not saved to the database.

## Conclusion

The activity demonstrates that `@Transactional` prevents partial database updates when an error occurs during a batch operation.

**Evidence:**
- [Insert screenshot of batchRegister() showing @Transactional]
- [Insert screenshot of REST Client request and error response]
- [Insert screenshot of MariaDB query showing zero matching records]

---

# TASK 5 — DEBUG A PERSISTENCE ERROR

## Objective

Identify and fix common JPA persistence errors by examining stack traces and SQL logs.

## Identified Defects

### 1. Missing @GeneratedValue

The original Pet entity had an ID field without the `@GeneratedValue` annotation.

Broken code:

```java
@Id
private Long id;
```

This caused an `IdentifierGenerationException` because Hibernate expected the ID to be manually assigned.

**Fix:**

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

This allows MariaDB to automatically generate the ID for new pet records.

### 2. Missing Species Field

The original Pet entity did not contain a `species` field.

Without this field, the application cannot properly store the pet's species.

**Fix:**

```java
private String species;

public String getSpecies() {
    return species;
}

public void setSpecies(String species) {
    this.species = species;
}
```

### 3. Null Microchip Number

The broken service code attempted to save a pet with a null microchip number.

Broken code:

```java
Pet p = new Pet();
p.setMicrochipNumber(null);
petRepository.save(p);
```

This violates the database constraint because the microchip number is marked as NOT NULL.

**Fix:**

Validate the microchip number before saving:

```java
if (pet.getMicrochipNumber() == null ||
        pet.getMicrochipNumber().isBlank()) {

    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Microchip number is required");
}
```

This prevents invalid data from being saved.

## Debugging Process

### Step 1 — Examine the Stack Trace

During testing, the application returned a 500 Internal Server Error.

The actual terminal error was:

```text
org.hibernate.id.IdentifierGenerationException:

Identifier of entity 'com.example.webdev2.entity.Pet'
must be manually assigned before calling 'persist()'
```

This error indicated that the Pet entity was missing its automatic ID generation annotation.

### Step 2 — Examine the SQL Logs

Enabled SQL logging in `application.properties`:

```properties
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

The SQL logs showed that the application successfully connected to MariaDB and retrieved existing pet records.

However, the attempted registration failed before Hibernate could execute the INSERT for the new pet because the ID generation strategy was missing.

### Step 3 — Apply the Fixes

Corrected the Pet entity by adding:

- `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- The missing `species` field
- Its getter and setter methods

Also restored validation in `PetService.java` to prevent null microchip numbers.

### Final Result

After correcting the identified problems, the application was ready for testing with a properly configured Pet entity and validated registration method.

A successful registration should return:

```text
HTTP/1.1 201
```

The newly registered pet should also appear in the MariaDB `pet` table.

**Evidence:**
- [Insert screenshot of broken Pet entity]
- [Insert screenshot of IdentifierGenerationException]
- [Insert screenshot of SQL logs]
- [Insert screenshot of corrected Pet.java]
- [Insert screenshot of successful pet registration]

---

# OVERALL CONCLUSION

In this laboratory activity, I developed a database-backed CRUD application using Spring Boot, Spring Data JPA, and MariaDB.

I learned how to create JPA entities and repositories, implement CRUD operations using a service and controller, create derived and custom query methods, and use `@Transactional` to handle database rollback.

I also practiced debugging common persistence errors by examining stack traces and SQL logs. Through this activity, I gained a better understanding of how Spring Boot communicates with a database and how to manage data using JPA.


