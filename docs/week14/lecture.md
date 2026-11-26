# WEBDEV2 — Week 14 F2 Lecture

## 1. Form and API data flow

The browser uses vanilla JavaScript forms. The Pet form submits `microchipNumber`, `name`, `species`, `breed`, `age`, and the selected `ownerId` to `POST /api/pets` or a complete `PUT /api/pets/{id}`. The owner selector is populated from `GET /api/owners`; the browser stores the selected numeric ID in the option value, separate from the owner's display name.

The Appointment form submits `appointmentDate`, `reasonForVisit`, `petId`, and `vetId` to `POST /api/appointments` or `PUT /api/appointments/{id}`. Pet choices are loaded from `GET /api/pets`; veterinarian/date choices are loaded from `GET /api/vet-availability?date=...`. The frontend does not invent foreign keys or capacity values.

The flow is: form submit → `preventDefault()` → client validation → JSON request → controller → service → repositories → MySQL → confirmed response → refresh the persisted list. A success message is displayed only after a successful HTTP response. The submit button is disabled while the request is pending and restored in `finally`. Failed values remain in the form so the user can correct them. Messages use `textContent`, not server-provided HTML.

## 2. PUT versus PATCH

This API uses PUT for editing both resource types. Each PUT request contains the complete editable representation. Pet PUT sends all editable pet fields, including the selected owner ID. Appointment PUT sends the full date, reason, pet ID, and veterinarian ID; if its schedule changes, the service adjusts old and new capacity within one transaction. PATCH would be appropriate only if the API explicitly documented a partial field update. The frontend does not treat PUT and PATCH as interchangeable.

## 3. Validation responsibility

Browser `required`, `min`, `maxlength`, `reportValidity()`, and JavaScript numeric checks improve immediate feedback but can be bypassed. Spring Bean Validation checks request fields at the API boundary. Services verify that selected records and schedule rows exist and that capacity remains. MySQL is the final integrity guard through NOT NULL, UNIQUE, CHECK, primary-key, and foreign-key constraints. A duplicate microchip or referenced pet deletion is reported as a conflict. Client validation is not a replacement for server or database validation.

## 4. Delete policy and exceptional paths

A pet may not be deleted while an appointment or vaccination record references it. The existing foreign keys use RESTRICT, and the Pet service translates the integrity conflict into HTTP 409. This protects clinic history instead of cascading away related records. Appointments are canceled through DELETE: the service restores one veterinarian/date slot and deletes the appointment in the same transaction. DELETE returns 204 with no response body, so the request helper does not attempt to parse JSON for that status.

The shared request helper supports JSON, text, and empty 204 responses. It throws an error containing the HTTP status and a safe message for 400 validation, 404 missing resources, and 409 conflicts. If the backend is stopped, the browser reports the network failure; the user can correct the issue or retry after the server is available. The UI does not print arbitrary server HTML.

## 5. Atomic appointment booking

The appointment workflow is one service endpoint, not two independent frontend writes. `AppointmentService.bookAppointment` runs under `@Transactional`: it locks the selected `vet_availability` row with `PESSIMISTIC_WRITE`, checks for a remaining slot, decrements capacity, and inserts the appointment. If the pet lookup or appointment insert fails after the decrement, the unchecked exception propagates and Spring rolls the transaction back. The database is checked before and after a controlled invalid-pet request to prove rollback; a browser error alone is not evidence.

Appointment PUT locks the old and new schedule rows in stable order before moving capacity and updating the appointment. Appointment DELETE restores capacity and deletes the row atomically. Locking the schedule row prevents two concurrent requests from both taking the last slot. The service's public transactional methods are invoked by the controller through Spring's proxy; no self-invocation is used for the transactional boundary.

## 6. Evidence still to capture locally

The code package includes `docs/week14/api-tests.http` and a test matrix in `checkpoint.md`. Execute the requests against the actual MySQL database, record response codes and request bodies, compare the changed rows with direct MySQL queries, and add real screenshots/exports. No browser/API/MySQL execution is claimed until those steps have been performed.
